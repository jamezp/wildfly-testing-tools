/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.testing.junit.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ExtendWith;
import org.wildfly.testing.junit.extension.WildFlyExtension;

/**
 * An annotation which starts a WildFly server and manages a server.
 *
 * @author <a href="mailto:jperkins@ibm.com">James R. Perkins</a>
 */
@Inherited
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith({
        WildFlyExtension.class
})
public @interface WildFlyTest {
}
