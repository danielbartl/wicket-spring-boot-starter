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

import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

/**
 * A Spring Boot application that wires Spring injection itself, the way the README's migration
 * section describes: its {@link WebApplication} registers the injector in {@code init()}, and its own
 * {@link SpringComponentInjector} bean makes the starter step aside.
 *
 * @author Daniel Bartl
 */
@SpringBootConfiguration
@EnableAutoConfiguration
class HandWiredApplication {

	@Bean
	Greeting greeting() {
		return () -> "Hello from a hand-wired injector";
	}

	@Bean
	WebApplication webApplication() {
		return new HandWiredWebApplication();
	}

	@Bean
	SpringComponentInjector springComponentInjector(WebApplication webApplication, ApplicationContext context) {
		return new SpringComponentInjector(webApplication, context);
	}

	static class HandWiredWebApplication extends WebApplication {

		@Override
		public Class<? extends Page> getHomePage() {
			return GreetingPage.class;
		}

		@Override
		protected void init() {
			super.init();
			getComponentInstantiationListeners().add(new SpringComponentInjector(this));
		}
	}
}
