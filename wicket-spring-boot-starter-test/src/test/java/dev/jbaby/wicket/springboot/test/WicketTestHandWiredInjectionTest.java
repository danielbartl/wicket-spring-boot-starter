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

import java.util.stream.StreamSupport;

import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * An application that registers the injector itself in {@code init()} gets injection from the
 * tester's servlet context, and only once.
 *
 * @author Daniel Bartl
 */
@WicketTest(classes = HandWiredApplication.class)
class WicketTestHandWiredInjectionTest {

	@Test
	void injectsThroughTheApplicationsOwnInjector(WicketTester tester) {
		tester.startPage(GreetingPage.class);

		tester.assertLabel("message", "Hello from a hand-wired injector");
	}

	@Test
	void registersSpringInjectionOnce(WicketTester tester) {
		assertThat(StreamSupport.stream(tester.getApplication().getComponentInstantiationListeners().spliterator(), false))
				.filteredOn(SpringComponentInjector.class::isInstance)
				.hasSize(1);
	}
}
