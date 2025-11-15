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
import org.wildfly.testing.junit.annotations.RequestPath;
import org.wildfly.testing.junit.annotations.ServerResource;
import org.wildfly.testing.junit.annotations.WildFlyTest;

/**
 * Abstract base class for WAR deployment tests. Subclasses only need to provide the deployment method.
 *
 * @author <a href="mailto:jperkins@ibm.com">James R. Perkins</a>
 */
@WildFlyTest
abstract class AbstractWarDeploymentTest {

    @ServerResource
    @RequestPath("/test")
    private URI uri;

    @Test
    public void validateUri() {
        Assertions.assertNotNull(uri);
        Assertions.assertTrue(uri.toString().endsWith("/test"),
                () -> String.format("Expected URI to contain the request path /test at the end: %s", uri));
    }

    @Test
    public void checkResponse() throws Exception {
        final HttpClient client = HttpClient.newHttpClient();
        final HttpRequest request = HttpRequest.newBuilder(uri)
                .GET()
                .build();
        final HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(200, response.statusCode(),
                () -> String.format("Expected HTTP status code %d: %s", response.statusCode(), response.body()));
        Assertions.assertTrue(response.body().startsWith("Test"),
                () -> String.format("Expected response to start with 'Test', but was: %s", response.body()));
    }
}
