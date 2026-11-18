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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.annotation.AliasFor;

/**
 * Starts the whole Spring Boot application, like {@link SpringBootTest}, and hands every test a
 * {@link org.apache.wicket.util.tester.WicketTester WicketTester} running the application's own Wicket
 * application, with Spring injection into pages and components.
 * <p>
 * Declare a {@code WicketTester} parameter on a {@code @Test}, {@code @BeforeEach} or
 * {@code @AfterEach} method. Within one test, all of them receive the same tester; every test gets a
 * new one, destroyed when the test ends:
 * </p>
 * <pre class="code">
 * &#064;WicketTest
 * class OrdersPageTests {
 *
 *     &#064;Autowired Orders orders;
 *
 *     &#064;Test
 *     void listsOpenOrders(WicketTester tester) {
 *         orders.place("ACME", ONE_WIDGET);
 *
 *         tester.startPage(OrdersPage.class);
 *
 *         tester.assertLabel("list:orders:0:customer", "ACME");
 *     }
 * }
 * </pre>
 * <p>
 * Use it instead of {@code @SpringBootTest}, not next to it. The application runs in Spring Boot's
 * default {@code MOCK} web environment: no servlet container is started, so tests don't collide on
 * the Wicket filter name. To keep a {@code @SpringBootTest} with other settings, add
 * {@code @ExtendWith(WicketTesterExtension.class)} to it instead.
 * </p>
 *
 * @author Daniel Bartl
 * @see WicketTesterExtension
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@SpringBootTest
@ExtendWith(WicketTesterExtension.class)
public @interface WicketTest {

	/**
	 * Properties in {@code key=value} form added to the test's environment, as in
	 * {@link SpringBootTest#properties()}.
	 *
	 * @return the properties to add
	 */
	@AliasFor(annotation = SpringBootTest.class, attribute = "properties")
	String[] properties() default {};

	/**
	 * The component classes to load the application context from, as in
	 * {@link SpringBootTest#classes()}. By default the {@code @SpringBootConfiguration} is found by
	 * searching up from the test's package.
	 *
	 * @return the component classes to load
	 */
	@AliasFor(annotation = SpringBootTest.class, attribute = "classes")
	Class<?>[] classes() default {};
}
