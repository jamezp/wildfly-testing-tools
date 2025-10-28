/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.testing.junit.extension;

import java.net.URI;
import java.util.Optional;
import java.util.function.Supplier;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Store;

/**
 * Holds deployment information for a test class, including the deployment name and resolved URI.
 * This class also manages caching deployment information in the JUnit {@link Store}.
 *
 * @author <a href="mailto:jperkins@ibm.com">James R. Perkins</a>
 */
class DeploymentContext {
    private static final ExtensionContext.Namespace DEPLOYMENT_NAMESPACE = ExtensionContext.Namespace
            .create("WildFly.Deployment");
    private static final String DEPLOYMENT_KEY = "deployment";

    /**
     * Retrieves cached deployment information for the current test class.
     *
     * @param context the extension context
     *
     * @return the cached deployment info, or empty if not cached
     */
    static Optional<DeploymentInfo> resolveDeployment(final ExtensionContext context) {
        final String key = deploymentKey(context.getRequiredTestClass());
        final Store store = getStore(context);
        return Optional.ofNullable(store.get(key, DeploymentInfo.class));
    }

    /**
     * Retrieves cached URI for the deployment.
     *
     * @param context the extension context
     *
     * @return the cached URI, or empty if not cached
     */
    static URI computeIfAbsent(final ExtensionContext context, final Supplier<URI> supplier) {
        final String key = uriKey(context.getRequiredTestClass());
        final Store store = getStore(context);
        return store.computeIfAbsent(key, (k) -> supplier.get(), URI.class);
    }

    /**
     * Caches deployment information for the current test class.
     *
     * @param context the extension context
     * @param info    the deployment information to cache
     */
    static void cache(final ExtensionContext context, final DeploymentInfo info) {
        final String key = deploymentKey(context.getRequiredTestClass());
        final Store store = getStore(context);
        store.put(key, info);
    }

    /**
     * Removes cached deployment information for the current test class.
     *
     * @param context the extension context
     */
    static void remove(final ExtensionContext context) {
        final Store store = getStore(context);
        store.remove(deploymentKey(context.getRequiredTestClass()));
        store.remove(uriKey(context.getRequiredTestClass()));
    }

    private static String deploymentKey(final Class<?> testClass) {
        return DEPLOYMENT_KEY + "-" + testClass.getName();
    }

    private static String uriKey(final Class<?> testClass) {
        return DEPLOYMENT_KEY + "-" + testClass.getName() + "-uri";
    }

    private static Store getStore(final ExtensionContext context) {
        // We want to store on the class context, attempt to determine which that context
        final ExtensionContext usingContext;
        if (context.getTestMethod().isPresent()) {
            usingContext = context.getParent().orElse(context);
        } else {
            usingContext = context;
        }
        return usingContext.getStore(DEPLOYMENT_NAMESPACE);
    }
}
