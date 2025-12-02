/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.testing.extension.extension.deployment;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wildfly.testing.junit.extension.annotations.DeploymentProducer;
import org.wildfly.testing.junit.extension.annotations.Domain;
import org.wildfly.testing.junit.extension.annotations.DomainServer;
import org.wildfly.testing.junit.extension.annotations.RequestPath;
import org.wildfly.testing.junit.extension.annotations.ServerResource;
import org.wildfly.testing.junit.extension.annotations.WildFlyTest;

/**
 *
 * @author <a href="mailto:jperkins@ibm.com">James R. Perkins</a>
 */
@WildFlyTest
@Domain("main-server-group")
public class DomainDeploymentIT {

    @DeploymentProducer
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addClasses(TestServlet.class);
    }

    @ServerResource
    @DomainServer("server-one")
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
        Assertions.assertTrue(response.body().startsWith("Test"));
    }
}
