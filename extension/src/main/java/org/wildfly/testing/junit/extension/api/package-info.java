/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

/**
 * Service Provider Interfaces (SPIs) for extending the WildFly JUnit Extension with custom functionality.
 * <p>
 * This package contains interfaces that allow you to customize server configuration and extend resource
 * injection capabilities. All SPIs use the Java {@link java.util.ServiceLoader} mechanism for discovery.
 * </p>
 *
 * <h2>Configuration Factories</h2>
 * <p>
 * Customize how the WildFly server is configured for testing:
 * </p>
 * <ul>
 * <li>{@link org.wildfly.testing.junit.extension.api.StandaloneConfigurationFactory} - Customize standalone
 * server configuration</li>
 * <li>{@link org.wildfly.testing.junit.extension.api.DomainConfigurationFactory} - Customize domain mode
 * configuration</li>
 * </ul>
 *
 * <h3>Example: Custom Standalone Configuration</h3>
 *
 * <pre>
 * {
 *     &#64;code
 *     &#64;MetaInfServices
 *     public class CustomConfigFactory implements StandaloneConfigurationFactory {
 *         @Override
 *         public StandaloneConfiguration.Builder apply(StandaloneConfiguration.Builder builder,
 *                 ExtensionContext context) {
 *             return builder.withTimeout(Duration.ofMinutes(5))
 *                     .withArgument("-Djboss.bind.address", "0.0.0.0");
 *         }
 *     }
 * }
 * </pre>
 *
 * @author <a href="mailto:jperkins@ibm.com">James R. Perkins</a>
 *
 * @see org.wildfly.testing.junit.extension.api.StandaloneConfigurationFactory
 * @see org.wildfly.testing.junit.extension.api.DomainConfigurationFactory
 */
package org.wildfly.testing.junit.extension.api;