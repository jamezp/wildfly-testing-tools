/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.testing.junit.deployment;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wildfly.testing.junit.annotations.DeploymentProducer;
import org.wildfly.testing.junit.annotations.RequestPath;
import org.wildfly.testing.junit.annotations.ServerResource;
import org.wildfly.testing.junit.annotations.WildFlyTest;

/**
 *
 * @author <a href="mailto:jperkins@ibm.com">James R. Perkins</a>
 */
@WildFlyTest
public class StandaloneDeploymentIT {

    @DeploymentProducer
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addClasses(TestServlet.class);
    }

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
        Assertions.assertTrue(response.body().startsWith("Test"));
    }

    @WebServlet("test")
    public static class TestServlet extends HttpServlet {
        @Override
        protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
            resp.setContentType("text/plain");
            resp.setCharacterEncoding("UTF-8");
            resp.getWriter().println("Test");
            resp.getWriter().flush();
        }
    }
}
