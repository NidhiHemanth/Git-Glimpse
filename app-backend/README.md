# app-backend

REST API for the AG Grid. Entry point: `org.bootcamp.com.BackendApiApplication`. Port **8081**. Base path `/api/github`. Controller: `GithubUserController`. Responses use `GithubUserDto`. Mapping is hand-written in `GithubUserMapper`.

The same operations exist twice. `ApplicationProperties.useSqlQueries` (`impl-type.useSqlQueries`, bound from `SQL_IMPLEMENTATION_TYPE`) chooses the path:

- `false` (local profile): Spring Data JPA. `GithubJpaRepository` plus `GithubJpaRepositoryImpl` (Criteria API). That class name and the `Impl` suffix are required. The class is not annotated `@Repository`.
- `true`: `NamedParameterJdbcTemplate` in `GithubJdbcRepositoryImpl`, SQL assembled by `SqlQueryBuilder`.

Infinite scroll is offset paging. AG Grid sends `endRow` as an exclusive index (`cacheBlockSize` is 10). JDBC uses `OFFSET start ROWS FETCH NEXT (end - start) ROWS ONLY`. JPA uses `setFirstResult(startRow)` and `setMaxResults(endRow - startRow + 1)`.

Add and delete are GET on purpose. The React client calls those paths.

| Method | Path | Behavior |
| --- | --- | --- |
| `GET` | `/api/github/user/{username}` | 200 + DTO, or 404 |
| `GET` | `/api/github/delete/{username}` | 200 when a row was removed, otherwise 404. Annotated `@PreAuthorize("hasRole('admin')")`. `@EnableMethodSecurity` is not present, so that annotation is not applied. |
| `POST` | `/api/github/fetch` | Body `GithubUserFetchRequest`. Returns one page. |
| `GET` | `/api/github/user/add/{username}` | Loads the user from GitHub and inserts one row. 200 added, 404 unknown GitHub user, 409 already present. |

`addUser` calls `https://api.github.com` for the profile, the first page of followers, the first page of following, and every repo page (`per_page=100`). Follower and following counts are the length of that first page. The most-starred repo is chosen in memory.

`SecurityConfig` is a servlet OAuth2 resource server. It authenticates every request and requires role `user` on `/api/github/**`. The issuer is `http://localhost:8080/realms/myrealm`. The React app sends no bearer token. Enforcing these matchers blocks the grid until the client sends a token and CORS still allows `http://localhost:3000`.

## Start

From the repo root, with MariaDB on `localhost:3306` and database `github` created:

```bash
mvn -pl app-backend -am spring-boot:run -Dspring-boot.run.profiles=local
```

`-am` builds `data-model` first. Profile `local` loads `application-local.yml`, which sets `SQL_IMPLEMENTATION_TYPE` to `false` (JPA).

Environment variables with no default in `application.yml`:

- `DB_USERNAME`, `DB_PASSWORD` — MariaDB login.
- `SQL_IMPLEMENTATION_TYPE` — `true` or `false`, unless profile `local` is active.
- `GITHUB_API_TOKEN` — optional. Without it, GitHub calls are unauthenticated.

Health: http://localhost:8081/actuator/health

## Swagger

Springdoc OpenAPI UI:

- http://localhost:8081/swagger-ui/index.html
- OpenAPI document: http://localhost:8081/api-docs

`springdoc.swagger-ui.path` is `/swagger-ui.html`. `springdoc.api-docs.path` is `/api-docs`.

## Configuration

`src/main/resources/application.yml`. Profile `local` adds `application-local.yml`.

| Key | Value | What it does |
| --- | --- | --- |
| `server.port` | `8081` | HTTP port. |
| `spring.application.name` | `Backend-API` | Process name. |
| `spring.jpa.hibernate.ddl-auto` | `update` | Hibernate updates `GITHUB_USER` from the entity. No migration tool. |
| `spring.jpa.show-sql` | `true` | Logs SQL. The local profile also sets `org.hibernate.sql` to `DEBUG`. |
| `spring.datasource.url` | `jdbc:mariadb://localhost:3306/github?...` | Database `github`, charset `utf8mb4`. |
| `spring.datasource.username` / `password` | `${DB_USERNAME}` / `${DB_PASSWORD}` | Required environment variables. |
| `spring.datasource.hikari.maximum-pool-size` | `10` | Hikari pool size. |
| `spring.security.oauth2.resourceserver.jwt.issuer-uri` | `http://localhost:8080/realms/myrealm` | Keycloak realm that must sign access tokens. |
| `spring.security.oauth2.client.registration.keycloak` | client id `backend-api`, authorization code | Client registration for Keycloak. `application.yml` holds the client secret. Do not copy that value into docs, logs, or new config. |
| `management.endpoints.web.exposure.include` | `health, info` | Actuator endpoints that are exposed. |
| `management.endpoint.health.show-details` | `always` | Health payload includes detail. |
| `impl-type.useSqlQueries` | `${SQL_IMPLEMENTATION_TYPE}` | `true` selects JDBC. `false` selects JPA. |
| `SQL_IMPLEMENTATION_TYPE` in `application-local.yml` | `false` | Local default is JPA. |
| `github.api.token` | `${GITHUB_API_TOKEN:}` | Optional GitHub token. Empty default. |
| `logging.level.org.bootcamp.com` | `DEBUG` | Application logs. Root is `WARN`. |

The service logs `PROCESSING … using JDBC Template` or `using JPA Repository` for the repository path that ran.

Compile settings: `java.version` is `21`, POM source/target properties say `23`, and `maven-compiler-plugin` forces source and target **21**.

## Dependencies

<details>
<summary><code>data-model</code> 1.0-SNAPSHOT</summary>

Shared `GithubUser` entity. The API persists that same type. `GithubRepository` in `data-model` is the batch writer's repository. This module uses its own `GithubJpaRepository`. The two repositories are separate.

</details>

<details>
<summary><code>spring-boot-starter-web</code></summary>

Servlet stack and Spring MVC. Provides the embedded Tomcat on port 8081, `@RestController`, and Jackson. `GithubUserController` and the request DTOs sit on this stack. GitHub API payloads are Java records with snake_case components so Jackson binds GitHub's JSON without a naming strategy.

</details>

<details>
<summary><code>spring-boot-starter-actuator</code></summary>

Health and info endpoints. `management.endpoints.web.exposure.include` limits exposure to `health` and `info`. Health is http://localhost:8081/actuator/health.

</details>

<details>
<summary><code>spring-boot-starter-jdbc</code></summary>

`JdbcTemplate` / `NamedParameterJdbcTemplate` for the `SQL_IMPLEMENTATION_TYPE=true` path. `SqlQueryBuilder`, `StringSqlBuilderUtil`, and `NumericSqlBuilderUtil` build that SQL. Filter values are concatenated into the SQL text. New query code should use bind parameters.

</details>

<details>
<summary><code>spring-boot-starter-data-jpa</code></summary>

Spring Data JPA and Hibernate for the `useSqlQueries=false` path, and for `ddl-auto: update`. `GithubJpaRepositoryImpl` builds Criteria queries. Sorting or filtering `mostStarredProjectName` through the Criteria API does not resolve to `mostStarredProject.projectName`, because that name is a DTO field.

</details>

<details>
<summary><code>spring-boot-starter-validation</code></summary>

Jakarta Bean Validation for request and entity constraints.

</details>

<details>
<summary><code>springdoc-openapi-starter-webmvc-ui</code> 2.4.0</summary>

Generates the OpenAPI document and the Swagger UI for the servlet stack (Spring Boot 3). Version is pinned because it is not part of the Spring Boot BOM. UI: http://localhost:8081/swagger-ui/index.html. Document path: `/api-docs`.

</details>

<details>
<summary><code>spring-boot-starter-security</code></summary>

Spring Security filter chain. `SecurityConfig` authenticates every request. CSRF is not disabled. Session policy is not set to stateless. `@PreAuthorize` on delete has no effect until `@EnableMethodSecurity` is added.

</details>

<details>
<summary><code>spring-boot-starter-oauth2-resource-server</code></summary>

Validates bearer JWTs against the Keycloak issuer in `issuer-uri`. The React app does not send a bearer token.

</details>

<details>
<summary><code>spring-security-oauth2-jose</code></summary>

JOSE support (JWT, JWS, JWK) used by the resource server to decode and verify tokens. Version comes from the Spring Boot parent BOM.

</details>

<details>
<summary><code>mariadb-java-client</code> 2.7.0</summary>

JDBC driver for `jdbc:mariadb://localhost:3306/github`. Version pinned in this POM. Both the JPA path and the JDBC path use this driver.

</details>

<details>
<summary><code>lombok</code> (optional)</summary>

`@Data`, `@Builder`, `@Slf4j`, and related annotations. Optional, so it is not transitive. The compiler plugin runs Lombok `1.18.38` as an annotation processor.

</details>

<details>
<summary><code>guava</code> 33.4.8-jre</summary>

Google Guava, pinned to the JRE variant. No class under `app-backend/src` imports `com.google.common`. The jar is still on the compile and runtime classpath.

</details>

<details>
<summary><code>spring-boot-starter-test</code> (test)</summary>

JUnit and Spring Boot test slices. Scope `test`. There is no `*Test.java` in this module. The committed request fixture is `src/test/resources/data/GithubFetchRequest.json`.

</details>

## Build plugins

<details>
<summary><code>spring-boot-maven-plugin</code></summary>

Provides `mvn spring-boot:run` and the executable repackage. Version `3.3.10` comes from `spring-boot-starter-parent`. The plugin block declares no version. Profile `local` is passed on the command line with `-Dspring-boot.run.profiles=local`.

</details>

<details>
<summary><code>maven-compiler-plugin</code> 3.11.0</summary>

Forces Java source and target **21** and registers Lombok `1.18.38` as an annotation processor. That overrides the POM properties that say source and target `23`.

</details>
