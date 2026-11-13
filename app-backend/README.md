# app-backend

Backend implementing REST APIs for accessing GitHub User data, and supplying to the AG Grid. 
- Entry point: `org.bootcamp.com.BackendApiApplication`. Port **8081**. 
- Base path `/api/github`. 
- Responses use `GithubUserDto`. Mapping is handwritten in `GithubUserMapper`.

### JDBC or JPA

Property `impl-type.useSqlQueries` chooses how this API talks to MariaDB tables:
- `false` uses JPA with Hibernate
  - This is the default in `local` profile.
  - **JPA** (Java Persistence API) is the Java standard for mapping a class to a table and loading rows as objects. Hibernate is the implementation this application uses. 
  - The application declares an entity, naming its table & columns. **Hibernate** creates the table & writes the objects of that entity in SQL. 
  - **Spring Data JPA** is a library dependency that provides `JpaRepository`. Hence, when a class extends this interface - it derives common CRUD operations from the entity fields. The `@Repository` annotation marks it as persistence code. 
  - However, Filters and Sorting operations are complex and the queries are built with the Criteria API.
- `true` uses JDBC
  - **JDBC** (Java Database Connectivity) is a standard Java API that lets a Java application connect to a relational database, execute SQL statements, and process the query results.
  - The **MariaDB driver** is the JDBC implementation this application uses. It is the library that speaks to the MariaDB server. **Spring JDBC** is the helper this application uses.

Hibernate runs once at startup despite the config value, because `ddl-auto: update` creates or alters the `GITHUB_USER` table.

### RESTful API endpoints

| Method | Path | Behavior |
| --- | --- | --- |
| `GET` | `/api/github/user/{username}` | 200 + DTO, or 404 |
| `DELETE` | `/api/github/delete/{username}` | 200 when a row was removed, otherwise 404. Annotated `@PreAuthorize("hasRole('admin')")`. `@EnableMethodSecurity` is not present, so that annotation is not applied. |
| `POST` | `/api/github/fetch` | Body `GithubUserFetchRequest`. Returns one page. |
| `POST` | `/api/github/user/add/{username}` | Loads the user from GitHub and inserts one row. 200 added, 404 unknown GitHub user, 409 already present. |

With profile `local`, `SecurityConfig` permits every request and disables CSRF. 

> The React app sends no bearer token, and Keycloak is not finished. Without the `local` profile the chain is an OAuth2 resource server: every request must be authenticated, and `/api/github/**` requires role `user`. The issuer is `http://localhost:8080/realms/myrealm`.

### Starting the Application

1. Export these environment variables either in the terminal session / add it in `application-local.yml`:
  - `DB_USERNAME`, `DB_PASSWORD` - MariaDB login.
  - `GITHUB_API_TOKEN` - optional. Without it, GitHub calls are unauthenticated.
2. From the repo root, with MariaDB running on `localhost:3306` and database `github` created, run this command:
    ```bash
    mvn -pl app-backend -am spring-boot:run -Dspring-boot.run.profiles=local
    ```
  - `-am` builds `data-model` first. 
  - Profile `local` loads `application-local.yml`, which sets `SQL_IMPLEMENTATION_TYPE` to `false` (JPA).
3. Check Health of the application: http://localhost:8081/actuator/health

### Swagger

SpringDoc OpenAPI UI:

- http://localhost:8081/swagger-ui/index.html
- OpenAPI document: http://localhost:8081/api-docs

`springdoc.swagger-ui.path` is `/swagger-ui.html`. 

`springdoc.api-docs.path` is `/api-docs`.

### Configuration

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
| `impl-type.useSqlQueries` | `${SQL_IMPLEMENTATION_TYPE}` | `true` selects JDBC. `false` selects JPA. See [JDBC or JPA](#jdbc-or-jpa). |
| `SQL_IMPLEMENTATION_TYPE` in `application-local.yml` | `false` | Local default is JPA. |
| `github.api.token` | `${GITHUB_API_TOKEN:}` | Optional GitHub token. Empty default. |
| `logging.level.org.bootcamp.com` | `DEBUG` | Application logs. Root is `WARN`. |

Compile settings: `java.version` is `21`, POM source/target properties say `23`, and `maven-compiler-plugin` forces source and target **21**.

### Dependencies

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

Spring Security filter chain. Profile `local` permits requests and disables CSRF so the grid can call `POST /fetch`. Any other profile authenticates every request. Session policy is not set to stateless. `@PreAuthorize` on delete has no effect until `@EnableMethodSecurity` is added.

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

### Build plugins

<details>
<summary><code>spring-boot-maven-plugin</code></summary>

Provides `mvn spring-boot:run` and the executable repackage. Version `3.3.10` comes from `spring-boot-starter-parent`. The plugin block declares no version. Profile `local` is passed on the command line with `-Dspring-boot.run.profiles=local`.

</details>

<details>
<summary><code>maven-compiler-plugin</code> 3.11.0</summary>

Forces Java source and target **21** and registers Lombok `1.18.38` as an annotation processor. That overrides the POM properties that say source and target `23`.

</details>
