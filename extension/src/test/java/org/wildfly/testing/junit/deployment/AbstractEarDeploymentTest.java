/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.testing.junit.deployment;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wildfly.plugin.tools.server.ServerManager;
import org.wildfly.testing.junit.annotations.GenerateDeployment;
import org.wildfly.testing.junit.annotations.ServerResource;
import org.wildfly.testing.junit.annotations.WildFlyTest;
import org.wildfly.testing.junit.api.Deployments;

/**
 * Tests {@link GenerateDeployment} with EAR type using {@link Deployments} utility.
 *
 * @author <a href="mailto:jperkins@ibm.com">James R. Perkins</a>
 */
@WildFlyTest
abstract class AbstractEarDeploymentTest {

    @ServerResource
    private ServerManager serverManager;

    @Test
    public void serverRunningWithEarDeployment() {
        Assertions.assertTrue(serverManager.isRunning(), "Server should be running with EAR deployment");
    }

    @Test
    public void validateUri() {
        final URI uri = uri();
        Assertions.assertNotNull(uri);
        Assertions.assertTrue(uri.toString().endsWith("/test"),
                () -> String.format("Expected URI to contain the request path /test at the end: %s", uri));
    }

    @Test
    public void checkResponse() throws Exception {
        final HttpClient client = HttpClient.newHttpClient();
        final HttpRequest request = HttpRequest.newBuilder(uri())
                .GET()
                .build();
        final HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(200, response.statusCode(),
                () -> String.format("Expected HTTP status code %d: %s", response.statusCode(), response.body()));
        Assertions.assertTrue(response.body().startsWith("Test"));
    }

    protected abstract URI uri();
}
