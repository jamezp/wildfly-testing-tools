/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.testing.extension.extension.deployment;

import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestInfo;
import org.wildfly.testing.junit.extension.annotation.GenerateDeployment;

/**
 * Tests {@link GenerateDeployment} with {@link TestInfo} parameter.
 *
 * @author <a href="mailto:jperkins@ibm.com">James R. Perkins</a>
 */
@Tag("cdi")
public class GenerateDeploymentWithTestInfoIT extends AbstractWarDeploymentTest {

    @GenerateDeployment
    public static void deployment(final WebArchive war, final TestInfo testInfo) {
        // Add beans.xml if the test is tagged with "cdi"
        if (testInfo.getTags().contains("cdi")) {
            war.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        }
        war.addClasses(TestServlet.class);
    }
}
