/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.testing.junit.deployment;

import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.wildfly.testing.junit.annotations.GenerateDeployment;

/**
 * Tests {@link GenerateDeployment} with explicit JAR type.
 *
 * @author <a href="mailto:jperkins@ibm.com">James R. Perkins</a>
 */
public class GenerateJarDeploymentIT extends AbstractJarDeploymentTest {

    @GenerateDeployment(GenerateDeployment.DeploymentType.JAR)
    public static void deployment(final JavaArchive jar) {
        jar.addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }
}
