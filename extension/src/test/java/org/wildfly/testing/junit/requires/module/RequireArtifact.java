/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.testing.junit.requires.module;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wildfly.testing.junit.annotations.RequiresModule;

/**
 * @author <a href="mailto:jpekrins@ibm.com">James R. Perkins</a>
 */
@SuppressWarnings("NewClassNamingConvention")
@RequiresModule("org.wildfly.testing.junit.test.artifact")
public class RequireArtifact {

    @Test
    public void passing() {
    }

    @Test
    @RequiresModule(value = "org.wildfly.testing.junit.test.artifact", minVersion = "2.0.0")
    public void skippedVersion() {
    }

    @Test
    @RequiresModule(value = "org.wildfly.testing.junit.test.artifact.invalid")
    public void skippedMissingModule() {
    }

    @Test
    @RequiresModule(value = "org.wildfly.testing.junit.test.client", minVersion = "3.0.0.Final")
    public void client() {
    }

    @Test
    @RequiresModule(value = "org.wildfly.testing.junit.test.client-api", minVersion = "1.0.0.Final")
    public void clientApi() {
    }

    @Test
    @RequiresModule(value = "org.wildfly.testing.junit.test.client-api", slot = "test", minVersion = "1.1.0.Beta1")
    public void clientApiTest() {
    }

    @Test
    @RequiresModule(value = "org.wildfly.testing.junit.test.client-spi", minVersion = "1.0.0.Final")
    public void clientSpi() {
        Assertions.fail("Should have skipped");
    }
}
