/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.testing.extension.extension.deployment;

import java.net.URI;

import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.jupiter.api.TestInfo;
import org.wildfly.testing.junit.extension.annotation.GenerateDeployment;
import org.wildfly.testing.junit.extension.annotation.RequestPath;
import org.wildfly.testing.junit.extension.annotation.ServerResource;
import org.wildfly.testing.tools.deployment.Deployments;

/**
 * Tests {@link GenerateDeployment} with inferred EAR type from {@link EnterpriseArchive} parameter.
 *
 * @author <a href="mailto:jperkins@ibm.com">James R. Perkins</a>
 */
public class InferEarDeploymentIT extends AbstractEarDeploymentTest {

    @ServerResource
    @RequestPath("/test")
    private URI uri;

    @GenerateDeployment // Infers EAR from EnterpriseArchive parameter
    public static void deployment(final EnterpriseArchive ear, final TestInfo testInfo) {
        // Use Deployments utility to create nested archives
        ear.addAsModule(Deployments
                .war(testInfo.getTestClass().orElseThrow(() -> new AssertionError("Expected test class to be present")))
                .addClasses(TestServlet.class));
    }

    @Override
    protected URI uri() {
        return uri;
    }
}
