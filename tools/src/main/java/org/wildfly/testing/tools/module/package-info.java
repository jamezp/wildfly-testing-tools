/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

/**
 * Utilities for creating and managing WildFly modules programmatically during testing.
 * <p>
 * This package provides a fluent API for building custom WildFly modules, adding module dependencies,
 * and managing module lifecycle. This is useful when tests need to provision custom modules that aren't
 * part of the standard WildFly distribution.
 * </p>
 *
 * <h2>Key Classes</h2>
 * <ul>
 * <li>{@link org.wildfly.testing.tools.module.ModuleBuilder} - Fluent builder for creating WildFly modules</li>
 * <li>{@link org.wildfly.testing.tools.module.ModuleDescription} - Represents a built module with lifecycle
 * management</li>
 * <li>{@link org.wildfly.testing.tools.module.ModuleDependency} - Represents a module dependency with optional
 * export and services configuration</li>
 * <li>{@link org.wildfly.testing.tools.module.Modules} - Utility methods for working with WildFly module paths</li>
 * </ul>
 *
 * <h2>Example: Creating a Simple Module</h2>
 *
 * <pre>{@code
 * // Create a module with JAR resources
 * ModuleDescription module = ModuleBuilder.of("com.example.mymodule")
 *         .addResourcePath("/path/to/mylib.jar")
 *         .addDependency("org.jboss.logging")
 *         .build(Path.of(System.getProperty("jboss.home"), "modules"));
 *
 * // Module is automatically written to disk
 * // Clean up when done (usually in @AfterAll)
 * module.close();
 * }</pre>
 *
 * <h2>Example: Module with Dependencies and Exports</h2>
 *
 * <pre>{@code
 * ModuleDescription module = ModuleBuilder.of("com.example.api", "1.0")
 *         .addResource(ShrinkWrap.create(JavaArchive.class)
 *                 .addClass(MyApi.class))
 *         .addDependency(ModuleDependency.of("javax.api")
 *                 .setExport(true)
 *                 .setServices(ModuleDependency.ServiceType.IMPORT))
 *         .addDependency("org.jboss.logging")
 *         .build(modulePath);
 *
 * // Use in tests, then clean up
 * module.close();
 * }</pre>
 *
 * <h2>Example: Creating META-INF/services Files</h2>
 *
 * <pre>{@code
 * // Automatically generate META-INF/services files in the module
 * ModuleDescription module = ModuleBuilder.of("com.example.plugin")
 *         .addResource(archive)
 *         .addMetaInfServices(PluginInterface.class,
 *                 PluginImpl1.class.getName(),
 *                 PluginImpl2.class.getName())
 *         .build(modulePath);
 * }</pre>
 *
 * <h2>Lifecycle Management</h2>
 * <p>
 * {@link org.wildfly.testing.tools.module.ModuleDescription} implements {@link java.lang.AutoCloseable},
 * making it easy to clean up modules after tests:
 * </p>
 *
 * <pre>{@code
 * try (ModuleDescription module = ModuleBuilder.of("com.example.test").build(modulePath)) {
 *     // Run tests that use the module
 * } // Module directory is automatically deleted
 * }</pre>
 *
 * <h2>Module Paths and Immutability</h2>
 * <p>
 * The {@link org.wildfly.testing.tools.module.Modules} utility helps manage module paths and can enforce
 * immutability to prevent accidental modification of WildFly's core modules. Use the system property
 * {@code org.wildfly.testing.tools.modules.immutable.paths} to mark paths as read-only.
 * </p>
 *
 * <h2>Framework Compatibility</h2>
 * <p>
 * This package has no dependencies on JUnit or any specific testing framework. It can be used with any
 * Java-based testing framework that needs to provision custom WildFly modules.
 * </p>
 *
 * @author <a href="mailto:jperkins@ibm.com">James R. Perkins</a>
 *
 * @see org.wildfly.testing.tools.module.ModuleBuilder
 * @see org.wildfly.testing.tools.module.ModuleDescription
 * @see org.wildfly.testing.tools.module.ModuleDependency
 * @see org.wildfly.testing.tools.module.Modules
 */
package org.wildfly.testing.tools.module;