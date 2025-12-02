/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.testing.extension.extension.manual;

import org.wildfly.testing.junit.extension.annotation.Domain;

/**
 *
 * @author <a href="mailto:jperkins@ibm.com">James R. Perkins</a>
 */
@Domain
public class DomainBeforeEachIT extends AbstractBeforeEach {
    @Override
    public String expectedLaunchType() {
        return "DOMAIN";
    }
}
