# Lab 2 Web Server -- Project Report

## What I specified

I wanted to configure a basic Spring Boot web server with a custom error page, a JSON `/time` endpoint, and secure the application using HTTP/2 and TLS. 

I specified that the TLS certificate must be explicitly valid for the local IP address (`127.0.0.1`) alongside the `localhost` DNS. I knew the implementation would be correct if accessing a non-existent URL returned my custom HTML with a 404 status, accessing `/time` returned a valid JSON with the current time, and connecting to `https://127.0.0.1:8443` successfully negotiated the HTTP/2 protocol (`ALPN: h2`).

## What I changed

- **`src/main/resources/templates/error.html`**: Created a custom HTML template using Thymeleaf and basic CSS to display a large, red error code.
- **`ErrorPageTest.kt`**: Added an integration test using `TestRestTemplate` to verify that a request to an unknown path returns a 404 status and contains my custom HTML text.
- **`TimeComponent.kt`**: Created a single file grouping the `TimeDTO`, `TimeProvider` interface, `TimeService` implementation, an extension function, and the `TimeController` to expose the `/time` endpoint.
- **`TimeControllerTest.kt`**: Added a test using `MockMvc` to verify the `/time` endpoint returns a 200 OK status and contains the `$.time` JSON path.
- **`src/main/resources/application.yml`**: Configured the server to run on port 8443, enable HTTP/2, and use a PKCS12 keystore (`localhost.p12`).
- **`src/test/resources/application.yml`**: Added this configuration file to explicitly disable SSL during tests (`server.ssl.enabled: false`).
- **`localhost.p12`**: Generated a self-signed certificate and packed it into a keystore, adding it to version control (Git).

## Technical decisions

- **Dynamic Error Page**: Instead of a static HTML file, I chose to use Thymeleaf variables (`${status}` and `${path}`) to make the error page context-aware. I added inline CSS to make the error code larger and highly visible.
- **Test Environment Isolation**: I decided to disable SSL specifically in the `test` directory's `application.yml`. If I didn't do this, `ErrorPageTest.kt` would fail after enabling HTTPS globally because `TestRestTemplate` would attempt to connect using standard HTTP instead of HTTPS.
- **Certificate SAN (Subject Alternative Name)**: To fulfill the certificate requirements, I added `-addext "subjectAltName=DNS:localhost,IP:127.0.0.1"` to the OpenSSL command. This ensures the certificate is strictly valid for the IP address, preventing strict browser blocks when testing via IP.

- **Test Implementation:** Due to unresolved classpath dependencies with standard Spring test clients (MockMvc and TestRestTemplate) in my local environment, I opted to write the integration test for /time using native Java HttpURLConnection. This ensured the server response could be validated reliably without third-party library conflicts.

## How I verified

I verified the code format by running `./gradlew ktlintFormat` and `./gradlew ktlintCheck`. I verified the logic by running `./gradlew check`.

During manual verification, I faced a major issue: running `curl -v --http2 ...` failed in Windows CMD, PowerShell, and Git Bash with the error `"the installed libcurl version does not support this"`. My local `curl` was missing the `nghttp2` library. 
To fix this and verify the server correctly negotiated `ALPN: h2`, I opened Google Chrome, navigated to `https://127.0.0.1:8443/time`, opened the Developer Tools (F12) -> Network tab, and verified that the Protocol column explicitly displayed `h2`.

I also faced OpenSSL path errors (`No such file or directory`) because I tried to pack the `.p12` file before generating the `.crt` and `.key` files. I fixed this by executing the commands in the correct chronological order.

Finally, I encountered code style violations (trailing spaces and missing newlines) when running `./gradlew check`. I resolved these formatting issues automatically by running `./gradlew ktlintFormat` before successfully passing the final build check

## AI disclosure

- **Tools / skills:** Gemini.
- **Purpose:** Brainstorming the HTML design, structuring the Kotlin code for the Time component, troubleshooting OpenSSL terminal errors, and finding alternatives for the `curl` HTTP/2 issue on Windows, resolving Kotlin test compilation issues (Unresolved reference), and fixing ktlint formatting violations.
- **Representative prompts:** 
  - "Como planteo la primera tarea? alguna idea?"
  - "Como podría hacer X o Y?"
- **Affected files/sections:** `error.html` Thymeleaf syntax, `TimeComponent.kt` structure, and OpenSSL terminal commands.
- **Validation steps:** - **Validation steps:** I personally executed `./gradlew check` in my terminal to run all the tests and verify the Ktlint format. I also manually verified the HTTP/2 protocol using the browser's Network tab.
- **Citations:** Adapted OpenSSL command structures for generating and packing PKCS12 keystores.
- **Human-reviewed:** I manually moved the `error.html` file to the correct `templates` folder after initially placing it in the wrong directory. I also rejected the AI's complex suggestion to use Docker containers to run `curl`, deciding instead that verifying the HTTP/2 protocol via the web browser was a much simpler and equally valid solution.