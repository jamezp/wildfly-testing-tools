/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

/**
 * Annotations for conditional test execution and resource injection in WildFly-based tests.
 * <p>
 * This package provides JUnit annotations that can be used with any testing framework to conditionally
 * execute tests based on WildFly server configuration and inject server-related information.
 * </p>
 *
 * <h2>Key Annotations</h2>
 * <ul>
 * <li>{@link org.wildfly.testing.junit.annotation.RequiresModule @RequiresModule} - Skip tests when specific
 * WildFly modules are not available</li>
 * <li>{@link org.wildfly.testing.junit.annotation.AnyOf @AnyOf} - Run tests if any of multiple module
 * requirements are met</li>
 * <li>{@link org.wildfly.testing.junit.annotation.JBossHome @JBossHome} - Inject the WildFly installation
 * directory path</li>
 * </ul>
 *
 * <h2>Usage with WildFly JUnit Extension</h2>
 * <p>
 * These annotations work seamlessly with the {@code wildfly-junit-extension} module for full server lifecycle
 * management:
 * </p>
 *
 * <pre>{@code
 * @WildFlyTest
 * @RequiresModule("org.jboss.as.ejb3")
 * public class EjbTest {
 *     // Test only runs if EJB module is available
 * }
 * }</pre>
 *
 * <h2>Usage with Other Frameworks</h2>
 * <p>
 * These annotations can also be used independently with Arquillian or other testing frameworks that
 * manage the WildFly server lifecycle. Simply add the {@code wildfly-junit-api} dependency and the
 * annotations will work as JUnit extensions.
 * </p>
 *
 * @author <a href="mailto:jperkins@ibm.com">James R. Perkins</a>
 *
 * @see org.wildfly.testing.junit.annotation.RequiresModule
 * @see org.wildfly.testing.junit.annotation.AnyOf
 * @see org.wildfly.testing.junit.annotation.JBossHome
 */
package org.wildfly.testing.junit.annotation;