/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.testing.junit.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.net.URI;

/**
 * Qualifies the injection point for a {@link URI} to indicate which server in a domain is being targeted.
 *
 * <pre>
 * &#x40;WildlyTest
 * public class OrderTest {
 *     &#x40;ServerResource
 *     &#x40;DomainServer("server-one")
 *     &#x40;RequestPath("orders")
 *     private URI uri;
 *
 *     &#x40;Test
 *     public void listOrders() throws Exception {
 *         try (Response response = Client.newClient().target(uri).request().get()) {
 *             Assertions.assertEquals(200, response.getStatus(),
 *                     () -> String.format("Failed to get orders: %s", response.readEntity(String.class)));
 *         }
 *     }
 * }
 * </pre>
 *
 * @author <a href="mailto:jperkins@ibm.com">James R. Perkins</a>
 */
@Inherited
@Documented
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface DomainServer {

    /**
     * The name of the domain server
     *
     * @return the name of the domain server
     */
    String value();
}
