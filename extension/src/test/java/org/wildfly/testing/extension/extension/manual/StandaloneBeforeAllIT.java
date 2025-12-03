/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.testing.extension.extension.manual;

import java.util.concurrent.TimeUnit;

import org.jboss.as.controller.client.helpers.Operations;
import org.jboss.dmr.ModelNode;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.wildfly.plugin.tools.server.ServerManager;
import org.wildfly.testing.junit.extension.annotation.ServerResource;

/**
 *
 * @author <a href="mailto:jperkins@ibm.com">James R. Perkins</a>
 */
public class StandaloneBeforeAllIT extends AbstractBeforeAll {
    @ServerResource
    private static ServerManager serverManager;
    private static String restorePath;

    @BeforeAll
    static void snapshot() throws Exception {
        restorePath = serverManager.takeSnapshot();
    }

    @AfterAll
    public static void restore() throws Exception {
        if (restorePath != null) {
            final ModelNode op = Operations.createOperation("reload");
            op.get("server-config").set(restorePath);
            serverManager.executeReload(op);
            serverManager.waitFor(30L, TimeUnit.SECONDS);
            serverManager.executeOperation(Operations.createOperation("write-config"));
        }
    }

    @Override
    public String expectedLaunchType() {
        return "STANDALONE";
    }
}
