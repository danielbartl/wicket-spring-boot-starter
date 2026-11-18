/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.jbaby.wicket.springboot.test;

import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.mock.MockServletContext;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.ClassUtils;
import org.springframework.web.context.WebApplicationContext;

/**
 * Resolves {@link WicketTester} parameters of test methods from the test's Spring application context.
 * <p>
 * For every test it creates a new instance of the context's {@link WebApplication} bean class through
 * Spring, so constructor dependencies are injected and the starter registers its
 * {@link org.apache.wicket.spring.injection.annot.SpringComponentInjector SpringComponentInjector}
 * exactly as it does in the running application. The tester's servlet context carries the Spring
 * application context, as the embedded container's does, so an application that registers the
 * injector itself in {@code init()} gets injection too. The tester is shared by the test's
 * {@code @BeforeEach}, {@code @Test} and {@code @AfterEach} methods and destroyed after the test.
 * </p>
 * <p>
 * The {@code WebApplication} bean itself is never handed to a tester: Spring keeps test contexts
 * cached across test classes, and a destroyed application would break the next test using it.
 * </p>
 * <p>
 * {@link WicketTest} registers this extension; add {@code @ExtendWith(WicketTesterExtension.class)}
 * to a {@code @SpringBootTest} to use it without {@code @WicketTest}.
 * </p>
 *
 * @author Daniel Bartl
 * @see WicketTest
 */
public class WicketTesterExtension implements ParameterResolver, AfterEachCallback {

	private static final Namespace NAMESPACE = Namespace.create(WicketTesterExtension.class);

	/**
	 * Creates the extension. JUnit does that for {@link WicketTest} and {@code @ExtendWith}.
	 */
	public WicketTesterExtension() {
	}

	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		return parameterContext.getParameter().getType() == WicketTester.class;
	}

	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		if (extensionContext.getTestMethod().isEmpty()) {
			throw new ParameterResolutionException("A WicketTester belongs to a single test: declare it on a "
					+ "@Test, @BeforeEach or @AfterEach method, not on " + parameterContext.getDeclaringExecutable());
		}
		Store store = extensionContext.getStore(NAMESPACE);
		WicketTester tester = store.get(WicketTester.class, WicketTester.class);
		if (tester == null) {
			tester = createTester(SpringExtension.getApplicationContext(extensionContext));
			store.put(WicketTester.class, tester);
		}
		return tester;
	}

	@Override
	public void afterEach(ExtensionContext extensionContext) {
		WicketTester tester = extensionContext.getStore(NAMESPACE).remove(WicketTester.class, WicketTester.class);
		if (tester != null) {
			tester.destroy();
		}
	}

	private static WicketTester createTester(ApplicationContext context) {
		if (!(context instanceof WebApplicationContext webContext)) {
			throw new ParameterResolutionException("A WicketTester needs a servlet web application context, but "
					+ "the test runs with " + context.getClass().getName()
					+ ". Keep the default MOCK web environment.");
		}
		WebApplication application = newApplication(webContext);
		MockServletContext servletContext = new MockServletContext(application, null);
		servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, webContext);
		return new WicketTester(application, servletContext);
	}

	private static WebApplication newApplication(WebApplicationContext context) {
		Class<?> type;
		try {
			type = ClassUtils.getUserClass(context.getBean(WebApplication.class));
		}
		catch (BeansException ex) {
			throw new ParameterResolutionException("Found no single Wicket WebApplication bean in the test's "
					+ "application context to create a WicketTester from.", ex);
		}
		try {
			return (WebApplication) context.getAutowireCapableBeanFactory().createBean(type);
		}
		catch (BeansException ex) {
			throw new ParameterResolutionException("Couldn't create a new " + type.getName()
					+ " for the WicketTester. Its constructor parameters have to be Spring beans.", ex);
		}
	}
}
