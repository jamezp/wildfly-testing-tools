/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.testing.junit.deployment;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wildfly.plugin.tools.server.ServerManager;
import org.wildfly.testing.junit.annotations.ServerResource;
import org.wildfly.testing.junit.annotations.WildFlyTest;

/**
 * Abstract base class for RAR deployment tests. Subclasses only need to provide the deployment method.
 *
 * @author <a href="mailto:jperkins@ibm.com">James R. Perkins</a>
 */
@WildFlyTest
abstract class AbstractRarDeploymentTest {

    @ServerResource
    private ServerManager serverManager;

    @Test
    public void rarIsDeployed() throws Exception {
        Assertions.assertTrue(serverManager.isRunning(), "Server should be running with RAR deployment");
        final var deployments = serverManager.deploymentManager().getDeployments();
        boolean found = false;
        for (final var deployment : deployments) {
            if ((getClass().getSimpleName() + ".rar").equals(deployment.getName())) {
                found = true;
                break;
            }
        }
        Assertions.assertTrue(found, () -> String.format("Expected deployment '%s.rar' to exist in %s",
                getClass().getSimpleName(), deployments));
    }
}
