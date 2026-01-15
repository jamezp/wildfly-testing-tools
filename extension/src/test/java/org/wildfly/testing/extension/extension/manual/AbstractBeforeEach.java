/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.testing.extension.extension.manual;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.wildfly.plugin.tools.server.ServerManager;
import org.wildfly.testing.junit.extension.annotation.ManualMode;
import org.wildfly.testing.junit.extension.annotation.ServerResource;

/**
 *
 * @author <a href="mailto:jperkins@ibm.com">James R. Perkins</a>
 */
@ManualMode
abstract class AbstractBeforeEach implements ManualModeTest {

    @ServerResource
    private ServerManager serverManager;

    @BeforeEach
    void startServer() {
        serverManager.start();
    }

    @AfterEach
    void stopServer() throws Exception {
        serverManager.shutdown(60L);
    }

    @Test
    void shutdown() throws Exception {
        checkServer();
        serverManager.shutdown(60L);
        Assertions.assertFalse(serverManager.isRunning(), "Server should be shutdown");
        serverManager.start();
        checkServer();
    }

    @Test
    void checkServer() {
        Assertions.assertTrue(serverManager.isRunning(), "Server should be running");
        Assertions.assertEquals(expectedLaunchType(), serverManager.launchType());
    }

}
