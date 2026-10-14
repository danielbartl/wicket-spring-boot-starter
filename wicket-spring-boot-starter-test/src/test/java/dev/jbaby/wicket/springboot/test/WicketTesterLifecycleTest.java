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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.StreamSupport;

import org.apache.wicket.Application;
import org.apache.wicket.IApplicationListener;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * What {@link WicketTest} hands a test: a tester per test, shared with {@code @BeforeEach}, running a
 * new instance of the application's own Wicket application with Spring injection, destroyed
 * afterwards.
 *
 * @author Daniel Bartl
 */
@WicketTest(classes = GreetingApplication.class)
class WicketTesterLifecycleTest {

	private static final List<WicketTester> TESTERS = new ArrayList<>();
	private static final AtomicInteger DESTROYED = new AtomicInteger();

	private WicketTester fromBeforeEach;

	@BeforeEach
	void watch(WicketTester tester) {
		fromBeforeEach = tester;
		TESTERS.add(tester);
		tester.getApplication().getApplicationListeners().add(new IApplicationListener() {
			@Override
			public void onBeforeDestroyed(Application application) {
				DESTROYED.incrementAndGet();
			}
		});
	}

	@AfterAll
	static void everyTestHadItsOwnTesterAndItWasDestroyed() {
		assertThat(TESTERS).isNotEmpty().doesNotHaveDuplicates();
		assertThat(DESTROYED).hasValue(TESTERS.size());
	}

	@Test
	void injectsSpringBeansIntoPages(WicketTester tester) {
		tester.startPage(GreetingPage.class);

		tester.assertRenderedPage(GreetingPage.class);
		tester.assertLabel("message", "Hello from Spring");
	}

	@Test
	void runsANewInstanceOfTheApplicationsOwnWebApplication(WicketTester tester, @Autowired WebApplication bean) {
		assertThat(tester.getApplication()).isInstanceOf(GreetingWebApplication.class).isNotSameAs(bean);
		assertThat(((GreetingWebApplication) tester.getApplication()).greeting().text())
				.isEqualTo("Hello from Spring");
	}

	@Test
	void registersSpringInjectionOnce(WicketTester tester) {
		assertThat(StreamSupport.stream(tester.getApplication().getComponentInstantiationListeners().spliterator(), false))
				.filteredOn(SpringComponentInjector.class::isInstance)
				.hasSize(1);
	}

	@Test
	void sharesOneTesterBetweenBeforeEachAndTheTest(WicketTester tester) {
		assertThat(tester).isSameAs(fromBeforeEach);
	}
}
