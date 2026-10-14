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

import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.Test;

/**
 * {@link WicketTest#properties()} reaches the application context.
 *
 * @author Daniel Bartl
 */
@WicketTest(classes = GreetingApplication.class, properties = "greeting.text=Hello from a property")
class WicketTestPropertiesTest {

	@Test
	void passesPropertiesToTheApplication(WicketTester tester) {
		tester.startPage(GreetingPage.class);

		tester.assertLabel("message", "Hello from a property");
	}
}
