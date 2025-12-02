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
@RequiresModule("org.wildfly.testing.junit.test.snapshot")
public class RequireSnapshot {

    @Test
    @RequiresModule(value = "org.wildfly.testing.junit.test.snapshot", minVersion = "1.0.0.Beta2")
    public void passingVersion() {
    }

    @Test
    @RequiresModule(value = "org.wildfly.testing.junit.test.snapshot", minVersion = "1.0.0.Beta3")
    public void skippedVersion() {
    }
}
