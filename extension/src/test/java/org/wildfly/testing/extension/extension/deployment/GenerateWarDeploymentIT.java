/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.testing.extension.extension.deployment;

import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.wildfly.testing.junit.extension.annotations.GenerateDeployment;

/**
 * Tests {@link GenerateDeployment} with inferred WAR type.
 *
 * @author <a href="mailto:jperkins@ibm.com">James R. Perkins</a>
 */
public class GenerateWarDeploymentIT extends AbstractWarDeploymentTest {

    @GenerateDeployment // Infers WAR from WebArchive parameter
    public static void deployment(final WebArchive war) {
        war.addClasses(TestServlet.class);
    }
}
