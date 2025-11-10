/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.testing.junit.extension;

import java.util.Optional;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.util.AnnotationUtils;
import org.wildfly.testing.junit.annotations.ManualMode;

/**
 * Support class for the extension.
 *
 * @author <a href="mailto:jperkins@ibm.com">James R. Perkins</a>
 */
class TestSupport {

    /**
     * Checks if this is a manual mode test.
     *
     * @param context the context to check, which requires a test class be present
     *
     * @return {@code true} if this is a manual mode test, otherwise {@code false}
     */
    static boolean isManualMode(final ExtensionContext context) {
        return context.getRequiredTestClass().isAnnotationPresent(ManualMode.class);
    }

    /**
     * Returns the {@link ManualMode} annotation if present.
     *
     * @param context the context to check, which requires a test class be present
     *
     * @return the optional annotation
     */
    static Optional<ManualMode> getManualMode(final ExtensionContext context) {
        return AnnotationUtils.findAnnotation(context.getRequiredTestClass(), ManualMode.class);
    }
}
