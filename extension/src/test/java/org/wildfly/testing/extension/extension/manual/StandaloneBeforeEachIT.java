/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.testing.extension.extension.manual;

/**
 *
 * @author <a href="mailto:jperkins@ibm.com">James R. Perkins</a>
 */
public class StandaloneBeforeEachIT extends AbstractBeforeEach {
    @Override
    public String expectedLaunchType() {
        return "STANDALONE";
    }
}
