# AGENTS.md

Git Glimpse is a bootcamp full-stack app: a React AG Grid UI over a Spring Boot API, with GitHub user rows stored in MariaDB. A separate Spring Batch job loads the seed JSON. A separate reactive service summarizes a GitHub user and stores that summary in MongoDB.

`ReadMe.md` is the human setup guide and includes a future design (Keycloak roles, follow/unfollow, Kafka, notifications). This file is the working map of the code that exists today. `Terminologies.md` and `SpringBoot.md` are study notes, not runtime contracts.

## Repository map

Maven reactor root is `pom.xml` (`org.bootcamp.com:Bootcamp`, packaging `pom`). It aggregates four modules. Those modules do **not** inherit from the root POM. Each Spring Boot module parents `spring-boot-starter-parent` **3.3.10** on its own. `app-frontend` is not a Maven module.

| Module | Role | Entry point | Port |
| --- | --- | --- | --- |
| `data-model` | JPA entity, repository, and save service shared by ingest and the API. Library only. | — | — |
| `data-ingest` | Non-web Spring Batch job. Reads classpath JSON, validates, writes MariaDB. | `org.bootcamp.com.BatchProcessingApplication` | none |
| `app-backend` | REST API used by the grid. JDBC and JPA implementations of the same operations. | `org.bootcamp.com.BackendApiApplication` | 8081 |
| `summary-service` | Reactive MongoDB summary API. Does not depend on `data-model`. | `org.bootcamp.com.SummarizerApplication` | 8082 |
| `app-frontend` | Create React App + TypeScript + AG Grid. | `src/index.tsx` | 3000 |

Java package for all backend code is `org.bootcamp.com`.

## Runtime dependencies

- **Java 21.** Module POMs set `maven.compiler.source` / `target` to 23 and `java.version` to 21, but each `maven-compiler-plugin` configuration forces source and target **21**. Compile and run with JDK 21. Changing only the root POM does not change a module.
- **MariaDB** on `localhost:3306`, database name `github`, charset `utf8mb4` / `utf8mb4_unicode_ci`. Hibernate `ddl-auto` is `update` in both `app-backend` and `data-ingest`. There is no Flyway/Liquibase changelog.
- **MongoDB** on `localhost:27017`, database `github`, `authSource=admin`, only for `summary-service`.
- **GitHub REST API** when adding a user or building a summary. Optional `GITHUB_API_TOKEN`. Without it, calls are unauthenticated.
- **Keycloak** is wired in config (`http://localhost:8080/realms/myrealm`) and is not finished. See Security.

Required environment variables, with no defaults in `application.yml`:

- `DB_USERNAME`, `DB_PASSWORD` for MariaDB and, on the summary service, MongoDB.
- `SQL_IMPLEMENTATION_TYPE` (`true` or `false`) for `app-backend`, unless profile `local` is active. `application-local.yml` sets it to `false`.
- `INPUT_DIR` for `data-ingest`, unless profile `local` is active. `application-local.yml` sets it to `data`.
- `GITHUB_API_TOKEN` optional. `OPEN_AI_API_KEY` required before the summarizer will call the model.

Activate the local profile when you start a Spring process: `spring.profiles.active=local`.

`app-backend` `application.yml` contains a Keycloak client secret. Do not copy that value into docs, logs, commits, or new config. Do not rotate or rewrite it unless the task is specifically about credentials.

## Build and run

From the repo root:

```bash
mvn -pl data-model,data-ingest,app-backend,summary-service test
mvn -pl app-backend -am spring-boot:run -Dspring-boot.run.profiles=local
mvn -pl summary-service spring-boot:run -Dspring-boot.run.profiles=local
```

`data-ingest` does not declare `spring-boot-maven-plugin`. Launch it with the plugin coordinate:

```bash
mvn -pl data-ingest -am org.springframework.boot:spring-boot-maven-plugin:3.3.10:run -Dspring-boot.run.profiles=local
```

The batch app sets `WebApplicationType.NONE`, disables Spring Boot’s automatic job launch (`spring.batch.job.enabled=false`), and starts `importGithubUsers` from a `CommandLineRunner`. On success the job listener calls `System.exit(0)`.

Frontend, from `app-frontend`:

```bash
npm install
npm start
npm test -- --watchAll=false
npm run build
```

There is no dev-server proxy. `src/api/axios.ts` calls `http://localhost:8081/api/github` directly. The API allows CORS from `http://localhost:3000` only (`@CrossOrigin` on `GithubUserController`).

Swagger: `http://localhost:8081/swagger-ui/index.html` and `http://localhost:8082/swagger-ui/index.html`. Actuator health is exposed on both APIs (`/actuator/health`).

## What to change where

- A column on a stored GitHub user belongs on `data-model` `GithubUser` / embedded `GithubProject`. Ingest and `app-backend` both persist that entity. After an entity change, update `GithubUserMapper` (entity, DTO, `ResultSet`, insert parameter map), `Constants.TABLE_COLUMNS`, and the grid column defs together.
- `summary-service` has its own `UserSummary` Mongo document (`username`, `summary`). It does not read `GITHUB_USER`.
- Filter, sort, and page behavior exists twice. `GithubUserServiceImpl` branches on `ApplicationProperties.useSqlQueries` (`impl-type.useSqlQueries`, bound from `SQL_IMPLEMENTATION_TYPE`):
  - `false` (local default): Spring Data JPA. `GithubJpaRepository` plus `GithubJpaRepositoryImpl` (Criteria API).
  - `true`: `NamedParameterJdbcTemplate` in `GithubJdbcRepositoryImpl`, SQL assembled by `SqlQueryBuilder`.
- Keep both paths behaviorally aligned unless the task says to touch only one. `GithubJpaRepositoryImpl` must keep that class name and must implement `GithubCustomJpaRepository`. Spring Data binds the custom fragment by the `Impl` suffix. The class is not annotated `@Repository`.
- `GithubRepository` in `data-model` is the batch writer’s repository. The API uses its own `GithubJpaRepository`. Do not merge them casually; they live in different applications.

## Persistence model

`GithubUser` maps to table `GITHUB_USER` (`@Table(name = "GITHUB_USER")`). JDBC SQL uses the lowercase name `github_user` (`Constants.TABLE_NAME`). On a case-sensitive MariaDB install those names differ. Keep new SQL consistent with the existing JDBC strings, and keep the JPA table name as declared.

`username` is unique. It is not part of the primary key. `id` is `GenerationType.IDENTITY`. The comment on the entity explains why a composite key was avoided: `GenerationType.IDENTITY` with a composite key is unsupported by Spring Data JPA.

`GithubProject` is `@Embeddable` and stored as columns on the same row (`project_name`, `project_description`, `project_language`, `project_stargazers`, `project_forks`, `project_url`). Only the user’s single most-starred repository is stored. There is no project collection table.

`@Version` is also `@Transient`, so optimistic locking is not active.

Bean validation group `OnCreate` exists. The batch processor calls `Validator.validate(user)` with the default group, so `@NotBlank(groups = OnCreate.class)` on `username` does **not** run during ingest.

## app-backend API

Base path `/api/github`. Controller is `GithubUserController`. Responses use `GithubUserDto`, not the entity. Mapping is hand-written in `GithubUserMapper` (singleton `INSTANCE` plus a Spring `@Component`). GitHub API payloads use Java records with snake_case components (`GithubApiUserDto`, `GithubApiRepoDto`) so Jackson binds GitHub’s JSON without a naming strategy.

| Method | Path | Behavior |
| --- | --- | --- |
| `GET` | `/user/{username}` | 200 + DTO, or 404 |
| `GET` | `/delete/{username}` | 200 if a row was removed, otherwise 404. Annotated `@PreAuthorize("hasRole('admin')")` |
| `POST` | `/fetch` | Body `GithubUserFetchRequest`, returns `GithubUserFetchResponse` |
| `GET` | `/user/add/{username}` | Loads the user from GitHub, inserts one row. 200 added, 404 unknown GitHub user, 409 already present (`addUser` returns `-1` on `DataIntegrityViolationException`) |

Delete and add are `GET` on purpose. The React client calls those paths. Changing the verb means changing `src/api/github` in the same change.

`addUser` calls `https://api.github.com`: user profile, followers list, following list, and every repo page (`per_page=100`). Follower and following counts are the length of the first page, not the `followers` / `following` fields on the user payload. The most-starred repo is chosen in memory with `max(stargazers_count)`.

### Fetch contract

`GithubUserFetchRequest` fields: `startRow`, `endRow`, `columnFilters`, `sortColumns`. `tableName` is `@JsonIgnore` and always `github_user`.

Infinite scroll is offset paging. Cursor paging was removed because it conflicts with sorting. AG Grid’s server-side row model sends `endRow` as an exclusive index (`cacheBlockSize` is 10, so blocks are `0–10`, `10–20`).

- JDBC: `OFFSET start ROWS FETCH NEXT (end - start) ROWS ONLY` (matches an exclusive `endRow`).
- JPA: `setFirstResult(startRow)` and `setMaxResults(endRow - startRow + 1)` (one extra row).
- Both set `lastRow` to `-1` when the page is empty, otherwise `startRow + pageSize`. The grid does not use `lastRow` as the total count. It passes `rowCount: undefined` and treats `lastRow === -1` on the first block as the empty overlay.

`ColumnFilter` is polymorphic Jackson. The discriminator property is `columnFilterType`:

- `stringColumnFilter` — operators serialized as `equals`, `notEqual`, `contains`, `blank`, `notBlank`.
- `numericColumnFilter` — operators `equals`, `notEqual`, `greaterThan`, `lessThan`, `toValue` (range). JPA’s numeric switch does not implement `IN_RANGE`; an unrecognized numeric operator throws `IllegalStateException`.

Sort JSON uses `sortDirection`: `ascending` or `descending`.

Column names differ by stack. Preserve `GithubUserMapper.FIELD_TO_COLUMN_MAP` and `COLUMN_TO_FIELD_MAP` (`fullName` ↔ `full_name`, `mostStarredProjectName` ↔ `project_name`).

- JDBC filters and sorts translate through `FIELD_TO_COLUMN_MAP`, then interpolate the name into SQL.
- JPA resolves the path with `COLUMN_TO_FIELD_MAP`, then `root.get(...)`. The attribute must exist on `GithubUser`. `mostStarredProjectName` is a DTO field, not an entity attribute. Sorting or filtering that name through Criteria API does not resolve to `mostStarredProject.projectName`.

`StringSqlBuilderUtil` and `NumericSqlBuilderUtil` concatenate filter values into SQL text. `SqlQueryBuilder` then runs that string with no bind parameters. New query code should use bind parameters. If you change the filter shape, update the sample at `app-backend/src/test/resources/data/GithubFetchRequest.json` and the TypeScript types in `app-frontend/src/api/types/FetchRequest.ts`.

The grid sends AG Grid field ids (`fullName`, `followers`) and AG Grid operator ids (`equals`, `greaterThan`). The sample JSON uses SQL-style `full_name` as well. Both forms are accepted for the mapped columns.

## data-ingest

Job name `importGithubUsers`, step `json-step`, chunk size 10. Job parameters are hard-coded in `BatchProcessingApplication`: classpath files `manifest.json` and `GitHub_Users.json`, plus a `startAt` timestamp so a new `JobInstance` can run. Both files resolve under `ref.inputDir` via `ClassPathResource` (bundled at `data-ingest/src/main/resources/data/`).

Startup checks, in order:

1. Job parameters must include `dataFile` and `manifest`.
2. Manifest bean validation (`checksum` not blank, `recordCount` positive).
3. MD5 of the data file (`ManifestUtil.calculateCheckSum`) must equal `manifest.checksum`.
4. After the step, `step.readCount` must equal `manifest.recordCount`.

`GithubItemMapper` is the `ItemProcessor`. It validates the bean and throws `DataValidationException` on failure. The step is fault tolerant, `processorNonTransactional`, and skips that exception up to `skipLimit` 10. A checksum or record-count mismatch fails the job (`InvalidJobParametersException`, `JobExecutionFailedException`).

The writer calls `GithubService.saveUser` per item, which calls `GithubRepository.save`. `RepositoryItemWriter` is commented out. `taskExecutor()` exists and is not attached to the step.

`spring.batch.jdbc.initialize-schema` is `ALWAYS`, so Batch’s metadata tables are created in the same MariaDB database.

`data-ingest/src/test/resources/create/githubData.py` builds sample payloads. Test resources live under `src/test/resources`; there are no Java test classes in the repo.

## summary-service

`GET /api/summarize/user/{username}` returns `Mono<ResponseEntity<String>>`.

`SummarizeServiceImpl.constructPrompt` loads up to 20 repos and 10 starred repos from GitHub. `getAiSummary` POSTs that text to the Hugging Face inference URL for `facebook/bart-large-cnn`, using `openai.api.key` as the bearer token. `Constants.OPEN_AI_API_URL` is unused. If `OPEN_AI_API_KEY` is empty the method returns `Optional.empty()` and the controller responds 400.

Current return path, if you are debugging a “wrong summary” report:

- `summarizeGithubUser` returns `Optional.of(username)`, not the model text.
- `UserReactiveServiceImpl.saveUser` persists `new UserSummary(username, "Summary: " + username)`.

The README marks the summarizer as an unfinished item (shorter prompt, better model, GitHub GraphQL). Treat those return values as the current behavior until a task asks to change them.

## app-frontend

Create React App (`react-scripts` 5), React 19, TypeScript `strict`. UI is `App` → `DataTable`. AG Grid Enterprise is registered in `components/agGridModules.ts` (`ServerSideRowModelModule` plus community filter, tooltip, and CSV modules). Grid options that matter:

- `rowModelType="serverSide"`, `cacheBlockSize={10}`, `maxBlocksInCache={2}`, `maxConcurrentDatasourceRequests={1}`.
- Datasource in `DataTable` builds `FetchRequest` and calls `fetchUsers` (`POST /fetch`).
- Sorting and filtering are server-side. The client does not hold the full table.
- Column defs in `components/config/ColumnDefs.tsx` are the product list: username, full name, email, bio, followers, following, top project. Username and top project render links. Email and bio use tooltips.
- “Add User” calls `GET /user/{username}` and, on a null result, `GET /user/add/{username}`, then filters the grid to that username.
- “Clear Filters” resets filter model and sort, then `refreshServerSide({ purge: true })`.
- CSV export uses `exportDataAsCsv` on rows the grid has already loaded. It does not download the whole database.

`src/App.test.tsx` still asserts the Create React App string `learn react`. The header text is `Github User Data`, so `npm test` fails on that test until it is updated.

Match existing style: function components, AG Grid typed with `GithubUser`, API helpers under `src/api/github`, shared types in `src/api/types`. `defaultHeaders` is only `Content-Type: application/json`. No auth header is attached.

## Security

`SecurityConfig` is a servlet OAuth2 resource server. It authenticates every request and requires role `user` on `/api/github/**`. JWT issuer is `http://localhost:8080/realms/myrealm`. The filter chain does not disable CSRF and does not set a stateless session policy.

`@PreAuthorize` on delete has no effect until `@EnableMethodSecurity` is added. It is not present.

The React app sends no bearer token. Keycloak setup in `ReadMe.md` is still “To be updated”. A security change that starts enforcing the existing matchers will break the grid until the frontend sends a token and CORS/CSRF still allow `localhost:3000`.

## Conventions

- Layering in `app-backend`: `controller`, `service` / `service.impl`, `repository` with `jdbc` and `jpa` implementations, `dto`, `request`, `response`, `utils`, `config`, `constants`, `exception`.
- Prefer constructor injection for new beans. Existing classes mix constructor injection and field `@Autowired`.
- Lombok is on the annotation-processor path (`1.18.38`): `@Data`, `@Builder`, `@Slf4j`, `@SneakyThrows`. A new entity or DTO should follow that, including `@NoArgsConstructor` where Jackson or JPA needs it.
- Logging markers already in the service (`PROCESSING … using JDBC Template` / `using JPA Repository`) are the signal for which repository path ran. Log at those same points when you add a branch.
- Checked failures in this codebase are small custom runtime exceptions (`DataPersistenceException`, `InvalidQueryException`, `RowMappingException`, `DataValidationException`, `JobExecutionFailedException`). There is no `@ControllerAdvice`.
- Comments in this repo often explain Spring annotations for learning. Add a comment only for a constraint the next reader cannot see (pagination exclusivity, `Impl` suffix, validation groups, checksum).

## Tests

No `*Test.java` sources exist. `spring-boot-starter-test` and, on the summary service, `reactor-test` and Testcontainers MongoDB are on the classpath for when tests are added. The only committed request fixture is `app-backend/src/test/resources/data/GithubFetchRequest.json`.

When you add backend tests, put them in the module you changed and run `mvn -pl <module> -am test`. For a grid, filter, or API-contract change, update the fixture and the TypeScript request types in the same change. For a UI behavior change, run the frontend test script and use the app against the API (add user, filter, sort, empty overlay, clear filters).

## Leave alone unless the task asks

- Planned architecture in `ReadMe.md`: admin/user roles, follow graph, email, Kafka repo-created events, WebSocket or SSE notifications, Redis, GitHub OAuth, polling scheduler.
- Row-click project summary and in-grid delete. The README describes them; `DataTable` does not implement them.
- Hard-coded batch file names, chunk size, skip limit, and `System.exit(0)` on success.
- The summarizer’s placeholder `UserSummary` text described above.
- Switching the default `SQL_IMPLEMENTATION_TYPE` or collapsing JDBC and JPA into one implementation.
- Formatting or rewriting unrelated modules. Educational comments in code you are not changing stay as they are.
