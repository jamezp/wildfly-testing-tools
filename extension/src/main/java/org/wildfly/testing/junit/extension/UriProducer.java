/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.testing.junit.extension;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.util.Optional;
import java.util.function.Supplier;

import org.jboss.as.controller.client.helpers.Operations;
import org.jboss.dmr.ModelNode;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.JUnitException;
import org.kohsuke.MetaInfServices;
import org.wildfly.plugin.tools.server.DomainManager;
import org.wildfly.plugin.tools.server.ServerManager;
import org.wildfly.testing.junit.annotations.DomainServer;
import org.wildfly.testing.junit.annotations.RequestPath;
import org.wildfly.testing.junit.api.ServerConfiguration;
import org.wildfly.testing.junit.api.ServerResourceProducer;

/**
 * Produces {@link URI} instances for injection into test fields and parameters.
 * The URI is resolved from the deployed application's base URI, optionally
 * combined with a {@link RequestPath} qualifier.
 *
 * @author <a href="mailto:jperkins@ibm.com">James R. Perkins</a>
 */
@MetaInfServices
public class UriProducer implements ServerResourceProducer {
    @Override
    public boolean canInject(final ExtensionContext context, final Class<?> clazz, final Annotation... annotations) {
        return URI.class.isAssignableFrom(clazz);
    }

    @Override
    public Object produce(final ExtensionContext context, final Class<?> clazz, final Annotation... annotations)
            throws IllegalArgumentException {
        if (!URI.class.isAssignableFrom(clazz)) {
            throw new IllegalArgumentException(
                    String.format("Type %s is not assignable to %s", clazz.getName(), URI.class.getName()));
        }

        final Optional<ServerManager> opt = WildFlyExtension.getServer(context);
        if (opt.isEmpty()) {
            // Shouldn't happen, but we have no server so we can't resolve anything
            return URI.create(ServerConfiguration.resolveBaseUri(context));
        }
        final ServerManager server = opt.get();
        final Supplier<URI> supplier;

        // Get deployment info from cache
        final Optional<DeploymentInfo> deploymentInfo = DeploymentContext.resolveDeployment(context);
        if (deploymentInfo.isEmpty()) {
            supplier = () -> URI.create(ServerConfiguration.resolveBaseUri(context));
        } else {
            // Create the supplier for cases when we have not yet resolved the URI
            final DomainServer serverGroup = findQualifier(DomainServer.class, annotations);
            if (serverGroup == null) {
                supplier = () -> resolveDeploymentUri(context, server, deploymentInfo.get().deploymentName());
            } else {
                supplier = () -> resolveDeploymentUri(context, server, deploymentInfo.get()
                        .deploymentName(), serverGroup.value());
            }
        }

        final URI baseUri = DeploymentContext.computeIfAbsent(context, supplier);

        // Check for RequestPath qualifier to append to base URI
        final RequestPath requestPath = findQualifier(RequestPath.class, annotations);
        if (requestPath != null) {
            return createUri(baseUri, requestPath.value());
        }

        return baseUri;
    }

    /**
     * Creates a URI by appending a path to a base URI, handling slashes correctly.
     *
     * @param baseUri the base URI
     * @param path    the path to append
     *
     * @return the combined URI
     */
    private URI createUri(final URI baseUri, final String path) {
        final String uriString = baseUri.toString();
        if (uriString.endsWith("/")) {
            if (path.startsWith("/")) {
                return URI.create(uriString + path.substring(1));
            } else {
                return URI.create(uriString + path);
            }
        }
        // Handle double slashes when both base ends with "/" and path starts with "/"
        if (path.startsWith("/")) {
            return URI.create(uriString + path);
        }
        return URI.create(uriString + "/" + path);
    }

    /**
     * Finds a qualifier annotation in an array of annotations.
     *
     * @param qualifier   the qualifier type to find
     * @param annotations the annotations to search
     * @param <T>         the qualifier type
     *
     * @return the qualifier annotation, or {@code null} if not found
     */
    private static <T extends Annotation> T findQualifier(final Class<T> qualifier, final Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            if (annotation.annotationType().equals(qualifier)) {
                return qualifier.cast(annotation);
            }
        }
        return null;
    }

    /**
     * Resolves the URI for a deployed application by querying the Undertow subsystem.
     * If the context-root cannot be determined (e.g., Undertow not available, non-web deployment),
     * returns a default base URI.
     *
     * @param context        the extension context
     * @param serverManager  the server manager
     * @param deploymentName the deployment name
     *
     * @return the resolved URI
     */
    private URI resolveDeploymentUri(final ExtensionContext context, final ServerManager serverManager,
            final String deploymentName) {
        String baseUri = ServerConfiguration.resolveBaseUri(context);

        // Try to get the context-root from Undertow subsystem
        final ModelNode address = Operations.createAddress(
                "deployment", deploymentName,
                "subsystem", "undertow");

        try {
            final ModelNode result = serverManager.client()
                    .execute(Operations.createReadAttributeOperation(address, "context-root"));

            // If successful, append the context-root to base URI
            if (Operations.isSuccessfulOutcome(result)) {
                final String contextRoot = Operations.readResult(result).asString();
                baseUri = baseUri + (contextRoot.startsWith("/") ? contextRoot : "/" + contextRoot);
            }
            // If not successful, just use base URI (might not be a web deployment)
        } catch (IOException ignore) {
            // Ignore - Undertow might not be available, or this might not be a web deployment
            // Just use the default base URI
        }

        return URI.create(baseUri);
    }

    private URI resolveDeploymentUri(final ExtensionContext context, final ServerManager serverManager,
            final String deploymentName,
            final String domainServer) {
        if (serverManager instanceof DomainManager domainManager) {
            String baseUri = ServerConfiguration.resolveBaseUri(context);

            try {
                // Try to get the context-root from Undertow subsystem
                final ModelNode address = domainManager.determineHostAddress()
                        .add("server", domainServer)
                        .add("deployment", deploymentName)
                        .add("subsystem", "undertow");
                final ModelNode result = serverManager.client()
                        .execute(Operations.createReadAttributeOperation(address, "context-root"));

                // If successful, append the context-root to base URI
                if (Operations.isSuccessfulOutcome(result)) {
                    final String contextRoot = Operations.readResult(result).asString();
                    baseUri = baseUri + (contextRoot.startsWith("/") ? contextRoot : "/" + contextRoot);
                }
                // If not successful, just use base URI (might not be a web deployment)
            } catch (IOException e) {
                // Ignore - Undertow might not be available, or this might not be a web deployment
                // Just use the default base URI
            }
            return URI.create(baseUri);
        }
        throw new JUnitException(String.format("ServerManager %s is not a DomainManager", serverManager));
    }
}
