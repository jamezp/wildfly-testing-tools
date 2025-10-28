/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.testing.junit.condition;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.wildfly.testing.junit.annotations.JBossHome;
import org.wildfly.testing.junit.api.ServerConfiguration;

/**
 * Resolves the {@code jboss.home} system property or if not set the {@code JBOSS_HOME} environment variable. If neither
 * are set a {@link ParameterResolutionException} is thrown.
 *
 * @author <a href="mailto:jperkins@ibm.com">James R. Perkins</a>
 */
public class JBossHomeParameterResolver implements ParameterResolver {
    @Override
    public boolean supportsParameter(final ParameterContext parameterContext, final ExtensionContext extensionContext)
            throws ParameterResolutionException {
        // Ignore parameters not annotated with @JBossHome
        if (!parameterContext.isAnnotated(JBossHome.class)) {
            return false;
        }
        final Class<?> parameterType = parameterContext.getParameter().getType();
        return parameterType.isAssignableFrom(Path.class) || parameterType.isAssignableFrom(String.class)
                || parameterType.isAssignableFrom(File.class);
    }

    @Override
    public Object resolveParameter(final ParameterContext parameterContext, final ExtensionContext extensionContext)
            throws ParameterResolutionException {
        final Optional<Path> value = ServerConfiguration.resolveJBossHome(extensionContext);
        if (value.isEmpty()) {
            throw new ParameterResolutionException(
                    "Could not resolve the jboss.home system property or JBOSS_HOME environment variable.");
        }
        final Path path = value.get().toAbsolutePath();
        final Class<?> parameterType = parameterContext.getParameter().getType();
        if (parameterType.isAssignableFrom(Path.class)) {
            return path;
        }
        if (parameterType.isAssignableFrom(String.class)) {
            return path.toString();
        }
        if (parameterType.isAssignableFrom(File.class)) {
            return path.toFile();
        }
        throw new ParameterResolutionException(
                "Cannot convert the JBoss Home directory into a type of " + parameterType.getName());
    }
}
