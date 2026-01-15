/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.testing.extension.extension.manual;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.wildfly.plugin.tools.server.ServerManager;
import org.wildfly.testing.junit.extension.annotation.ManualMode;
import org.wildfly.testing.junit.extension.annotation.ServerResource;

/**
 *
 * @author <a href="mailto:jperkins@ibm.com">James R. Perkins</a>
 */
@ManualMode
abstract class AbstractBeforeAll implements ManualModeTest {

    @ServerResource
    private static ServerManager serverManager;

    @BeforeAll
    static void startServer() {
        serverManager.start();
    }

    @Test
    void checkServer() {
        Assertions.assertTrue(serverManager.isRunning(), "Server should be running");
        Assertions.assertEquals(expectedLaunchType(), serverManager.launchType());
    }

}
