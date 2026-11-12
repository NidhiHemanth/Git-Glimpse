# summary-service

Reactive HTTP service that spins up a short summary of a given GitHub user and persists it in MongoDB. 
- Entry point: `org.bootcamp.com.SummarizerApplication`. 
- Port **8082**. 

> `summary-service` is not currently in use. Nothing in the grid or the API calls it.

### API: `GET /api/summarize/user/{username}`

Takes a `username` as input and returns `Mono<ResponseEntity<String>>` containing the user's GitHub profile summary. 
- `SummarizeServiceImpl.constructPrompt` loads up to 20 repos and 10 starred repos for the given user from GitHub. 
- `getAiSummary` POSTs that text to the Hugging Face inference URL for `facebook/bart-large-cnn`, using `openai.api.key` as the bearer token.
- The summary is returned as the API response.

The current save path does not store the model text. Ideally `UserReactiveServiceImpl.saveUser` persists `new UserSummary(username, "Summary: " + username)` into the Mongo document.

### Starting the Application

Environment variables to be set in the terminal session using `export VAR_NAME=VAR_VALUE`:
- `DB_USERNAME`, `DB_PASSWORD` — MongoDB login inside the connection URI. No default.
- `GITHUB_API_TOKEN` — optional. Empty default.
- `OPEN_AI_API_KEY` — required before the summarizer will call the model. Empty default returns 400.

1. MongoDB service must be running on `localhost:27017` with database `github` and `authSource=admin`. 
2. From the repo root, run the command
    ```bash
    mvn -pl summary-service spring-boot:run -Dspring-boot.run.profiles=local
    ```
    - This module does not depend on `data-model`, so `-am` is unnecessary. 
    - Currently `application-local.yml` is empty. Profile `local` adds no keys.
3. Verify the application is up with http://localhost:8082/actuator/health.

### Swagger

- http://localhost:8082/swagger-ui/index.html
- OpenAPI document: http://localhost:8082/api-docs

`springdoc.swagger-ui.path` is `/swagger-ui.html`. `springdoc.api-docs.path` is `/api-docs`.

### Configuration

`src/main/resources/application.yml`

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

### Dependencies

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

### Build plugins

<details>
<summary><code>spring-boot-maven-plugin</code></summary>

Provides `mvn spring-boot:run` and the executable repackage. Version `3.3.10` comes from `spring-boot-starter-parent`. The plugin block declares no version.

</details>

<details>
<summary><code>maven-compiler-plugin</code> 3.11.0</summary>

Forces Java source and target **21** and registers Lombok `1.18.38` as an annotation processor. That overrides the POM properties that say source and target `23`.

</details>
