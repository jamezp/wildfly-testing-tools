/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.testing.extension.extension.deployment;

import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.wildfly.testing.junit.extension.annotations.GenerateDeployment;

/**
 * Tests {@link GenerateDeployment} with inferred JAR type from {@link JavaArchive} parameter.
 *
 * @author <a href="mailto:jperkins@ibm.com">James R. Perkins</a>
 */
public class InferJarDeploymentIT extends AbstractJarDeploymentTest {

    @GenerateDeployment // Infers JAR from JavaArchive parameter
    public static void deployment(final JavaArchive jar) {
        jar.addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }
}
