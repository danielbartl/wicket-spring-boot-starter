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

/**
 * An application's own Wicket application, with a constructor dependency on a Spring bean.
 *
 * @author Daniel Bartl
 */
class GreetingWebApplication extends WebApplication {

	private final Greeting greeting;

	GreetingWebApplication(Greeting greeting) {
		this.greeting = greeting;
	}

	Greeting greeting() {
		return greeting;
	}

	@Override
	public Class<? extends Page> getHomePage() {
		return GreetingPage.class;
	}
}
