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

import dev.jbaby.wicket.springboot.DefaultHomePage;
import dev.jbaby.wicket.springboot.DefaultWebApplication;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Without a Wicket application of its own, the tester runs the starter's default one.
 *
 * @author Daniel Bartl
 */
@WicketTest(classes = BareApplication.class)
class WicketTestDefaultApplicationTest {

	@Test
	void runsTheStartersDefaultApplication(WicketTester tester) {
		assertThat(tester.getApplication()).isInstanceOf(DefaultWebApplication.class);

		tester.startPage(DefaultHomePage.class);

		tester.assertRenderedPage(DefaultHomePage.class);
	}
}
