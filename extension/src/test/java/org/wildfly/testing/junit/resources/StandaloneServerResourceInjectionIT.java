/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.testing.junit.resources;

import java.net.URI;

import org.jboss.as.controller.client.ModelControllerClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.wildfly.plugin.tools.DeploymentManager;
import org.wildfly.plugin.tools.server.ServerManager;
import org.wildfly.testing.junit.annotations.RequestPath;
import org.wildfly.testing.junit.annotations.ServerResource;
import org.wildfly.testing.junit.annotations.WildFlyTest;

/**
 *
 * @author <a href="mailto:jperkins@ibm.com">James R. Perkins</a>
 */
@WildFlyTest
class StandaloneServerResourceInjectionIT extends ServerResourceInjection {

    @BeforeAll
    static void clientBeforeAll(@ServerResource final ModelControllerClient client) {
        Assertions.assertNotNull(client, "The ModelControllerClient should not be null");
    }

    @BeforeAll
    static void baseUriBeforeAll(@ServerResource final URI uri) {
        Assertions.assertNotNull(uri, "The URI should not be null");
        Assertions.assertEquals(URI.create("http://localhost:8080"), uri);
    }

    @BeforeAll
    static void testUriBeforeAll(@ServerResource @RequestPath("test") final URI uri) {
        Assertions.assertNotNull(uri, "The URI should not be null");
        Assertions.assertEquals(URI.create("http://localhost:8080/test"), uri);
    }

    @BeforeAll
    static void deploymentManagerBeforeAll(@ServerResource final DeploymentManager deploymentManager) {
        Assertions.assertNotNull(deploymentManager, "The DeploymentManager should not be null");
    }

    @BeforeAll
    static void serverManagerBeforeAll(@ServerResource final ServerManager serverManager) {
        Assertions.assertNotNull(serverManager, "The ServerManager should not be null");
    }
}
