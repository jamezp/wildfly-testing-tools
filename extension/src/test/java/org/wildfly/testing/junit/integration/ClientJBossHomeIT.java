/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.testing.junit.integration;

import java.io.File;
import java.nio.file.Path;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wildfly.testing.junit.annotations.JBossHome;
import org.wildfly.testing.junit.annotations.WildFlyTest;

/**
 * @author <a href="mailto:jpekrins@ibm.com">James R. Perkins</a>
 */
@WildFlyTest
public class ClientJBossHomeIT {

    @Test
    public void stringSet(@JBossHome final String value) {
        Assertions.assertNotNull(value, "Expected the JBossHome to be set to a java.lang.String");
    }

    @Test
    public void pathSet(@JBossHome final Path value) {
        Assertions.assertNotNull(value, "Expected the JBossHome to be set to a java.nio.file.Path");
    }

    @Test
    public void fileSet(@JBossHome final File value) {
        Assertions.assertNotNull(value, "Expected the JBossHome to be set to a java.io.File");
    }
}
