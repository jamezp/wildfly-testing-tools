/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.testing.extension.extension.resources;

import java.net.URI;

import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.client.helpers.domain.DomainClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.wildfly.plugin.tools.DeploymentManager;
import org.wildfly.plugin.tools.server.ServerManager;
import org.wildfly.testing.junit.extension.annotation.RequestPath;
import org.wildfly.testing.junit.extension.annotation.ServerResource;
import org.wildfly.testing.junit.extension.annotation.WildFlyDomainTest;

/**
 *
 * @author <a href="mailto:jperkins@ibm.com">James R. Perkins</a>
 */
@WildFlyDomainTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DomainPerClassServerResourceInjectionIT extends ServerResourceInjection {

    @ServerResource
    private static DomainClient staticDomainClient;

    @ServerResource
    private DomainClient domainClient;

    @BeforeAll
    void clientBeforeAll(@ServerResource final ModelControllerClient client) {
        Assertions.assertNotNull(client, "The ModelControllerClient should not be null");
    }

    @BeforeAll
    void domainClientBeforeAll(@ServerResource final DomainClient client) {
        Assertions.assertNotNull(client, "The DomainClient should not be null");
    }

    @BeforeAll
    void baseUriBeforeAll(@ServerResource final URI uri) {
        Assertions.assertNotNull(uri, "The URI should not be null");
        Assertions.assertEquals(URI.create("http://localhost:8080"), uri);
    }

    @BeforeAll
    void testUriBeforeAll(@ServerResource @RequestPath("test") final URI uri) {
        Assertions.assertNotNull(uri, "The URI should not be null");
        Assertions.assertEquals(URI.create("http://localhost:8080/test"), uri);
    }

    @BeforeAll
    void deploymentManagerBeforeAll(@ServerResource final DeploymentManager deploymentManager) {
        Assertions.assertNotNull(deploymentManager, "The DeploymentManager should not be null");
    }

    @BeforeAll
    void serverManagerBeforeAll(@ServerResource final ServerManager serverManager) {
        Assertions.assertNotNull(serverManager, "The ServerManager should not be null");
    }

    @Test
    void domainStaticClient() {
        Assertions.assertNotNull(staticDomainClient, "The static DomainClient should not be null");
    }

    @Test
    void domainClient() {
        Assertions.assertNotNull(domainClient, "The DomainClient should not be null");
    }

    @Test
    void domainClientParameter(@ServerResource final DomainClient client) {
        Assertions.assertNotNull(client, "The DomainClient parameter should not be null");
    }
}
