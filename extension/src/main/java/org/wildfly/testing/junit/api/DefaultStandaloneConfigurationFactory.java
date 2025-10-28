/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.testing.junit.api;

import java.nio.file.Path;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.JUnitException;
import org.wildfly.core.launcher.StandaloneCommandBuilder;
import org.wildfly.plugin.tools.server.Configuration;
import org.wildfly.plugin.tools.server.StandaloneConfiguration;

/**
 * The default implementation for the {@link StandaloneConfigurationFactory}.
 *
 * @author <a href="mailto:jperkins@ibm.com">James R. Perkins</a>
 */
class DefaultStandaloneConfigurationFactory implements StandaloneConfigurationFactory {

    @Override
    public StandaloneConfiguration configuration(final ExtensionContext context) {
        final Path jbossHome = ServerConfiguration.resolveJBossHome(context).orElseThrow(() -> new JUnitException(
                "Server home not configured. Set jboss.home in junit-platform.properties, " +
                        "jboss.home system property, or JBOSS_HOME environment variable."));

        final StandaloneCommandBuilder commandBuilder = StandaloneCommandBuilder.of(jbossHome);

        // Configure optional properties
        ServerConfiguration.resolveJavaHome(context).ifPresent(commandBuilder::setJavaHome);

        ServerConfiguration.resolveModulePath(context).ifPresent(modulePath -> commandBuilder.setModuleDirs(modulePath));

        return Configuration.create(commandBuilder);
    }
}
