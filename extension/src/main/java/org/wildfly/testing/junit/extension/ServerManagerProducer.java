/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.testing.junit.extension;

import java.lang.annotation.Annotation;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.kohsuke.MetaInfServices;
import org.wildfly.plugin.tools.server.ServerManager;
import org.wildfly.testing.junit.api.ServerResourceProducer;

/**
 * Produces {@link ServerManager} instances for injection into test fields and parameters.
 * The ServerManager is obtained from the shared server started by {@link WildFlyExtension}.
 *
 * @author <a href="mailto:jperkins@ibm.com">James R. Perkins</a>
 */
@MetaInfServices
public class ServerManagerProducer implements ServerResourceProducer {
    @Override
    public boolean canInject(final ExtensionContext context, final Class<?> clazz, final Annotation... annotations) {
        return ServerManager.class.isAssignableFrom(clazz);
    }

    @Override
    public Object produce(final ExtensionContext context, final Class<?> clazz, final Annotation... annotations)
            throws IllegalArgumentException {
        if (!ServerManager.class.isAssignableFrom(clazz)) {
            throw new IllegalArgumentException(
                    String.format("Type %s is not assignable to %s", clazz.getName(), ServerManager.class.getName()));
        }

        // Get the server from WildFlyExtension and return as managed which will not allow shutting down the server
        return WildFlyExtension.getServer(context)
                .map(ServerManager::asManaged)
                .orElse(null);
    }
}
