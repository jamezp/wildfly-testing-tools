/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.testing.junit.extension;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.support.AnnotationSupport;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.wildfly.plugin.tools.Deployment;
import org.wildfly.plugin.tools.DeploymentResult;
import org.wildfly.plugin.tools.UndeployDescription;
import org.wildfly.plugin.tools.server.DomainConfiguration;
import org.wildfly.plugin.tools.server.ServerManager;
import org.wildfly.plugin.tools.server.ServerManagerException;
import org.wildfly.plugin.tools.server.StandaloneConfiguration;
import org.wildfly.testing.junit.annotations.DeploymentProducer;
import org.wildfly.testing.junit.annotations.Domain;
import org.wildfly.testing.junit.api.DomainConfigurationFactory;
import org.wildfly.testing.junit.api.ServerConfiguration;
import org.wildfly.testing.junit.api.StandaloneConfigurationFactory;

/**
 * JUnit extension that manages WildFly server lifecycle and per-test-class deployments.
 * <p>
 * The server is started once and shared across all test classes (suite-level lifecycle).
 * Deployments are managed per-test-class (class-level lifecycle):
 * <ul>
 * <li>{@code beforeAll}: Start server (if needed) and deploy test's application</li>
 * <li>{@code afterAll}: Undeploy test's application</li>
 * </ul>
 *
 * @author <a href="mailto:jperkins@ibm.com">James R. Perkins</a>
 */
public class WildFlyExtension implements BeforeAllCallback, AfterAllCallback {
    private static final Logger LOGGER = Logger.getLogger(WildFlyExtension.class);

    private static final String SERVER_KEY = "wildfly.server";
    private static final ExtensionContext.Namespace SERVER_NAMESPACE = ExtensionContext.Namespace
            .create("WildFly.Server");

    @Override
    public void beforeAll(final ExtensionContext context) {
        // Start server (if not already started) - shared across all test classes
        final ServerManager serverManager = getOrStartServer(context);

        // Check if deployment already exists in cache (shouldn't happen, but be defensive)
        if (DeploymentContext.resolveDeployment(context).isPresent()) {
            return; // Already deployed
        }

        // Deploy any deployments and cache the deployment information
        deploy(serverManager, context);
    }

    @Override
    public void afterAll(final ExtensionContext context) {
        // Get deployment info
        final Optional<DeploymentInfo> deploymentInfo = DeploymentContext.resolveDeployment(context);
        if (deploymentInfo.isEmpty()) {
            return;
        }
        final Optional<ServerManager> opt = getServer(context);
        if (opt.isEmpty()) {
            return;
        }
        final ServerManager serverManager = opt.get();
        // Check for @Domain annotation
        final Optional<Domain> domain = AnnotationSupport
                .findAnnotation(context.getRequiredTestClass(), Domain.class);
        final String deploymentName = deploymentInfo.get().deploymentName();
        final UndeployDescription undeployDescription = UndeployDescription.of(deploymentName);
        if (domain.isPresent()) {
            undeployDescription.addServerGroups(deploymentInfo.get().serverGroup());
        }
        // Undeploy from server
        try {
            final DeploymentResult result = serverManager.deploymentManager()
                    .undeploy(undeployDescription);
            if (!result.successful()) {
                LOGGER.warnf("Failed to undeploy application %s: %s", deploymentName, result.getFailureMessage());
            }
        } catch (Exception e) {
            LOGGER.warnf(e, "Failed to undeploy application %s.", deploymentName);
        }

        // Remove from cache
        DeploymentContext.remove(context);
    }

    /**
     * Gets or starts the shared WildFly server instance.
     * The server is stored in the root context store and shared across all test classes.
     *
     * @param context the extension context
     *
     * @return the server manager
     */
    @SuppressWarnings("resource")
    private ServerManager getOrStartServer(final ExtensionContext context) {
        final ExtensionContext.Store store = getStore(context);

        return store.computeIfAbsent(SERVER_KEY, key -> {
            // Start the server
            final ServerManager serverManager = startServer(context);

            // Return a CloseableResource that stops server on cleanup
            return new ServerResource(serverManager, context);
        }, ServerResource.class).get();
    }

    /**
     * Gets the already-started server, if available.
     * Package-private to allow access from {@link ServerManagerProducer}.
     *
     * @param context the extension context
     *
     * @return the server manager, or empty if not started
     */
    static Optional<ServerManager> getServer(final ExtensionContext context) {
        final ExtensionContext.Store store = getStore(context);
        final ServerResource resource = store.get(SERVER_KEY, ServerResource.class);
        return Optional.ofNullable(resource).map(ServerResource::get);
    }

    /**
     * Starts the WildFly server and waits for it to be ready.
     *
     * @param context the extension context
     *
     * @return the started server manager
     */
    private ServerManager startServer(final ExtensionContext context) {
        try {
            // Check for @Domain annotation
            final Optional<Domain> domain = AnnotationSupport
                    .findAnnotation(context.getRequiredTestClass(), Domain.class);
            // Determine configuration based on launch type
            final ServerManager serverManager;
            if (domain.isPresent()) {
                final DomainConfiguration configuration = DomainConfigurationFactory.create()
                        .configuration(context);
                serverManager = ServerManager.start(configuration);
            } else {
                final StandaloneConfiguration configuration = StandaloneConfigurationFactory.create()
                        .configuration(context);
                serverManager = ServerManager.start(configuration);
            }

            // Get timeout from configuration (defaults to 60 seconds)
            final long timeout = ServerConfiguration.timeout(context);

            // Wait for server to start
            if (!serverManager.waitFor(timeout, TimeUnit.SECONDS)) {
                serverManager.kill();
                throw new AssertionError("WildFly server did not start within " + timeout + " seconds");
            }

            return serverManager;
        } catch (ServerManagerException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new JUnitException("Failed to start WildFly server", e);
        }
    }

    /**
     * Stops the WildFly server gracefully.
     *
     * @param context       the extension context
     * @param serverManager the server manager to stop
     */
    private void stopServer(final ExtensionContext context, final ServerManager serverManager) {
        try {
            // Get timeout from configuration (defaults to 60 seconds)
            final long timeout = ServerConfiguration.timeout(context);
            serverManager.shutdown(timeout);
        } catch (IOException e) {
            // If graceful shutdown fails, kill the server
            serverManager.kill();
            LOGGER.error("Failed to stop server", e);
        }
    }

    /**
     * Deploys an archive to the server.
     *
     * @param serverManager the server manager
     * @param context       the extension context
     *
     * @throws JUnitException if deployment fails
     */
    private void deploy(final ServerManager serverManager, final ExtensionContext context) {

        // Find deployment method for this test class
        final Optional<Method> deploymentMethod = resolveDeploymentMethod(context);

        if (deploymentMethod.isEmpty()) {
            return; // No deployment for this test
        }

        final Method method = deploymentMethod.get();

        // Invoke deployment method to get Archive
        final Archive<?> archive;
        try {
            if (Modifier.isStatic(method.getModifiers())) {
                archive = (Archive<?>) method.invoke(null);
            } else {
                throw new JUnitException(String.format("Deployment method %s is required to be static for test %s", method,
                        context.getTestClass().orElse(null)));
            }
        } catch (Exception e) {
            throw new JUnitException("Failed to create deployment from method " + method, e);
        }
        final String deploymentName = archive.getName();
        // Check for @Domain annotation
        final Optional<Domain> domain = AnnotationSupport.findAnnotation(context.getRequiredTestClass(), Domain.class);
        final Set<String> serverGroups;
        if (domain.isPresent()) {
            serverGroups = Set.of(domain.get().value());
            if (serverGroups.isEmpty()) {
                throw new JUnitException("No server groups defined for domain to deploy to.");
            }
        } else {
            serverGroups = Set.of();
        }

        // Convert Archive to Deployment
        try (
                var in = archive.as(ZipExporter.class).exportAsInputStream();
                var deployment = Deployment.of(in, deploymentName)) {
            // Set the server groups, which could be empty for a standalone server
            deployment.setServerGroups(serverGroups);

            // Deploy to server
            final var deploymentResult = serverManager.deploymentManager().deploy(deployment);
            if (!deploymentResult.successful()) {
                throw new JUnitException(String.format("Failed to deploy %s to server: %s", deploymentName,
                        deploymentResult.getFailureMessage()));
            }
            DeploymentContext.cache(context, new DeploymentInfo(deploymentName, serverGroups));
        } catch (IOException e) {
            throw new JUnitException(
                    String.format("Failed to export archive %s as deployment", deploymentName), e);
        }
    }

    private static ExtensionContext.Store getStore(final ExtensionContext context) {
        return context.getRoot().getStore(ExtensionContext.StoreScope.LAUNCHER_SESSION, SERVER_NAMESPACE);
    }

    private static Optional<Method> resolveDeploymentMethod(final ExtensionContext context) {
        final Class<?> testClass = context.getRequiredTestClass();
        final List<Method> deploymentMethods = AnnotationSupport.findAnnotatedMethods(
                testClass,
                DeploymentProducer.class,
                HierarchyTraversalMode.TOP_DOWN);

        // No deployment method found
        if (deploymentMethods.isEmpty()) {
            return Optional.empty();
        }

        // Validate: only one deployment method allowed per test class
        if (deploymentMethods.size() > 1) {
            throw new JUnitException(
                    String.format("Only one @DeploymentProducer method is allowed per test class. Found %d in %s",
                            deploymentMethods.size(), testClass.getName()));
        }
        final Method deploymentMethod = deploymentMethods.get(0);

        // Validate: return type must be Archive
        if (!Archive.class.isAssignableFrom(deploymentMethod.getReturnType())) {
            throw new JUnitException(
                    String.format("@DeploymentProducer method %s.%s() must return %s, but returns %s",
                            testClass.getName(),
                            deploymentMethod.getName(),
                            Archive.class.getName(),
                            deploymentMethod.getReturnType().getName()));
        }
        return Optional.of(deploymentMethod);
    }

    /**
     * Wrapper for ServerManager that implements AutoCloseable for automatic cleanup.
     */
    private class ServerResource implements AutoCloseable {
        private final ServerManager serverManager;
        private final ExtensionContext context;

        ServerResource(final ServerManager serverManager, final ExtensionContext context) {
            this.serverManager = serverManager;
            this.context = context;
        }

        ServerManager get() {
            return serverManager;
        }

        @Override
        public void close() {
            stopServer(context, serverManager);
        }
    }
}
