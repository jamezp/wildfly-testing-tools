/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.testing.junit.extension;

import java.io.IOException;
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
import org.wildfly.plugin.tools.Deployment;
import org.wildfly.plugin.tools.DeploymentResult;
import org.wildfly.plugin.tools.UndeployDescription;
import org.wildfly.plugin.tools.server.Configuration;
import org.wildfly.plugin.tools.server.ServerManager;
import org.wildfly.plugin.tools.server.ServerManagerListener;
import org.wildfly.testing.junit.extension.annotation.Domain;
import org.wildfly.testing.junit.extension.annotation.ManualMode;
import org.wildfly.testing.junit.extension.api.DomainConfigurationFactory;
import org.wildfly.testing.junit.extension.api.ServerConfiguration;
import org.wildfly.testing.junit.extension.api.StandaloneConfigurationFactory;

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
    private static final String SERVER_LISTENER_KEY = "wildfly.server.listener";
    private static final ExtensionContext.Namespace SERVER_NAMESPACE = ExtensionContext.Namespace
            .create("WildFly.Server");

    @Override
    public void beforeAll(final ExtensionContext context) throws Exception {
        // Start server (if not already started) - shared across all test classes
        final ServerManager serverManager = getOrCreateServerManager(context);

        final Optional<ManualMode> manualMode = TestSupport.getManualMode(context);

        if (manualMode.isEmpty()) {
            if (!serverManager.isRunning()) {
                // Get timeout from configuration (defaults to 60 seconds)
                final long timeout = ServerConfiguration.timeout(context);
                // Start the server
                serverManager.start(timeout, TimeUnit.SECONDS);
            }
            // Deploy any deployments and cache the deployment information
            deploy(serverManager, context);
        } else {
            final var autoStart = manualMode.get().value();
            if (autoStart) {
                if (!serverManager.isRunning()) {
                    // Get timeout from configuration (defaults to 60 seconds)
                    final long timeout = ServerConfiguration.timeout(context);
                    // Start the server
                    serverManager.start(timeout, TimeUnit.SECONDS);
                }
                deploy(serverManager, context);
            } else {
                if (serverManager.isRunning()) {
                    LOGGER.debugf("Shutting down server for manual mode test %s", context.getRequiredTestClass()
                            .getName());
                    stopServer(context, serverManager);
                }
                final var listener = new ExtensionServerManagerListener(context, serverManager);
                getClassStore(context).put(SERVER_LISTENER_KEY, listener);
                serverManager.addServerManagerListener(listener);
            }
        }
    }

    @Override
    public void afterAll(final ExtensionContext context) {
        final Optional<ServerManager> opt = getServer(context);
        if (opt.isEmpty()) {
            return;
        }
        final ServerManager serverManager = opt.get();
        // Get deployment info
        final Optional<DeploymentInfo> deploymentInfo = DeploymentContext.resolveDeployment(context);
        if (deploymentInfo.isEmpty()) {
            return;
        }
        undeploy(context, serverManager, deploymentInfo.get());
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
    private ServerManager getOrCreateServerManager(final ExtensionContext context) {
        final ExtensionContext.Store store = getGlobalStore(context);

        return store.computeIfAbsent(SERVER_KEY, key -> {
            // Start the server
            final ServerManager serverManager = createServer(context);
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
        final ExtensionContext.Store store = getGlobalStore(context);
        final ServerResource resource = store.get(SERVER_KEY, ServerResource.class);
        return Optional.ofNullable(resource).map(ServerResource::get);
    }

    private ServerManager createServer(final ExtensionContext context) {
        // Check for @Domain annotation
        final Optional<Domain> domain = AnnotationSupport
                .findAnnotation(context.getRequiredTestClass(), Domain.class);
        // Determine configuration based on launch type
        final Configuration<?> configuration;
        if (domain.isPresent()) {
            configuration = DomainConfigurationFactory.create()
                    .configuration(context);
        } else {
            configuration = StandaloneConfigurationFactory.create()
                    .configuration(context);
        }
        return ServerManager.of(configuration);
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
        // Check if deployment already exists in cache (shouldn't happen, but be defensive)
        if (DeploymentContext.resolveDeployment(context).isPresent()) {
            return; // Already deployed
        }

        // Find deployment method for this test class
        final Optional<Archive<?>> deploymentArchive = resolveDeployment(context);

        if (deploymentArchive.isEmpty()) {
            return; // No deployment for this test
        }

        // Invoke deployment method to get Archive
        final Archive<?> archive = deploymentArchive.get();
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

    private void undeploy(final ExtensionContext context, final ServerManager serverManager,
            final DeploymentInfo deploymentInfo) {
        // Check for @Domain annotation
        final Optional<Domain> domain = AnnotationSupport
                .findAnnotation(context.getRequiredTestClass(), Domain.class);
        final String deploymentName = deploymentInfo.deploymentName();
        final UndeployDescription undeployDescription = UndeployDescription.of(deploymentName);
        if (domain.isPresent()) {
            undeployDescription.addServerGroups(deploymentInfo.serverGroup());
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

    private static ExtensionContext.Store getGlobalStore(final ExtensionContext context) {
        return context.getRoot().getStore(ExtensionContext.StoreScope.LAUNCHER_SESSION, SERVER_NAMESPACE);
    }

    private static ExtensionContext.Store getClassStore(final ExtensionContext context) {
        // We want to store on the class context, attempt to determine which that context
        final ExtensionContext usingContext;
        if (context.getTestMethod().isPresent()) {
            usingContext = context.getParent().orElse(context);
        } else {
            usingContext = context;
        }
        return usingContext.getStore(SERVER_NAMESPACE);
    }

    private static Optional<Archive<?>> resolveDeployment(final ExtensionContext context) {
        final Class<?> testClass = context.getRequiredTestClass();
        final Optional<Archive<?>> testDeployment = TestSupport.findDeploymentMethod(context);
        final Optional<Archive<?>> deploymentProducer = TestSupport.findDeploymentProducerMethod(context);
        if (testDeployment.isPresent() && deploymentProducer.isPresent()) {
            throw new JUnitException(
                    String.format("Test %s cannot have both @GenerateDeployment and @DeploymentProducer methods. " +
                            "Use only one deployment method type per test class.", testClass.getName()));
        }
        if (testDeployment.isPresent()) {
            return testDeployment;
        }
        return deploymentProducer;
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

    private class ExtensionServerManagerListener implements ServerManagerListener, AutoCloseable {
        private final ExtensionContext context;
        private final ServerManager serverManager;

        private ExtensionServerManagerListener(final ExtensionContext context, final ServerManager serverManager) {
            this.context = context;
            this.serverManager = serverManager;
        }

        @Override
        public void afterStart(final ServerManager serverManager) {
            // Deploy any deployments and cache the deployment information
            deploy(serverManager, context);
        }

        @Override
        public void beforeShutdown(final ServerManager serverManager) {
            // Get deployment info
            final Optional<DeploymentInfo> deploymentInfo = DeploymentContext.resolveDeployment(context);
            if (deploymentInfo.isEmpty()) {
                return;
            }
            undeploy(context, serverManager, deploymentInfo.get());
        }

        @Override
        public void close() {
            getClassStore(context).remove(SERVER_LISTENER_KEY, ServerManagerListener.class);
            serverManager.removeServerManagerListener(this);
        }
    }
}
