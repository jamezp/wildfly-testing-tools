/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.testing.junit.manual;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.wildfly.plugin.tools.server.ServerManager;
import org.wildfly.testing.junit.annotations.ManualMode;
import org.wildfly.testing.junit.annotations.ServerResource;
import org.wildfly.testing.junit.annotations.WildFlyTest;

/**
 *
 * @author <a href="mailto:jperkins@ibm.com">James R. Perkins</a>
 */
@WildFlyTest
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
