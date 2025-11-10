/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.testing.junit.manual;

import java.net.URI;

import org.wildfly.testing.junit.annotations.RequestPath;
import org.wildfly.testing.junit.annotations.ServerResource;

/**
 *
 * @author <a href="mailto:jperkins@ibm.com">James R. Perkins</a>
 */
public class StandaloneAutoStartBeforeEachIT extends AbstractAutoStartBeforeEach {

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
