/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

/**
 * Utilities for creating deployment descriptors and configuring deployments for WildFly testing.
 * <p>
 * This package provides helper methods to generate common WildFly deployment descriptors programmatically,
 * making it easier to configure test deployments with ShrinkWrap or other archive builders.
 * </p>
 *
 * <h2>Key Classes</h2>
 * <ul>
 * <li>{@link org.wildfly.testing.tools.deployment.DeploymentDescriptors} - Factory methods for creating
 * deployment descriptor XML files</li>
 * <li>{@link org.wildfly.testing.tools.deployment.Deployments} - Utility methods for working with
 * ShrinkWrap deployments</li>
 * </ul>
 *
 * <h2>Supported Deployment Descriptors</h2>
 * <ul>
 * <li>{@code jboss-deployment-structure.xml} - Control deployment dependencies and module exclusions</li>
 * <li>{@code jboss-web.xml} - Configure web deployment settings like context root and security domain</li>
 * <li>{@code permissions.xml} - Define Jakarta EE security permissions for the deployment</li>
 * </ul>
 *
 * <h2>Example: Adding Module Dependencies</h2>
 *
 * <pre>{@code
 * WebArchive war = ShrinkWrap.create(WebArchive.class, "test.war")
 *         .addClass(MyService.class);
 *
 * // Add jboss-deployment-structure.xml to include additional modules
 * Asset deploymentStructure = DeploymentDescriptors.createJBossDeploymentStructureAsset(
 *         Set.of("org.jboss.logging"), // modules to add
 *         Set.of() // modules to exclude
 * );
 * war.addAsManifestResource(deploymentStructure, "jboss-deployment-structure.xml");
 * }</pre>
 *
 * <h2>Example: Setting Context Root</h2>
 *
 * <pre>{@code
 * WebArchive war = ShrinkWrap.create(WebArchive.class, "myapp.war")
 *         .addClass(MyServlet.class);
 *
 * // Set custom context root
 * Asset jbossWeb = DeploymentDescriptors.createJBossWebContextRoot("/api");
 * war.addAsWebInfResource(jbossWeb, "jboss-web.xml");
 * }</pre>
 *
 * <h2>Example: Adding Security Permissions</h2>
 *
 * <pre>{@code
 * JavaArchive jar = ShrinkWrap.create(JavaArchive.class, "test.jar")
 *         .addClass(MyClass.class);
 *
 * // Add permissions for file system access
 * Collection<Permission> permissions = DeploymentDescriptors.createTempDirPermission("read,write");
 * Asset permissionsXml = DeploymentDescriptors.createPermissionsXmlAsset(permissions);
 * jar.addAsManifestResource(permissionsXml, "permissions.xml");
 * }</pre>
 *
 * <h2>Framework Compatibility</h2>
 * <p>
 * This package has no dependencies on JUnit or any specific testing framework. It can be used with:
 * </p>
 * <ul>
 * <li>WildFly JUnit Extension</li>
 * <li>Arquillian</li>
 * <li>TestNG</li>
 * <li>Any framework that uses ShrinkWrap for deployment creation</li>
 * </ul>
 *
 * @author <a href="mailto:jperkins@ibm.com">James R. Perkins</a>
 *
 * @see org.wildfly.testing.tools.deployment.DeploymentDescriptors
 * @see org.wildfly.testing.tools.deployment.Deployments
 */
package org.wildfly.testing.tools.deployment;