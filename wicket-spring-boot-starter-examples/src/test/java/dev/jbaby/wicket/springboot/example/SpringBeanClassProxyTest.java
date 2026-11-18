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
package dev.jbaby.wicket.springboot.example;

import java.io.Serializable;

import dev.jbaby.wicket.springboot.test.WicketTest;
import org.apache.wicket.core.util.lang.WicketObjects;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A {@code @SpringBean} whose type is a class rather than an interface. Wicket proxies an interface
 * with a JDK proxy, but a class with a ByteBuddy subclass, created without calling its constructor.
 * That is the part newer JDKs restrict: Wicket 10.11 can't create these proxies on Java 26. The proxy
 * must also survive serialization, as it does when Wicket stores the page holding it.
 *
 * @author Daniel Bartl
 */
@WicketTest
@Import(SpringBeanClassProxyTest.ClassBeanConfig.class)
class SpringBeanClassProxyTest {

	@Test
	void injectsAndSerializesASpringBeanDeclaredAsAClass(WicketTester tester) {
		Holder holder = new Holder();
		Injector.get().inject(holder);

		assertThat(holder.greeter.getClass()).isNotEqualTo(Greeter.class);
		assertThat(holder.greeter.greet()).isEqualTo("Hello from a class, Wicket!");

		Holder restored = WicketObjects.cloneObject(holder);
		assertThat(restored.greeter.greet()).isEqualTo("Hello from a class, Wicket!");
	}

	/** A Spring bean without an interface and without a no-argument constructor. */
	public static class Greeter {

		private final String greeting;

		public Greeter(String greeting) {
			this.greeting = greeting;
		}

		public String greet() {
			return greeting;
		}
	}

	static class Holder implements Serializable {
		private static final long serialVersionUID = 1L;

		@SpringBean
		private Greeter greeter;
	}

	@TestConfiguration
	static class ClassBeanConfig {

		@Bean
		WebApplication webApplication() {
			return new QuickstartWebApplication();
		}

		@Bean
		GreetingService greetingService() {
			return () -> "Hello from Spring, Wicket!";
		}

		@Bean
		Greeter greeter() {
			return new Greeter("Hello from a class, Wicket!");
		}
	}
}
