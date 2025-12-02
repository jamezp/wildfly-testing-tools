/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.testing.extension.extension.manual;

import java.net.URI;

import org.wildfly.testing.junit.extension.annotations.RequestPath;
import org.wildfly.testing.junit.extension.annotations.ServerResource;

/**
 *
 * @author <a href="mailto:jperkins@ibm.com">James R. Perkins</a>
 */
public class StandaloneAutoStartTestIT extends AbstractAutoStartTest {

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
