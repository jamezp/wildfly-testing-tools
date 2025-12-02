/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.testing.junit;

import org.junit.jupiter.api.Test;
import org.wildfly.testing.junit.annotations.RequiresModule;

/**
 * @author <a href="mailto:jpekrins@ibm.com">James R. Perkins</a>
 */
@SuppressWarnings("NewClassNamingConvention")
@RequiresModule("org.wildfly.testing.junit.test.resource-root")
public class RequireResourceRoot {

    @Test
    public void passing() {
    }

    @Test
    @RequiresModule(value = "org.wildfly.testing.junit.test.resource-root", minVersion = "1.0.0")
    public void passingVersion() {
    }

    @Test
    @RequiresModule(value = "org.wildfly.testing.junit.test.resource-root", minVersion = "2.0.1")
    public void skippedVersion() {
    }

    @Test
    @RequiresModule(value = "org.wildfly.testing.junit.test.resource-root.invalid")
    public void skippedMissingModule() {
    }
}
