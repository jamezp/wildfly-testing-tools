/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.testing.extension.extension.deployment;

import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.wildfly.testing.junit.extension.annotation.GenerateDeployment;

/**
 * Tests {@link GenerateDeployment} with explicit WAR type (not inferred).
 *
 * @author <a href="mailto:jperkins@ibm.com">James R. Perkins</a>
 */
public class ExplicitWarDeploymentIT extends AbstractWarDeploymentTest {

    @GenerateDeployment(GenerateDeployment.DeploymentType.WAR)
    public static void deployment(final WebArchive war) {
        war.addClasses(TestServlet.class);
    }
}
