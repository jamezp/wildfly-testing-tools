/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.testing.extension.extension.manual;

import java.net.URI;

import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.wildfly.testing.junit.extension.annotation.GenerateDeployment;
import org.wildfly.testing.junit.extension.annotation.RequestPath;
import org.wildfly.testing.junit.extension.annotation.ServerResource;
import org.wildfly.testing.junit.extension.annotation.WildFlyTest;

/**
 *
 * @author <a href="mailto:jperkins@ibm.com">James R. Perkins</a>
 */
@WildFlyTest
public class StandaloneAutoStartTestIT extends AbstractAutoStartTest {

    @GenerateDeployment
    public static void createDeployment(final WebArchive war) {
        createDefaultDeployment(war);
    }

    @ServerResource
    @RequestPath("/test")
    private URI uri;

    @Override
    public String expectedLaunchType() {
        return "STANDALONE";
    }

    @Override
    protected URI getUri() {
        return uri;
    }
}
