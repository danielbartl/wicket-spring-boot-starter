# Wicket Spring Boot Starter

**[Project page](https://danielbartl.github.io/wicket-spring-boot-starter/)**

A lightweight, zero-boilerplate starter that integrates **Apache Wicket 10.x** with **Spring Boot 4.x**. It configures the Wicket web context automatically, bridges Spring injection into Wicket components, and enables simple configuration via `application.properties` or `application.yml`.

---

## Motivation

Running Wicket on Spring Boot always needs the same handful of pieces: a `WebApplication`, a filter
registration for it, and a `SpringComponentInjector` so that `@SpringBean` works. None of it is
difficult, but it is the same every time, it is easy to get subtly wrong, and it is the first thing
standing between someone and a running Wicket page.

This starter supplies exactly those pieces and then gets out of the way. Everything past that point
is ordinary Wicket and ordinary Spring, documented in their own projects.

**Staying small is the point, not a limitation.** A deliberately narrow starter is cheap to keep
alive: when Wicket or Spring Boot publishes a new major version, there is very little surface to
re-verify, and each release pins one Wicket version against the Spring Boot lines it has been tested
with (see the [Compatibility Matrix](#compatibility-matrix)).

It also makes a good starting point for teaching Wicket: the whole auto-configuration fits on a
screen, so nothing about how Wicket and Spring Boot meet stays hidden. Pointing a
[Spring Initializr](#using-it-from-your-own-spring-initializr) at it makes a new Wicket project one
click away.

### Is this the right starter for you?

There is an established alternative, [`MarcGiffing/wicket-spring-boot`][giffing]
(`com.giffing.wicket.spring.boot.starter`), which is a much broader integration: Spring Security,
native WebSockets, bean validation, CSRF protection, several serializers, session datastores,
monitoring and a set of development-mode helpers.

Pick that one if you want those batteries included. Pick this one if you would rather start from the
smallest thing that works and add what you need yourself. They solve the same problem with opposite
philosophies, and neither is a replacement for the other.

[giffing]: https://github.com/MarcGiffing/wicket-spring-boot

---

## Features

- **Auto-Configuration**: Automatically registers Wicket's `WicketFilter` in Spring Boot's embedded servlet container.
- **Auto-Discovery**: Detects any subclass of Wicket's `WebApplication` registered as a Spring bean/component and binds it.
- **Spring Injection Support**: Automatically adds `SpringComponentInjector` to the application, allowing you to use `@SpringBean` to inject Spring-managed components directly into Wicket Pages and Panels.
- **Externalized Settings**: Configure Wicket's execution mode (`DEVELOPMENT` vs `DEPLOYMENT`) and the filter's path, name and order directly from Spring properties.
- **IDE Auto-Completion Support**: Ships with built-in configuration metadata, enabling auto-completion and documentation tooltips for Wicket properties in popular IDEs (IntelliJ, Eclipse, VS Code).
- **Spring MVC Coexistence**: Requests Wicket does not handle fall through to Spring MVC, so `@RestController` endpoints work next to Wicket pages.
- **DevTools Support**: Works with `spring-boot-devtools` out of the box: automatic restarts, the back button and restored pages keep working with `@SpringBean` fields.

---

## How It Works

The starter contributes a single auto-configuration, `WicketAutoConfiguration`. It applies only when
all of the following hold:

- the application is a **servlet** web application (a reactive application is left alone);
- Wicket's `WebApplication` and `WicketFilter` are on the classpath;
- `wicket.enabled` is not set to `false`.

When it applies, it contributes:

| Bean | What it does |
|---|---|
| `webApplication` | The Wicket `WebApplication`. Falls back to a built-in default (see below) if you have not defined one. |
| `springComponentInjectorRegistrar` | Adds Wicket's `SpringComponentInjector` to every `WebApplication` bean as soon as it is created, which is what makes `@SpringBean` work inside pages and components, and already in your application's `init()`. |
| `wicketFilterRegistration` | Registers `WicketFilter` with the servlet container, mapped at `wicket.filter-path`, passing Wicket's filter-mapping and `configuration` init parameters from your settings. |

### Living alongside Spring MVC

The starter brings `spring-boot-starter-webmvc`, so your application also has Spring MVC available.
Wicket's filter is mapped at `/*` but forwards anything it does not handle further down the filter
chain, so `@RestController` endpoints keep working next to Wicket pages:

```java
@RestController
class GreetingController {

    @GetMapping("/api/greeting")
    String greeting() {
        return "hello";
    }
}
```

With the defaults, `/api/greeting` reaches the controller while `/` renders your Wicket home page.
If you would rather keep the two strictly apart, confine Wicket to its own prefix with
`wicket.filter-path=/app/*`.

### Deploying as a WAR

The starter brings an embedded Tomcat, but it does not commit you to it. Spring Boot's usual recipe
for deploying to an external servlet container works unchanged — set `war` packaging, extend
`SpringBootServletInitializer`, and re-declare the container at `provided` scope:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-tomcat</artifactId>
    <scope>provided</scope>
</dependency>
```

A direct declaration wins over the transitive one, so the embedded container is demoted to
`provided` and stays out of `WEB-INF/lib` while Wicket and the starter are packaged as normal.

### Using Spring Boot DevTools

The starter works with `spring-boot-devtools` out of the box. It ships a
`META-INF/spring-devtools.properties` that loads the Wicket jars (this starter and any wicketstuff
modules included) in DevTools' restart classloader, next to your own classes. Without it, Wicket would restore stored pages
(e.g. on the back button) against a stale copy of your classes and fail with a
`ClassCastException` on every `@SpringBean` field.

On a restart your session survives, but by default the pages in it do not: Wicket keeps them in
the servlet container's temporary directory, and Spring Boot creates a new one for every start of
the embedded Tomcat. Requesting an old page then simply renders it afresh. To keep page state across
restarts as well, give Tomcat a fixed base directory in development:

```properties
server.tomcat.basedir=target/tomcat
```

---

## Dependency Configuration

This is the only dependency you need: it brings Apache Wicket, the Wicket/Spring bridge and the
embedded servlet container with it. Add it to your Maven `pom.xml` (check
[Maven Central](https://central.sonatype.com/artifact/dev.jbaby/wicket-spring-boot-starter)
for the latest released version):

```xml
<dependency>
    <groupId>dev.jbaby</groupId>
    <artifactId>wicket-spring-boot-starter</artifactId>
    <version>0.1.0</version>
</dependency>
```

---

## Compatibility Matrix

Ensure you match the correct starter version with your Spring Boot and Java environment:

| Starter Version | Apache Wicket | Spring Boot | Spring Framework | Java |
| :--- | :--- | :--- | :--- | :--- |
| **`0.1.x`** (Current) | `10.11.x` | `4.0.x`, `4.1.x` | `7.0.x` | 17 to 25 |

Java 26 and later are not supported yet: Wicket 10.11 fails to create the `@SpringBean` proxies there,
because Java 26 no longer lets ByteBuddy define classes through `Unsafe`. This is fixed in Wicket
10.12, which the next starter release will move to.

Compatibility is checked by the integration test under `wicket-spring-boot-starter/src/it`: a
project shaped like one generated by Spring Initializr, inheriting from `spring-boot-starter-parent`
so it runs against exactly the library versions Spring Boot manages. Every build runs it against the
Spring Boot version in the parent pom; to check another supported line, run from the starter module:

```bash
mvn verify -Dspring-boot.version=4.0.8
```

---

## The Default Page

Add the dependency, start the application, and you already have a running Wicket application: with
no `WebApplication` bean of your own, the starter registers a built-in one that serves a placeholder
home page telling you how to replace it.

That default disappears the moment you declare your own `WebApplication` bean, which is what the
Quickstart below does. It exists so a freshly generated project runs and shows something, not as a
page you are meant to keep.

---

## Quickstart

### 1. Create a Wicket Application Component
Subclass `WebApplication` and register it as a Spring bean using `@Component`:

```java
package com.example;

import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.WebApplication;
import org.springframework.stereotype.Component;

@Component
public class WicketApplication extends WebApplication {

    @Override
    public Class<? extends Page> getHomePage() {
        return HomePage.class;
    }

    @Override
    protected void init() {
        super.init();
        // Additional custom Wicket configurations...
    }
}
```

### 2. Inject Spring Beans into Pages
Any Spring bean can be injected, for example a simple service:

```java
package com.example;

import org.springframework.stereotype.Service;

@Service
public class MySpringService {

    public String sayHello() {
        return "Hello from Spring!";
    }
}
```

Use Wicket's standard `@SpringBean` annotation to inject it into a page:

```java
package com.example;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.spring.injection.annot.SpringBean;

public class HomePage extends WebPage {
    private static final long serialVersionUID = 1L;

    @SpringBean
    private MySpringService mySpringService;

    public HomePage() {
        add(new Label("message", mySpringService.sayHello()));
    }
}
```

Every Wicket page needs its markup, in an HTML file named after the class. Put it in
`src/main/resources/com/example/HomePage.html`: Maven only copies resources from
`src/main/resources`, so an HTML file next to the Java source would not end up on the classpath.

```html
<!DOCTYPE html>
<html xmlns:wicket="http://wicket.apache.org">
<body>
    <h1 wicket:id="message"></h1>
</body>
</html>
```

### 3. Bootstrap your Spring Boot App
Annotate your entry point with `@SpringBootApplication` and run it:

```java
package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExampleApplication.class, args);
    }
}
```

---

## Overriding the Defaults

Every bean the starter contributes steps aside as soon as you define your own, so you can take over
as much or as little as you need:

| Bean | Steps aside when | Define your own to |
|---|---|---|
| `webApplication` | any `WebApplication` bean exists | use your own Wicket application (the usual case — see the Quickstart) |
| `springComponentInjectorRegistrar` | any `SpringComponentInjector` bean exists | control how Spring injection is wired |
| `wicketFilterRegistration` | a `FilterRegistrationBean<WicketFilter>` or a plain `WicketFilter` bean exists | control every detail of the filter registration (for path, name and order, the `wicket.filter-*` properties are enough) |

Overriding the filter registration does **not** cost you Spring injection: the injector is bound to
the `WebApplication` rather than to the filter, so `@SpringBean` keeps working in your pages either
way.

### Migrating an existing Wicket + Spring application

Applications that wire Wicket and Spring by hand usually register the injector themselves, along
these lines:

```java
@Override
protected void init() {
    super.init();
    getComponentInstantiationListeners().add(new SpringComponentInjector(this));
}
```

Keeping that line as well as the starter's registration would register two injectors. Either drop
it and let the starter do it, or, if you want to keep control of the wiring, declare your own
`SpringComponentInjector` bean so the starter steps aside.

---

## Configuration Properties

The following properties can be configured in your `application.properties` or `application.yml` file:

| Property | Default Value | Description |
|---|---|---|
| `wicket.enabled` | `true` | Set to `false` to switch the auto-configuration off entirely. |
| `wicket.filter-path` | `/*` | URL mapping pattern for the Wicket filter. |
| `wicket.filter-name` | `wicket-filter` | The name of the registered Wicket servlet filter. |
| `wicket.filter-order` | `Ordered.LOWEST_PRECEDENCE` | Position of the Wicket filter in the servlet filter chain; lower values run earlier. |
| `wicket.configuration` | `DEVELOPMENT` | The configuration type: `DEVELOPMENT` or `DEPLOYMENT`. |

For example:
```properties
wicket.configuration=DEPLOYMENT
wicket.filter-path=/app/*
wicket.filter-name=my-custom-wicket-filter
```

By default the Wicket filter runs last, after every other filter, including Spring Security's
filter chain (order `-100`). That is usually what you want, since security decisions are made before
Wicket renders anything. Set `wicket.filter-order` if another filter of yours has to run after
Wicket instead.

---

## Going to Production

Like Wicket itself, the starter runs in `DEVELOPMENT` mode unless told otherwise, and says so with a
banner in the log on every start. That mode is meant for your machine only: it reloads changed
markup, leaves `wicket:id` attributes in the rendered HTML, enables the Ajax debug window, serves
unminified JavaScript and renders exception pages with full stack traces.

Switch to `DEPLOYMENT` wherever the application actually runs. A Spring profile keeps the setting
next to the rest of your production configuration, in `application-prod.properties`:

```properties
wicket.configuration=DEPLOYMENT
```

activated with `--spring.profiles.active=prod` (or `SPRING_PROFILES_ACTIVE=prod`). Without a
profile, setting the environment variable `WICKET_CONFIGURATION=DEPLOYMENT` does the same.

---

## Testing

Wicket keys its application registry by the filter name, and Spring keeps test contexts cached for
the lifetime of the JVM. Two tests that each start a real servlet container therefore collide on the
default filter name, failing with `Application with name 'wicket-filter' already exists`.

Give each such test its own filter name:

```java
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "wicket.filter-name=my-test-filter")
class MyIntegrationTest {
    // ...
}
```

Tests that do not start a container (the default `MOCK` web environment) are unaffected, because the
filter is never initialised.

---

## Using it from your own Spring Initializr

The starter is not listed on start.spring.io, but [Spring Initializr](https://github.com/spring-io/initializr)
is open source, and an instance of your own (for a team, or for a training) can offer it next to
everything start.spring.io has. IntelliJ IDEA and the Spring Boot CLI both accept a custom Initializr
URL, so generating a Wicket project then works exactly like generating any other.

### The Initializr entry

The dependency is added to the Initializr's `application.yml`. Spring Boot's BOM does not manage
this starter, so the entry has to say which starter version goes with which Spring Boot line;
otherwise the generated build has no version for the dependency and does not resolve.

```yaml
initializr:
  dependencies:
    - name: Web
      content:
        - name: Apache Wicket
          id: wicket
          description: Build component-oriented, server-side web applications with Apache Wicket.
          groupId: dev.jbaby
          artifactId: wicket-spring-boot-starter
          compatibilityRange: "[4.0.0,4.2.0-M1)"
          mappings:
            - compatibilityRange: "[4.0.0,4.2.0-M1)"
              version: 0.1.0
          links:
            - rel: reference
              href: https://github.com/danielbartl/wicket-spring-boot-starter
            - rel: guide
              href: https://wicket.apache.org/start/quickstart.html
```

- `compatibilityRange` is always bounded above. When a new Spring Boot line appears, it is only
  opened up once the integration test above passes against it, so that Initializr never offers a
  combination that has not been tested.
- Each `mappings` entry pins the starter release for a Spring Boot range. When a starter release
  drops or adds a Spring Boot line, split the range into several mappings.
- `version` must be a release available on Maven Central, never a snapshot.

---

## Further Reading

- [Apache Wicket documentation](https://wicket.apache.org/) — writing pages, components and markup.
- [WicketStuff](https://github.com/wicketstuff/core/wiki) — community components and integrations for
  Wicket, which work with this starter like any other Wicket library.
- [API documentation](https://www.javadoc.io/doc/dev.jbaby/wicket-spring-boot-starter) — the
  starter's own Javadoc, published per release.

---

## Building and Releasing

`mvn verify` builds the starter, runs its tests and the integration test under `src/it`. Build with
JDK 17 to 25, for the reason given in the [Compatibility Matrix](#compatibility-matrix).

Releases go to Maven Central through the Central Portal, from the Release workflow. Pushing a tag
releases the version it names, while `main` stays on the next `-SNAPSHOT`:

1. Update the version in the README's dependency snippet, compatibility matrix and Initializr
   entry, and in the project page (`docs/index.html`), then commit.
2. Tag and push:
   ```bash
   git tag vX.Y.Z
   git push origin vX.Y.Z
   ```
3. Once the release is on Maven Central, move `main` to the next development version:
   ```bash
   mvn versions:set -DnewVersion=X.Y+1.0-SNAPSHOT -DgenerateBackupPoms=false
   ```

The workflow runs the full build, attaches sources and Javadoc, signs everything with GPG, uploads
it (leaving out the examples module) and creates a GitHub Release. It needs the repository secrets
`MAVEN_CENTRAL_USERNAME` and `MAVEN_CENTRAL_PASSWORD` (a Central Portal user token), and
`GPG_PRIVATE_KEY` and `GPG_PASSPHRASE`. To review a release in the Central Portal before it goes
public, set the repository variable `CENTRAL_AUTO_PUBLISH` to `false`.

---

Apache Wicket, Wicket and Apache are trademarks of The Apache Software Foundation. This project is
not affiliated with or endorsed by the ASF.
