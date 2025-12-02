/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.testing.extension.extension.manual;

import java.net.URI;

import org.wildfly.testing.junit.extension.annotations.Domain;
import org.wildfly.testing.junit.extension.annotations.DomainServer;
import org.wildfly.testing.junit.extension.annotations.RequestPath;
import org.wildfly.testing.junit.extension.annotations.ServerResource;

/**
 *
 * @author <a href="mailto:jperkins@ibm.com">James R. Perkins</a>
 */
@Domain("main-server-group")
public class DomainAutoStartTestIT extends AbstractAutoStartTest {

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
