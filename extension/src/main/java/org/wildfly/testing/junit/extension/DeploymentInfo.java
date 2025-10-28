/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.testing.junit.extension;

import java.util.Set;

/**
 * Simple information about a deployment.
 *
 * @author <a href="mailto:jperkins@ibm.com">James R. Perkins</a>
 */
record DeploymentInfo(String deploymentName, Set<String> serverGroup) {
}
