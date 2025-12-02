/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.testing.extension.extension.deployment;

import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;
import org.wildfly.testing.junit.extension.annotation.GenerateDeployment;

/**
 * Tests {@link GenerateDeployment} with explicit RAR type.
 *
 * @author <a href="mailto:jperkins@ibm.com">James R. Perkins</a>
 */
public class GenerateRarDeploymentIT extends AbstractRarDeploymentTest {

    @GenerateDeployment(GenerateDeployment.DeploymentType.RAR)
    public static void deployment(final ResourceAdapterArchive rar) {
        // Minimal RAR for testing deployment creation
        // Add a placeholder to satisfy ShrinkWrap requirement for at least one asset
        rar.addAsManifestResource(EmptyAsset.INSTANCE, "placeholder.txt");
        // Actual validation of RAR functionality is handled separately
    }
}
