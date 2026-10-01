# Lab 2 Web Server -- Project Report

## What I specified

I wanted to configure a basic Spring Boot web server with a custom error page, a JSON `/time` endpoint, and secure the application using HTTP/2 and TLS. 

I specified that the TLS certificate must be explicitly valid for the local IP address (`127.0.0.1`) alongside the `localhost` DNS. I knew the implementation would be correct if accessing a non-existent URL returned my custom HTML with a 404 status, accessing `/time` returned a valid JSON with the current time, and connecting to `https://127.0.0.1:8443` successfully negotiated the HTTP/2 protocol (`ALPN: h2`).

As a step further, I specified the need to test the `/time` endpoint deterministically by injecting a fixed `TimeProvider` that returns an exact timestamp, avoiding flaky tests caused by the system clock.

## What I changed

- **`src/main/resources/templates/error.html`**: Created a custom HTML template using Thymeleaf. It started as a very simple page (big red error code and the path). It is now a dark-fantasy "You died" screen (Dark Souls / Elden Ring style) with animations. It still shows the status and the path with `th:text="${status}"` and `th:text="${path}"`.
- **`src/main/resources/static/css/error.scss`** (new): All the styles for the error page, written in SCSS.
- **`src/main/resources/static/css/_theme.scss`** (new): SCSS partial with colours, fonts, functions and mixins used by `error.scss`.
- **`build.gradle.kts` and `gradle/libs.versions.toml`**: Added the Gradle plugin `io.freefair.sass-java` (9.5.0), which compiles the SCSS to CSS during the build. The CSS is not stored in git.
- **`PaginaErrorTest.kt`**: Added an integration test using `TestRestTemplate` to verify that a request to an unknown path returns a 404 status and contains my custom HTML text. I added a second test that checks `/css/error.css` is served, which proves the SCSS was compiled.
- **`TimeComponent.kt`**: Created a single file grouping the `TimeDTO`, `TimeProvider` interface, `TimeService` implementation, an extension function, and the `TimeController` to expose the `/time` endpoint.
- **`TimeControllerTest.kt`**: Added an integration test using native `HttpURLConnection` and `URI` to verify the `/time` endpoint returns a 200 OK status. Added a second pure unit test injecting an anonymous `TimeProvider` stub to assert a fixed timestamp.
- **`src/main/resources/application.yml`**: Configured the server to run on port 8443, enable HTTP/2, and use a PKCS12 keystore (`localhost.p12`).
- **`src/test/resources/application.yml`**: Added this configuration file to explicitly disable SSL during tests (`server.ssl.enabled: false`).
- **`localhost.p12`**: Generated a self-signed certificate and packed it into a keystore, adding it to version control (Git).

## Technical decisions

- **Dynamic Error Page**: Instead of a static HTML file, I chose to use Thymeleaf variables (`${status}` and `${path}`) to make the error page context-aware. I first used inline CSS to make the error code larger and highly visible.
- **SCSS compiled by Gradle**: For the final design I chose SCSS (requirement of the redesign) compiled at build time by a Gradle plugin, instead of committing generated CSS or needing Node. The trade-off is one extra build plugin. The page also loads Google Fonts, so the fonts need internet access (it falls back to system serif fonts if not).
- **Test Environment Isolation**: I decided to disable SSL specifically in the `test` directory's `application.yml`. If I didn't do this, `ErrorPageTest.kt` would fail after enabling HTTPS globally because `TestRestTemplate` would attempt to connect using standard HTTP instead of HTTPS.
- **Certificate SAN (Subject Alternative Name)**: To fulfill the certificate requirements, I added `-addext "subjectAltName=DNS:localhost,IP:127.0.0.1"` to the OpenSSL command. This ensures the certificate is strictly valid for the IP address, preventing strict browser blocks when testing via IP.
- **Test Implementation**: Due to unresolved classpath dependencies with standard Spring test clients (`MockMvc` and `TestRestTemplate`) in my local environment, I opted to write the integration test for `/time` using native Java `HttpURLConnection`. I also used `URI("...").toURL()` to avoid Java deprecation warnings. This ensured the server response could be validated reliably without third-party library conflicts.
- **Fixed TimeProvider Test (Step further)**: To fulfill the extra requirement, I added a pure unit test. Instead of modifying the endpoint to accept a timezone, I leveraged the `TimeProvider` interface to inject an anonymous stub that returns a hardcoded `LocalDateTime`. This proves the controller logic works deterministically without loading the entire Spring context.

## How I verified

I verified the code format by running `./gradlew ktlintFormat` and `./gradlew ktlintCheck`. I verified the logic by running `./gradlew check`.

During manual verification, I faced a major issue: running `curl -v --http2 ...` failed in Windows CMD, PowerShell, and Git Bash with the error `"the installed libcurl version does not support this"`. My local `curl` was missing the `nghttp2` library. 
To fix this and verify the server correctly negotiated `ALPN: h2`, I opened Google Chrome, navigated to `https://127.0.0.1:8443/time`, opened the Developer Tools (F12) -> Network tab, and verified that the Protocol column explicitly displayed `h2`.

I also faced OpenSSL path errors (`No such file or directory`) because I tried to pack the `.p12` file before generating the `.crt` and `.key` files. I fixed this by executing the commands in the correct chronological order.

Finally, I encountered code style violations (trailing spaces, missing newlines, and incorrect import ordering) and a Java deprecation warning when running `./gradlew check`. I resolved these formatting issues automatically by running `./gradlew ktlintFormat` and updating the deprecated `URL` constructor to `URI`, successfully passing the final build check.

For the redesigned error page, `./gradlew check` passes (5 tests, including the new CSS test). I opened the page in a browser with `bootRun` and a 404 URL to see it, and I check the visual result on my own device.

## AI disclosure

### Claude (Claude Opus, via Claude Code) — error page redesign

- **Tools / skills:** Claude Opus 5.5 in Claude Code (VS Code extension).
- **Purpose:** I had a very simple version of `error.html`. I asked Claude to turn it into a spectacular dark-fantasy page using SCSS. Claude wrote the design, the animations and the SCSS, and set up the Gradle plugin to compile it.
- **Representative prompt:** "Change error.html, keep the error code and the path, make it visually spectacular with a medieval fantasy theme like Dark Souls or Elden Ring, animations welcome, and use SCSS."
- **Files added or changed by Claude:** `error.html` (rewritten), `static/css/error.scss` (new), `static/css/_theme.scss` (new), `build.gradle.kts`, `gradle/libs.versions.toml`, and one new test in `PaginaErrorTest.kt`. Claude also edited this report section.
- **Validation steps:** `./gradlew check` passes. I ran the server and looked at the page in the browser. Visual checks are done by me on my device.
- **Human-reviewed:** I decided on the theme and on using SCSS. I kept the status and path in the page as required by the lab.
- **Note:** This is a large amount of generated code for one file (it is mostly styling). The Kotlin logic (`/time`, TLS, tests for them) is not from Claude.

### Gemini — rest of the lab

- **Tools / skills:** Gemini.
- **Purpose:** Brainstorming the HTML design, structuring the Kotlin code for the Time component, troubleshooting OpenSSL terminal errors, finding alternatives for the `curl` HTTP/2 issue on Windows, resolving Kotlin test compilation issues (`Unresolved reference`), fixing `URL` constructor deprecation warnings, implementing the step-further stub test, and fixing ktlint formatting violations.
- **Representative prompts:** 
  - "Como planteo la primera tarea? alguna idea?"
  - "Los imports tienen que estar ordenados alfabeticamente"
- **Affected files/sections:** `error.html` Thymeleaf syntax, `TimeComponent.kt` structure, `TimeControllerTest.kt` test logic, and OpenSSL terminal commands.
- **Validation steps:** I personally executed `./gradlew check` in my terminal to run all the tests and verify the Ktlint format. I also manually verified the HTTP/2 protocol using the browser's Network tab.
- **Citations:** Adapted OpenSSL command structures for generating and packing PKCS12 keystores.
- **Human-reviewed:** I manually moved the `error.html` file to the correct `templates` folder after initially placing it in the wrong directory. I also rejected the AI's complex suggestion to use Docker containers to run `curl`, deciding instead that verifying the HTTP/2 protocol via the web browser was a much simpler and equally valid solution.