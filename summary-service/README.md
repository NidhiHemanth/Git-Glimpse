# summary-service

Reactive HTTP service that stores a short text summary of a GitHub username in MongoDB. Entry point: `org.bootcamp.com.SummarizerApplication`. Port **8082**. It does not depend on `data-model` and it does not read table `GITHUB_USER`.

The grid and `app-backend` do not call this service. You do not start it to run the UI.

`GET /api/summarize/user/{username}` returns `Mono<ResponseEntity<String>>`. `SummarizeServiceImpl.constructPrompt` loads up to 20 repos and 10 starred repos from GitHub. `getAiSummary` POSTs that text to the Hugging Face inference URL for `facebook/bart-large-cnn`, using `openai.api.key` as the bearer token. `Constants.OPEN_AI_API_URL` is unused. If `OPEN_AI_API_KEY` is empty the method returns `Optional.empty()` and the controller responds 400.

The current save path does not store the model text. `summarizeGithubUser` returns `Optional.of(username)`. `UserReactiveServiceImpl.saveUser` persists `new UserSummary(username, "Summary: " + username)`.

The Mongo document is `UserSummary` (`username`, `summary`).

## Start

Only when you intend to work on this process. MongoDB must be on `localhost:27017` with database `github` and `authSource=admin`.

From the repo root:

```bash
mvn -pl summary-service spring-boot:run -Dspring-boot.run.profiles=local
```

This module does not depend on `data-model`, so `-am` is unnecessary. `application-local.yml` is empty. Profile `local` adds no keys.

Environment variables:

- `DB_USERNAME`, `DB_PASSWORD` — MongoDB login inside the connection URI. No default.
- `GITHUB_API_TOKEN` — optional. Empty default.
- `OPEN_AI_API_KEY` — required before the summarizer will call the model. Empty default returns 400.

Health: http://localhost:8082/actuator/health

## Swagger

- http://localhost:8082/swagger-ui/index.html
- OpenAPI document: http://localhost:8082/api-docs

`springdoc.swagger-ui.path` is `/swagger-ui.html`. `springdoc.api-docs.path` is `/api-docs`.

## Configuration

`src/main/resources/application.yml`.

| Key | Value | What it does |
| --- | --- | --- |
| `server.port` | `8082` | HTTP port. |
| `spring.application.name` | `Summarize-API` | Process name. |
| `spring.data.mongodb.uri` | `mongodb://${DB_USERNAME}:${DB_PASSWORD}@localhost:27017/github?authSource=admin` | Reactive MongoDB connection. Host, port, database, username, and password fields are present as comments and are not active. |
| `management.endpoints.web.exposure.include` | `health, info` | Exposed Actuator endpoints. |
| `management.endpoint.health.show-details` | `always` | Health payload includes detail. |
| `springdoc.api-docs.path` | `/api-docs` | OpenAPI JSON path. |
| `springdoc.swagger-ui.path` | `/swagger-ui.html` | Swagger UI entry. |
| `github.api.token` | `${GITHUB_API_TOKEN:}` | Optional GitHub token. |
| `openai.api.key` | `${OPEN_AI_API_KEY:}` | Bearer token for the Hugging Face call. The property name says OpenAI. The code posts to the Hugging Face bart-large-cnn URL. |
| `logging.level.org.bootcamp.com` | `DEBUG` | Application logs. Root is `WARN`. |

Compile settings: `java.version` is `21`, POM source/target properties say `23`, and `maven-compiler-plugin` forces source and target **21**.

## Dependencies

`spring-boot-starter-data-mongodb` (the blocking driver) is commented out in `pom.xml`. The active Mongo stack is the reactive starter.

<details>
<summary><code>spring-boot-starter-web</code></summary>

Servlet Spring MVC and an embedded Tomcat. `SummarizeUserController` is a `@RestController` on this stack. The method return type is Reactor `Mono`, so the reactive types come from `spring-boot-starter-webflux` as well. Both starters are on the classpath.

</details>

<details>
<summary><code>spring-boot-starter-validation</code></summary>

Jakarta Bean Validation. Declared for constraint annotations. The summarize endpoint takes a path variable and does not bind a request body.

</details>

<details>
<summary><code>springdoc-openapi-starter-webmvc-ui</code> 2.4.0</summary>

OpenAPI and Swagger UI for the servlet stack. Version pinned. UI: http://localhost:8082/swagger-ui/index.html.

</details>

<details>
<summary><code>spring-boot-starter-data-mongodb-reactive</code></summary>

Spring Data MongoDB with the reactive driver. `SummarizerRepository` and `UserSummary` use this. The connection URI is `spring.data.mongodb.uri`. This service does not use MariaDB.

</details>

<details>
<summary><code>spring-boot-starter-webflux</code></summary>

Project Reactor and WebClient. `Mono` on the controller, and the outbound call that posts the prompt, use this stack.

</details>

<details>
<summary><code>lombok</code> (optional)</summary>

`@Slf4j` and the usual boilerplate annotations. Optional, so it is not transitive. The compiler plugin runs Lombok `1.18.38` as an annotation processor.

</details>

<details>
<summary><code>spring-boot-starter-test</code> (test)</summary>

Spring Boot test starter. Scope `test`. No `*Test.java` sources are in this module.

</details>

<details>
<summary><code>reactor-test</code> (test)</summary>

`StepVerifier` and other Reactor test helpers. Scope `test`. Present so a future test can assert `Mono` results.

</details>

<details>
<summary><code>testcontainers</code> artifact <code>mongodb</code> (test)</summary>

Testcontainers module that starts a MongoDB container for tests. Scope `test`. No test class launches it yet.

</details>

## Build plugins

<details>
<summary><code>spring-boot-maven-plugin</code></summary>

Provides `mvn spring-boot:run` and the executable repackage. Version `3.3.10` comes from `spring-boot-starter-parent`. The plugin block declares no version.

</details>

<details>
<summary><code>maven-compiler-plugin</code> 3.11.0</summary>

Forces Java source and target **21** and registers Lombok `1.18.38` as an annotation processor. That overrides the POM properties that say source and target `23`.

</details>
