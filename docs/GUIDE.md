---
title: "Web Engineering 2026-2027"
subtitle: "Lab 2: Web Server"
date: "2026-09-28"
format:
  html:
    toc: true
    toc-depth: 3
    number-sections: true
    code-fold: true
    code-tools: true
    code-overflow: wrap
    theme: cosmo
    css: |
      body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; }
      .quarto-title-block { border-bottom: 2px solid #2c3e50; padding-bottom: 1rem; }
      h1, h2, h3 { color: #2c3e50; }
      pre { background-color: #f8f9fa; border-left: 4px solid #007acc; }
      .callout { border-left: 4px solid #28a745; }
      pre, code { white-space: pre-wrap; word-break: break-word; overflow-wrap: anywhere; }
  pdf:
    documentclass: article
    classoption: [11pt, a4paper]
    toc: true
    toc-depth: 3
    number-sections: true
    geometry: [margin=2.5cm, headheight=15pt]
    fontsize: 11pt
    linestretch: 1.15
    colorlinks: true
    breakurl: true
    urlcolor: blue
    linkcolor: blue
    citecolor: blue
    hyperrefoptions:
      - linktoc=all
      - bookmarksnumbered=true
      - bookmarksopen=true
    header-includes:
      - |
        \usepackage{helvet}
        \renewcommand{\familydefault}{\sfdefault}
        \usepackage{hyperref}
        \usepackage{fancyhdr}
        \pagestyle{fancy}
        \fancyhf{}
        \fancyhead[L]{Web Engineering 2026-2027}
        \fancyhead[R]{Lab 2: Web Server}
        \fancyfoot[C]{\thepage}
        \renewcommand{\headrulewidth}{0.4pt}
        \usepackage{microtype}
        \usepackage{booktabs}
        \usepackage{array}
        \usepackage{longtable}
        \usepackage{xcolor}
        \definecolor{sectioncolor}{RGB}{44,62,80}
        \usepackage{sectsty}
        \allsectionsfont{\color{sectioncolor}}
        \usepackage{fvextra}
        \fvset{breaklines=true, breakanywhere=true}
        \DefineVerbatimEnvironment{Highlighting}{Verbatim}{breaklines,breakanywhere,commandchars=\\\{\}}
lang: en-GB
---

Second lab of 2026--2027. Command line is enough; **VS Code**, **IntelliJ IDEA**, **Eclipse**, or **GitHub Codespaces** are fine. You need **Java 25 LTS** (same pin as the group project) and **OpenSSL**.

## Requirements

- [Kotlin 2.4.0](https://kotlinlang.org/) on **Java 25 LTS**. Do not downgrade the toolchain.
- [Gradle 9.6.0](https://gradle.org/) via the wrapper (`./gradlew`).
- [Spring Boot 4.1.0](https://docs.spring.io/spring-boot/) (Spring Framework 7). Use `spring-boot-starter-webmvc`, not the Boot 3 name `spring-boot-starter-web`.
- **Thymeleaf** for the error page.
- **OpenSSL** for the self-signed certificate.

## Git and clone

1. Install [git](https://git-scm.com/).
2. GitHub education account: real name, university email.
3. Configure Git:

   ```bash
   git config --global user.name "Your Real Name"
   git config --global user.email "your_nip@unizar.es"
   ```

4. Authenticate with GitHub so you can clone this starter if Git asks. HTTPS is enough: [`gh auth login`](https://docs.github.com/en/github-cli/github-cli/quickstart) or Git Credential Manager. Do not use an account password. [Set up Git](https://docs.github.com/en/get-started/git-basics/set-up-git).

```bash
git clone \
  https://github.com/UNIZAR-30246-WebEngineering/lab2-web-server.git
cd lab2-web-server
./gradlew check
./gradlew bootRun
```

Open <http://localhost:8080>. The starter has no pages yet, so `/` is the whitelabel error until you replace it.

### Codespaces (optional)

You can do the lab in [GitHub Codespaces](https://docs.github.com/en/codespaces) instead of a local JDK. Forward port **8080**, and **8443** after the TLS task.

You cannot push to the course repository. If you need a remote of your own (Codespaces persistence), **fork** first and open the Codespace from that fork. A fork is not a submission: delivery is the Moodle zip only.

## Objective

Complete the three tasks below. Document them in **REPORT.md**. Kotlin: [KDoc](https://kotlinlang.org/docs/kotlin-doc.html).

The starter is a blank Spring Boot application. You add the behaviour.

The focus is the **server**: the error view, the `/time` response, and TLS with HTTP/2 on the embedded container. A static page with no server behaviour does not meet the objective.

### Oral defence (all students)

An oral defence is **possible for everyone**, not only for the bonus. The teacher may ask you to explain this lab in person.

You are expected to **know** the submission and to **justify all decisions** in it: what you specified, what you changed, what you rejected, how you verified it, and any AI use. If you cannot explain a choice, you do not own it.

### 1. Customize the whitelabel error page

When the application has no handler for a request, Spring Boot shows a default whitelabel error page. Replace that page.

1. Create `error.html` with your own content.
2. Save it in `src/main/resources/templates`. Spring Boot uses this Thymeleaf template for errors when the client accepts HTML.
3. Add a test that requests an unknown path with `Accept: text/html` and checks for your content and status `404`. Use a running server (`@SpringBootTest(webEnvironment = RANDOM_PORT)`). `MockMvc` records the `404` and an empty body; it does not render `error.html`. For that test, override `server.ssl.enabled` to `false` in `src/test/resources/application.yml` so the test does not need the keystore.

### 2. Add `/time`

Return the current server time as JSON.

`/time` is an HTTP response from this process. Designing resource APIs is Lab 3.

1. Create `TimeComponent.kt` in `es.unizar.webeng.lab2`.

2. Define a DTO:

   ```kotlin
   import java.time.LocalDateTime

   data class TimeDTO(val time: LocalDateTime)
   ```

3. Create a time provider:

   ```kotlin
   interface TimeProvider {
       fun now(): LocalDateTime
   }
   ```

4. Implement it:

   ```kotlin
   import org.springframework.stereotype.Service

   @Service
   class TimeService : TimeProvider {
       override fun now(): LocalDateTime = LocalDateTime.now()
   }
   ```

5. Add an extension:

   ```kotlin
   fun LocalDateTime.toDTO(): TimeDTO = TimeDTO(time = this)
   ```

6. Add a controller:

   ```kotlin
   import org.springframework.web.bind.annotation.GetMapping
   import org.springframework.web.bind.annotation.RestController

   @RestController
   class TimeController(private val service: TimeProvider) {
       @GetMapping("/time")
       fun time(): TimeDTO = service.now().toDTO()
   }
   ```

7. Add a test that `GET /time` returns `200` and a JSON `time` field. Use the same Boot 4 `AutoConfigureMockMvc` as the error-page test.

Jackson is auto-configured by `spring-boot-starter-webmvc`. With `tools.jackson.module:jackson-module-kotlin` on the classpath, Kotlin data classes such as `TimeDTO` serialize correctly.

Inject `TimeProvider` so a test can supply a fixed clock instead of `LocalDateTime.now()`.

### 3. Enable HTTP/2 and TLS

Turn on HTTP/2 over TLS with a self-signed certificate.

This lab is **`h2`**: HTTP/2 negotiated inside TLS with ALPN, which is what browsers use. Cleartext HTTP/2 (`h2c`) is not the task.

Embedded Tomcat terminates TLS inside this one process. The usual deployment from the web-server lecture puts a gateway in front of the application server and terminates TLS there. This lab is the single process, not that edge.

The certificate is self-signed, so there is no CA. `curl -k` skips that missing trust. That is not the TLS trust model from the protocols lecture.

1. **Write an OpenSSL config** in the project root, `openssl-localhost.cnf`:

   ```ini
   [req]
   distinguished_name = dn
   x509_extensions = EXT
   prompt = no

   [dn]
   CN = localhost

   [EXT]
   subjectAltName = DNS:localhost
   keyUsage = digitalSignature
   extendedKeyUsage = serverAuth
   ```

2. **Create the certificate and key:**

   ```bash
   openssl req -x509 -newkey rsa:2048 -nodes -sha256 \
     -keyout localhost.key -out localhost.crt \
     -config openssl-localhost.cnf
   ```

   *Windows:* use WSL or [OpenSSL for Windows](https://wiki.openssl.org/index.php/Binaries) if `openssl` is not on `PATH`.

3. **Pack a PKCS12 keystore.** Remember the export password (the guide uses `secret`):

   ```bash
   openssl pkcs12 -export \
     -in localhost.crt -inkey localhost.key \
     -name localhost -out localhost.p12 \
     -passout pass:secret
   mv localhost.p12 src/main/resources/
   ```

   `localhost.crt`, `localhost.key`, and `openssl-localhost.cnf` are gitignored. **`localhost.p12` is not.** `git add` the keystore. Only files under git control are reviewed.

4. **Configure Boot.** Create `src/main/resources/application.yml`:

   ```yaml
   server:
     port: 8443
     ssl:
       enabled: true
       key-store: classpath:localhost.p12
       key-store-password: "secret"
       key-store-type: PKCS12
     http2:
       enabled: true
   ```

   The property name is `server.ssl` because that is Spring Boot’s name. The protocol you are configuring is TLS.

   After this file exists, `./gradlew bootRun` listens on **8443**, not 8080.

### Manual verification

If `curl` does not negotiate HTTP/2, check `curl -V` for `nghttp2`. Force HTTP/2 with `curl --http2`.

```bash
./gradlew bootRun
```

Custom error page (expect `HTTP/2` and `404`, and your HTML):

```bash
curl --http2 -k -H "Accept: text/html" -i https://127.0.0.1:8443/
```

`/time` (expect `HTTP/2`, `200`, and a JSON body with `time`):

```bash
curl --http2 -k -i https://127.0.0.1:8443/time
```

`-k` ignores the missing CA. `-i` prints the status line, where you should see `HTTP/2`.

### Code quality

[Ktlint](https://pinterest.github.io/ktlint/) is on the build. Format before you submit:

```bash
./gradlew ktlintFormat
./gradlew ktlintCheck
```

### What to document

English, at least B1. Cover what **you** added: the error page, `/time`, TLS, and how you tested them. Commands in the report must actually work.

### Bonus

The bonus is optional. The bonus is **+0.05** on factor **k** (labs; cap **+0.10** across the six labs).

It is not extra work you add at submit time. Sequence:

1. The **student proposes** the extra (what you will do, why it is outstanding, how git history will stay readable). Propose during the lab window, **before** you treat that extra as bonus work.
2. The **instructor accepts** — or refuses if the proposal is too large, off-scope, or late.
3. **Only then** may that extra be done as bonus work.

Examples you may propose (they are not required, and “being first” is not the bonus): another embedded server (Jetty or Undertow) with HTTP/2 still working; response compression; content negotiation on `/time`; CORS; RFC 9457 errors instead of `error.html`; OpenAPI; structured logging; profiles; an automated HTTP/2 test.

After acceptance implement what was agreed, with tests and documentation another student could follow, then defend it in person.

Git is **mandatory** for the bonus: imperative commit subjects, small logical commits, feature branches, no dump-everything commits. `git rebase -i` is allowed to tidy *your* unpublished history; do not rewrite commits already in a zip you submitted.

Those commits must be in the Moodle zip’s `.git`. You do not push to the course repository.

## Finish and submit

Keep the **REPORT.md** headings. It is an engineering note (specified, changed, verified, AI), not a restatement of the starter README.

**Only files under git control are reviewed.** Commit what you want evaluated, including `localhost.p12` and `application.yml`. Untracked files do not count.

The Moodle zip must include the **`.git` directory** (the whole repository, not only the working tree).

```bash
./gradlew check
./gradlew clean
zip -r lab2-web-server-submission.zip . \
  -x ".gradle/*" ".kotlin/*" "build/*" ".idea/*" "*.iml" ".DS_Store"
```

Do not zip Gradle/Kotlin caches, `build/`, or IDE files.

Deadline **9 October 2026, 23:59** (one week after the last Lab 2 session). Submit **only on Moodle**: the zip after `./gradlew clean`, **with `.git`**. There is no GitHub delivery.

### REPORT.md

Do **not** delete the headings. Do **not** report a percentage of “AI vs original lines”.

1. **What I specified** — the tasks *before* generating or pasting code, and how you would know they work
2. **What I changed** — files and behaviour
3. **Technical decisions** — choices you own (and what you rejected)
4. **How I verified** — `./gradlew check` and the `curl` checks, what failed first, what you fixed
5. **AI disclosure** — the [AI use](#ai-use) table, **or** **No AI assistance**

**Thin (fails):** `Tools: ChatGPT. Used for: the assignment.`

**Adequate:** named tool; one concrete purpose; a prompt or two; files touched; `./gradlew check` plus what you changed after the model’s draft.

## AI use

The group project scores **AI use (10%)**: GenAI and agents to boost delivery, with disclosure you can defend. This lab is where you **practise that disclosure**. Lab 2 itself stays **limited**: assistive GenAI only — **not** a full or substantial generated solution. You do **not** need `AGENTS.md` or a skill here.

If you cannot explain it without the tool, you do not own it. This course grades **web engineering judgment** (specify, design, verify).

### Allowed (assistive)

- Brainstorming and outlines
- Boilerplate and scaffolding
- Debugging help; refactoring and lint fixes
- Test skeletons
- Grammar and style on **REPORT.md**

### Not allowed

- A full or substantial GenAI solution for this individual lab
- Fabricated results, tests, or citations
- Prompts meant to hide assistance
- Pasting exam, personal, or third-party confidential / proprietary material into tools. Your **own** lab in a local IDE agent / Copilot is allowed.

### Minimum if you used AI

Same fields as the project **AI use** slice. Fill them in **REPORT.md**. “I used ChatGPT” is not enough.

| Field | What to write |
| --- | --- |
| **Tools / skills** | Named GenAI and agent tools |
| **Purpose** | What you asked the tool to do (a step, not “do the lab”) |
| **Representative prompts** | Short examples, or an appendix |
| **Affected files/sections** | Where assistance landed |
| **Validation steps** | What you ran (`./gradlew check`, `curl`) and what you changed |
| **Citations** | External snippets you adapted |
| **Human-reviewed** | What you kept, edited, or rejected |

If you used no AI, write **No AI assistance**.

## Insights

- **TLS** encrypts the connection. This lab uses a self-signed certificate so you can see `h2` without a CA. Production trust is a different problem.
- **HTTP/2** on this server is multiplexing and HPACK header compression on one TCP connection. Server push is off in current browsers; do not treat it as a feature you are enabling.
- **`TimeProvider`** is the seam that lets a test freeze the clock.
- **`toDTO()`** is an extension function: new behaviour on `LocalDateTime` without a subclass.
