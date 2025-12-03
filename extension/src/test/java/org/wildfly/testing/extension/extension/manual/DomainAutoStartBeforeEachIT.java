/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.testing.extension.extension.manual;

import java.net.URI;

import org.wildfly.testing.junit.extension.annotation.Domain;
import org.wildfly.testing.junit.extension.annotation.DomainServer;
import org.wildfly.testing.junit.extension.annotation.RequestPath;
import org.wildfly.testing.junit.extension.annotation.ServerResource;

/**
 *
 * @author <a href="mailto:jperkins@ibm.com">James R. Perkins</a>
 */
@Domain("main-server-group")
public class DomainAutoStartBeforeEachIT extends AbstractAutoStartBeforeEach {

    @ServerResource
    @RequestPath("/test")
    @DomainServer("server-one")
    private URI uri;

    @Override
    public String expectedLaunchType() {
        return "DOMAIN";
    }

    @Override
    protected URI getUri() {
        return uri;
    }
}
