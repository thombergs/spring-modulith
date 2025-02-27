/*
 * Copyright 2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.modulith.test;

import java.lang.reflect.Method;
import java.util.function.Function;

import org.awaitility.core.ConditionFactory;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.Assert;

/**
 * A JUnit {@link InvocationInterceptor} to register a default customizer to be applied to all {@link Scenario}
 * instances associated with that test case.
 *
 * @author Oliver Drotbohm
 */
public interface ScenarioCustomizer extends InvocationInterceptor {

	/**
	 * Return a customizer to be applied to the {@link Scenario} instance handed into the given method.
	 *
	 * @param method will never be {@literal null}.
	 * @param context will never be {@literal null}.
	 * @return must not be {@literal null}.
	 */
	Function<ConditionFactory, ConditionFactory> getDefaultCustomizer(Method method, ApplicationContext context);

	/*
	 * (non-Javadoc)
	 * @see org.junit.jupiter.api.extension.InvocationInterceptor#interceptTestTemplateMethod(org.junit.jupiter.api.extension.InvocationInterceptor.Invocation, org.junit.jupiter.api.extension.ReflectiveInvocationContext, org.junit.jupiter.api.extension.ExtensionContext)
	 */
	@Override
	default void interceptTestTemplateMethod(Invocation<Void> invocation,
			ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {

		prepareScenarioInstance(invocationContext, extensionContext);

		invocation.proceed();
	}

	/*
	 * (non-Javadoc)
	 * @see org.junit.jupiter.api.extension.InvocationInterceptor#interceptTestFactoryMethod(org.junit.jupiter.api.extension.InvocationInterceptor.Invocation, org.junit.jupiter.api.extension.ReflectiveInvocationContext, org.junit.jupiter.api.extension.ExtensionContext)
	 */
	@Override
	default <T> T interceptTestFactoryMethod(Invocation<T> invocation,
			ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {

		prepareScenarioInstance(invocationContext, extensionContext);

		return invocation.proceed();
	}

	/*
	 * (non-Javadoc)
	 * @see org.junit.jupiter.api.extension.InvocationInterceptor#interceptTestMethod(org.junit.jupiter.api.extension.InvocationInterceptor.Invocation, org.junit.jupiter.api.extension.ReflectiveInvocationContext, org.junit.jupiter.api.extension.ExtensionContext)
	 */
	@Override
	default void interceptTestMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext,
			ExtensionContext extensionContext) throws Throwable {

		prepareScenarioInstance(invocationContext, extensionContext);

		invocation.proceed();
	}

	private void prepareScenarioInstance(ReflectiveInvocationContext<Method> invocationContext,
			ExtensionContext extensionContext) {

		invocationContext.getArguments().stream()
				.filter(Scenario.class::isInstance)
				.map(Scenario.class::cast)
				.forEach(it -> {

					var context = SpringExtension.getApplicationContext(extensionContext);
					var customizer = getDefaultCustomizer(invocationContext.getExecutable(), context);
					Assert.state(customizer != null, "Customizer must not be null!");

					it.setDefaultCustomizer(customizer);
				});
	}
}
