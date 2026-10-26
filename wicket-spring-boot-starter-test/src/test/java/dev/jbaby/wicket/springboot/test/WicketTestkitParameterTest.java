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

import dev.jbaby.wicket.testkit.WicketTestkit;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static dev.jbaby.wicket.testkit.locator.Locator.locate;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * What {@link WicketTest} hands a {@code WicketTestkit} parameter: a testkit for the test's own
 * tester, one per test, shared with {@code @BeforeEach}.
 *
 * @author Daniel Bartl
 */
@WicketTest(classes = GreetingApplication.class)
class WicketTestkitParameterTest {

	private static final List<WicketTestkit> KITS = new ArrayList<>();

	private WicketTestkit fromBeforeEach;

	@BeforeEach
	void remember(WicketTestkit kit) {
		fromBeforeEach = kit;
		KITS.add(kit);
	}

	@AfterAll
	static void everyTestHadItsOwnKit() {
		assertThat(KITS).isNotEmpty().doesNotHaveDuplicates();
	}

	@Test
	void wrapsTheTestsOwnTester(WicketTester tester) {
		tester.startPage(GreetingPage.class);

		fromBeforeEach.assertLabel(locate(Label.class).withId("message"), "Hello from Spring");
	}

	@Test
	void sharesOneKitBetweenBeforeEachAndTheTest(WicketTestkit kit) {
		assertThat(kit).isSameAs(fromBeforeEach);
	}
}
