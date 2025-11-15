/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.testing.junit.deployment;

import java.net.URI;

import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.jupiter.api.TestInfo;
import org.wildfly.testing.junit.annotations.GenerateDeployment;
import org.wildfly.testing.junit.annotations.RequestPath;
import org.wildfly.testing.junit.annotations.ServerResource;
import org.wildfly.testing.junit.api.Deployments;

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
        ear.addAsModule(Deployments.war(testInfo)
                .addClasses(TestServlet.class));
    }

    @Override
    protected URI uri() {
        return uri;
    }
}
