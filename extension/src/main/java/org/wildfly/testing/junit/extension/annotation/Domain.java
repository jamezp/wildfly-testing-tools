/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.testing.junit.extension.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.Tag;

/**
 * Indicates this should be a domain server.
 *
 * @author <a href="mailto:jperkins@ibm.com">James R. Perkins</a>
 */
@Inherited
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Tag("domain-test")
public @interface Domain {

    /**
     * An array of server groups in a domain which will be used for deployments if any are specified.
     *
     * @return an array of server groups
     */
    String[] value() default {};
}
