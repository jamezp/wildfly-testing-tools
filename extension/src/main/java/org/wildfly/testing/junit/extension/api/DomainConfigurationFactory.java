/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.testing.junit.extension.api;

import java.util.Optional;
import java.util.ServiceLoader;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.wildfly.plugin.tools.server.DomainConfiguration;

/**
 * Factory for creating {@link DomainConfiguration} instances.
 * <p>
 * Configuration is resolved in the following order (highest priority first):
 * <ol>
 * <li>ServiceLoader - custom implementations via META-INF/services</li>
 * <li>JUnit Platform configuration properties (junit-platform.properties)</li>
 * <li>System properties</li>
 * <li>Environment variables</li>
 * </ol>
 *
 * <p>
 * Supported JUnit Platform configuration properties:
 * <ul>
 * <li>{@code jboss.home} - Path to WildFly installation</li>
 * <li>{@code wildfly.java.home} - Java home to use for the server</li>
 * <li>{@code wildfly.module.path} - Module path for the server</li>
 * </ul>
 *
 * @author <a href="mailto:jperkins@ibm.com">James R. Perkins</a>
 */
public interface DomainConfigurationFactory {

    /**
     * Creates a domain configuration.
     *
     * @param context the extension context for accessing JUnit configuration
     *
     * @return the domain configuration
     */
    DomainConfiguration configuration(ExtensionContext context);

    /**
     * Creates a factory instance using the hybrid configuration approach.
     *
     * @return a configuration factory
     */
    static DomainConfigurationFactory create() {
        // Try ServiceLoader first for custom implementations
        final ServiceLoader<DomainConfigurationFactory> loader = ServiceLoader.load(DomainConfigurationFactory.class);
        final Optional<DomainConfigurationFactory> factory = loader.findFirst();
        return factory.orElseGet(DefaultDomainConfigurationFactory::new);
    }

}
