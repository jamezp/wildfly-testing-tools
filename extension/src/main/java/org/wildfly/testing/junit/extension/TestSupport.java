/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.testing.junit.extension;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.support.AnnotationSupport;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.commons.util.AnnotationUtils;
import org.wildfly.testing.junit.extension.annotation.DeploymentProducer;
import org.wildfly.testing.junit.extension.annotation.GenerateDeployment;
import org.wildfly.testing.junit.extension.annotation.ManualMode;

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

    /**
     * Locates the method annotated with {@link GenerateDeployment} and returns the result of invoking the method. If no
     * deployment method exists, an empty optional is returned.
     *
     * @param context the context to find the deployment method on
     *
     * @return the optional supplier
     */
    static Optional<Archive<?>> findDeploymentMethod(final ExtensionContext context) {
        final var testClass = context.getRequiredTestClass();
        final var methods = AnnotationSupport.findAnnotatedMethods(testClass, GenerateDeployment.class,
                HierarchyTraversalMode.BOTTOM_UP);
        if (methods.isEmpty()) {
            return Optional.empty();
        }
        final var method = validate(testClass, methods);
        // This must be a void return type
        if (!method.getReturnType().equals(Void.TYPE)) {
            throw new JUnitException(String.format("Method '%s' must return void", method));
        }
        final int count = method.getParameterCount();
        if (count > 2) {
            throw new JUnitException(
                    String.format("Method %s has too many parameters. Only two parameters are allowed.", method));
        }
        if (count == 0) {
            throw new JUnitException(String.format("Method '%s' must have at least one parameter.", method));
        }
        final var testDeployment = method.getAnnotation(GenerateDeployment.class);
        final var parameterTypes = method.getParameterTypes();
        final var deploymentType = testDeployment.value() == GenerateDeployment.DeploymentType.INFER
                ? inferredType(parameterTypes[0]).orElseThrow(() -> new JUnitException(String
                        .format("Could not infer the type to create for argument %s on method %s. " +
                                "If this is a custom %s type, please consider using a @DeploymentProducer", parameterTypes[0],
                                method, Archive.class.getName())))
                : testDeployment.value();
        // Check that first parameter is an Archive type
        if (!Archive.class.isAssignableFrom(parameterTypes[0])) {
            throw new JUnitException(String.format(
                    "@GenerateDeployment method %s must have an Archive type as the first parameter, but was %s",
                    method.getName(), parameterTypes[0].getName()));
        }
        // Validate the parameter type is assignable from the deployment type
        if (!deploymentType.archiveType().isAssignableFrom(parameterTypes[0])) {
            throw new JUnitException(String.format("Parameter '%s' must be assignable from '%s'", parameterTypes[0],
                    deploymentType.archiveType()));
        }
        final TestInfo testInfo;
        // Check the second parameter if applicable
        if (parameterTypes.length == 2) {
            if (!TestInfo.class.isAssignableFrom(parameterTypes[1])) {
                throw new JUnitException(
                        String.format("Parameter '%s' must be assignable from '%s'", parameterTypes[1], TestInfo.class));
            }
            testInfo = new ParameterTestInfo(context);
        } else {
            testInfo = null;
        }
        try {
            final Archive<?> archive = ShrinkWrap.create(deploymentType.archiveType(),
                    testClass.getSimpleName() + deploymentType.extension());
            if (testInfo == null) {
                method.invoke(null, archive);
            } else {
                method.invoke(null, archive, testInfo);
            }

            return Optional.of(archive);
        } catch (Exception e) {
            throw new JUnitException(String.format("Failed to execute deployment method in %s: %s", method.getName(), method),
                    e);
        }
    }

    static Optional<Archive<?>> findDeploymentProducerMethod(final ExtensionContext context) {
        final var testClass = context.getRequiredTestClass();
        final var methods = AnnotationSupport.findAnnotatedMethods(testClass, DeploymentProducer.class,
                HierarchyTraversalMode.BOTTOM_UP);
        if (methods.isEmpty()) {
            return Optional.empty();
        }
        final var method = validate(testClass, methods);

        // The return type must be an Archive<?> of some type
        if (!Archive.class.isAssignableFrom(method.getReturnType())) {
            throw new JUnitException(
                    String.format("Method '%s' must return assignable from %s", method, Archive.class.getName()));
        }
        // A single parameter of type TestInfo is allowed, but not required
        final var parameterTypes = method.getParameterTypes();
        if (parameterTypes.length > 1) {
            throw new JUnitException(String.format(
                    "Method %s has too many parameters. Only one parameter of type %s is allowed.", method, TestInfo.class));
        }
        if (parameterTypes.length == 1 && !TestInfo.class.isAssignableFrom(parameterTypes[0])) {
            throw new JUnitException(String.format(
                    "Method %s parameter must be of type %s, but was %s.", method, TestInfo.class, parameterTypes[0]));
        }

        final TestInfo testInfo;
        // Check the second parameter if applicable
        if (parameterTypes.length == 1) {
            testInfo = new ParameterTestInfo(context);
        } else {
            testInfo = null;
        }
        final Archive<?> archive;
        try {
            if (testInfo == null) {
                archive = (Archive<?>) method.invoke(null);
            } else {
                archive = (Archive<?>) method.invoke(null, testInfo);
            }
            return Optional.of(archive);
        } catch (Exception e) {
            throw new JUnitException(String.format("Failed to execute deployment method in %s: %s", method.getName(), method),
                    e);
        }
    }

    private static Method validate(final Class<?> testClass, final List<Method> methods) {
        // Only one deployment method is allowed
        if (methods.size() > 1) {
            throw new JUnitException(
                    String.format("Found more than one deployment method in %s: %s", testClass.getName(), methods));
        }
        final var method = methods.get(0);
        if (!Modifier.isStatic(method.getModifiers())) {
            throw new JUnitException(
                    String.format("Deployment method %s in type %s must be static.", testClass.getName(), method));
        }
        if (!method.trySetAccessible()) {
            throw new JUnitException(String.format("Method '%s' is not accessible", method));
        }
        return method;
    }

    private static class ParameterTestInfo implements TestInfo {

        private final String displayName;
        private final Set<String> tags;
        private final Class<?> testClass;

        ParameterTestInfo(ExtensionContext extensionContext) {
            this.displayName = extensionContext.getDisplayName();
            this.tags = extensionContext.getTags();
            this.testClass = extensionContext.getTestClass().orElse(null);
        }

        @Override
        public String getDisplayName() {
            return this.displayName;
        }

        @Override
        public Set<String> getTags() {
            return this.tags;
        }

        @Override
        public Optional<Class<?>> getTestClass() {
            return Optional.ofNullable(this.testClass);
        }

        @Override
        public Optional<Method> getTestMethod() {
            return Optional.empty();
        }

        @Override
        public String toString() {
            return "ParameterTestInfo(displayName=" + this.displayName + ", tags=" + this.tags + ", testClass="
                    + this.testClass + ", testMethod=null)";
        }

    }

    /**
     * Finds the deployment method (either {@link GenerateDeployment} or {@link DeploymentProducer})
     * and returns the Method reference without invoking it.
     *
     * @param context the extension context
     *
     * @return the deployment method, or empty if no deployment method exists
     */
    static Optional<Method> findDeploymentMethodReference(final ExtensionContext context) {
        final var testClass = context.getRequiredTestClass();

        // Check for @GenerateDeployment
        final var generateMethods = AnnotationSupport.findAnnotatedMethods(testClass, GenerateDeployment.class,
                HierarchyTraversalMode.BOTTOM_UP);
        if (!generateMethods.isEmpty()) {
            return Optional.of(validate(testClass, generateMethods));
        }

        // Check for @DeploymentProducer
        final var producerMethods = AnnotationSupport.findAnnotatedMethods(testClass, DeploymentProducer.class,
                HierarchyTraversalMode.BOTTOM_UP);
        if (!producerMethods.isEmpty()) {
            return Optional.of(validate(testClass, producerMethods));
        }

        return Optional.empty();
    }

    private static Optional<GenerateDeployment.DeploymentType> inferredType(final Class<?> methodParameterType) {
        for (final var type : GenerateDeployment.DeploymentType.values()) {
            if (type == GenerateDeployment.DeploymentType.INFER) {
                continue;
            }
            if (type.archiveType() == methodParameterType) {
                return Optional.of(type);
            }
        }
        return Optional.empty();
    }

}
