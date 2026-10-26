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

import dev.jbaby.wicket.springboot.test.WicketTest;
import dev.jbaby.wicket.testkit.WicketTestkit;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import static dev.jbaby.wicket.testkit.locator.Locator.locate;

/**
 * The README's Testing section: the Quickstart page tested with the test starter's
 * {@link WicketTest}. No servlet container is started; the {@link WicketTester} parameter runs a new
 * instance of the application's own {@link WebApplication}, with {@code @SpringBean} injection, and is
 * destroyed after the test. A {@link WicketTestkit} parameter wraps that tester.
 *
 * @author Daniel Bartl
 */
@WicketTest
@Import(WicketTesterQuickstartTest.QuickstartConfig.class)
class WicketTesterQuickstartTest {

	@Test
	void rendersTheQuickstartPageWithItsInjectedService(WicketTester tester) {
		tester.startPage(QuickstartHomePage.class);

		tester.assertRenderedPage(QuickstartHomePage.class);
		tester.assertLabel("message", "Hello from Spring, Wicket!");
	}

	@Test
	void findsTheMessageByWhatItIs(WicketTester tester, WicketTestkit kit) {
		tester.startPage(QuickstartHomePage.class);

		kit.assertLabel(locate(Label.class).withId("message"), "Hello from Spring, Wicket!");
	}

	@TestConfiguration
	static class QuickstartConfig {

		@Bean
		WebApplication webApplication() {
			return new QuickstartWebApplication();
		}

		@Bean
		GreetingService greetingService() {
			return () -> "Hello from Spring, Wicket!";
		}
	}
}
