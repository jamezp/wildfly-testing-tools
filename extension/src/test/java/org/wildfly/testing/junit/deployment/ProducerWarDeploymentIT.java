/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.testing.junit.deployment;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.wildfly.testing.junit.annotations.DeploymentProducer;

/**
 * Tests {@link DeploymentProducer} creating a WAR - same test logic as {@link GenerateWarDeploymentIT}.
 *
 * @author <a href="mailto:jperkins@ibm.com">James R. Perkins</a>
 */
public class ProducerWarDeploymentIT extends AbstractWarDeploymentTest {

    @DeploymentProducer
    public static WebArchive deployment() {
        return ShrinkWrap.create(WebArchive.class, "test.war")
                .addClasses(TestServlet.class);
    }
}
