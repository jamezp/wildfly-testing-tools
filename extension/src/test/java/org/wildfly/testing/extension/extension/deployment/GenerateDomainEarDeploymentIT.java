/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.testing.extension.extension.deployment;

import java.net.URI;

import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.jupiter.api.TestInfo;
import org.wildfly.testing.junit.extension.annotations.Domain;
import org.wildfly.testing.junit.extension.annotations.DomainServer;
import org.wildfly.testing.junit.extension.annotations.GenerateDeployment;
import org.wildfly.testing.junit.extension.annotations.RequestPath;
import org.wildfly.testing.junit.extension.annotations.ServerResource;
import org.wildfly.testing.tools.deployment.Deployments;

/**
 * Tests {@link GenerateDeployment} with EAR type using {@link Deployments} utility.
 *
 * @author <a href="mailto:jperkins@ibm.com">James R. Perkins</a>
 */
@Domain("main-server-group")
public class GenerateDomainEarDeploymentIT extends AbstractEarDeploymentTest {

    @ServerResource
    @RequestPath("/test")
    @DomainServer("server-one")
    private URI uri;

    @GenerateDeployment(GenerateDeployment.DeploymentType.EAR)
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
