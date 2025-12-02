/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.testing.junit.condition;

import java.nio.file.Path;
import java.util.Optional;

import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Support for resolving information about the local server installation.
 *
 * @author <a href="mailto:jperkins@ibm.com">James R. Perkins</a>
 */
public class Support {

    /**
     * Resolves the servers base directory. This first uses the
     * {@link ExtensionContext#getConfigurationParameter(String)} with the {@code jboss.home} key. If not found, it
     * checks for the environment variable {@code JBOSS_HOME}. If none of those are set, a
     * {@link org.junit.platform.commons.JUnitException} is thrown.
     *
     * @param context the extension context used to look up the property
     *
     * @return the path to the server
     */
    static Optional<Path> resolveJBossHome(final ExtensionContext context) {
        return context.getConfigurationParameter("jboss.home")
                .or(() -> Optional.ofNullable(System.getenv("JBOSS_HOME")))
                .or(() -> context.getConfigurationParameter("jboss.home.dir"))
                .map(Path::of);
    }

    /**
     * Resolves the {@code wildfly.module.path} property from the context.
     *
     * @param context the extension context
     *
     * @return the value of the {@code wildfly.module.path}, otherwise empty
     */
    static Optional<String> resolveModulePath(final ExtensionContext context) {
        return context.getConfigurationParameter("wildfly.module.path");
    }
}
