# data-ingest

Initial one-time Spring Batch job. It reads `manifest.json` and `GitHub_Users.json` from the classpath, validates each record, and writes `GithubUser` rows through `data-model`. Entry point: `org.bootcamp.com.BatchProcessingApplication`.

- `WebApplicationType.NONE` means this process opens no HTTP port. 
- `spring.batch.job.enabled` is `false`, so Spring Boot does not launch a job with empty parameters. 
- A `CommandLineRunner` starts job `importGithubUsers` with hard-coded parameters: classpath files `manifest.json` and `GitHub_Users.json`, plus a `startAt` timestamp so each launch can be a new `JobInstance`. On success the listener calls `System.exit(0)`.

Startup checks, in order:

1. Job parameters include `dataFile` and `manifest`.
2. Manifest bean validation: `checksum` is not blank, `recordCount` is positive.
3. MD5 of the data file matches `manifest.checksum`.
4. After the batch job is done, verify `step.readCount` equals `manifest.recordCount`.

The Batch Job `importGithubUsers` has only this one step and `json-step` uses chunk size 10. `GithubItemMapper` throws `DataValidationException` on a failed bean validation. The step is fault-tolerant and skips that exception up to `skipLimit` 10. A checksum or record-count mismatch fails the job.

The bundled files live at `src/main/resources/data/`.

### Starting the Application

Set the environment variables `DB_USERNAME` `DB_PASSWORD` in the terminal session using `export VAR_NAME=VAR_VALUE`. They have no default in `application.yml`.


1. On a Fedora OS, use these commands to start MariaDB server. It will start listening on `localhost:3306`.
    ```
    sudo systemctl start mariadb
    mariadb -u root -p
    ```
   Create an empty database called `github`.
2. From the repo root, run the following to start the application
    ```bash
    mvn -pl data-ingest -am spring-boot:run -Dspring-boot.run.profiles=local
    ```
    - `-pl data-ingest` builds this module. `-am` also builds `data-model`. `-Dspring-boot.run.profiles=local` loads `application-local.yml`, which sets `INPUT_DIR` to `data`.
    - `spring-boot-maven-plugin` is declared in this module's `pom.xml`, so the short goal `spring-boot:run` works. 
    - The parent POM is `spring-boot-starter-parent` `3.3.10`, which supplies the plugin version. 

The process exits when the job finishes.

### Swagger

None. There is no web server and no `springdoc` dependency.

### Configuration

`src/main/resources/application.yml`, overridden by `application-local.yml` when the `local` profile is active.

| Key | Value | What it does |
| --- | --- | --- |
| `spring.application.name` | `BatchProcessing` | Process name. |
| `spring.jpa.hibernate.ddl-auto` | `update` | Hibernate creates or updates tables, including `GITHUB_USER`, on startup. There is no Flyway or Liquibase changelog. |
| `spring.jpa.show-sql` | `false` | SQL is not printed by this flag. |
| `spring.datasource.url` | `jdbc:mariadb://localhost:3306/github?...` | MariaDB database `github`, Unicode `utf8mb4`. |
| `spring.datasource.username` / `password` | `${DB_USERNAME}` / `${DB_PASSWORD}` | Required environment variables. |
| `spring.datasource.driver-class-name` | `org.mariadb.jdbc.Driver` | MariaDB JDBC driver. |
| `spring.datasource.hikari.maximum-pool-size` | `10` | Hikari connection pool size. |
| `spring.batch.job.enabled` | `false` | Stops the automatic empty-parameter job launch. |
| `spring.batch.jdbc.initialize-schema` | `ALWAYS` | Creates Spring Batch metadata tables in the same `github` database. |
| `ref.inputDir` | `${INPUT_DIR}` | Classpath directory for the JSON files. |
| `INPUT_DIR` in `application-local.yml` | `data` | Value used when profile `local` is on. Without that profile, `INPUT_DIR` must be set in the environment. |
| `logging.level.org.bootcamp.com` | `DEBUG` | Application logs. Root is `WARN`. |

Compile settings match the other Spring modules: `java.version` is `21`, the POM source/target properties say `23`, and `maven-compiler-plugin` forces source and target **21**.

### Dependencies

<details>
<summary><code>data-model</code> 1.0-SNAPSHOT</summary>

The shared `GithubUser` entity, `GithubRepository`, and `GithubService`. The batch writer calls `GithubService.saveUser`, which calls `GithubRepository.save`. This module does not define its own copy of the table mapping.

</details>

<details>
<summary><code>spring-boot-starter-data-jpa</code></summary>

Hibernate and Spring Data JPA for the writer. `ddl-auto: update` uses this stack to align `GITHUB_USER` with the entity when the job starts.

</details>

<details>
<summary><code>spring-boot-starter-validation</code></summary>

Bean Validation used by `GithubItemMapper` on each record, and by manifest validation (`checksum`, `recordCount`) before the step runs.

</details>

<details>
<summary><code>mariadb-java-client</code> 2.7.0</summary>

JDBC driver named by `spring.datasource.driver-class-name`. Hibernate and Spring Batch's metadata tables both connect through it. The version is pinned in this POM.

</details>

<details>
<summary><code>spring-boot-starter-batch</code></summary>

Spring Batch: `Job`, `Step`, `JobLauncher`, and the JDBC job repository. This is the runtime for `importGithubUsers` / `json-step`.

</details>

<details>
<summary><code>lombok</code> (optional)</summary>

`@Slf4j` on `BatchProcessingApplication` and other classes. Optional, so it is not transitive. The compiler plugin runs Lombok `1.18.38` as an annotation processor.

</details>

<details>
<summary><code>spring-boot-starter-test</code> (test)</summary>

Spring Boot's test starter (JUnit, Spring Test, AssertJ). Scope `test`. This module has no `*Test.java` sources. Test resources live under `src/test/resources`, including `create/githubData.py`.

</details>

<details>
<summary><code>spring-batch-test</code> (test)</summary>

Spring Batch test utilities (`JobLauncherTestUtils` and related types). Scope `test`. No Java test class in this module uses it yet.

</details>

### Build plugins

<details>
<summary><code>spring-boot-maven-plugin</code></summary>

Lets you run this module with `mvn spring-boot:run` from a reactor build (`-pl data-ingest -am`). The version is inherited from `spring-boot-starter-parent` `3.3.10`, so the plugin block declares no version. Before this plugin was declared, the launch command had to pass the full coordinate `org.springframework.boot:spring-boot-maven-plugin:3.3.10:run`.

The plugin packages an executable jar when you `package`. The running app is still `WebApplicationType.NONE` and still exits after the job.

</details>

<details>
<summary><code>maven-compiler-plugin</code> 3.11.0</summary>

Forces Java source and target **21** and registers Lombok `1.18.38` as an annotation processor. That overrides `maven.compiler.source` and `maven.compiler.target`, which are set to `23` in the POM properties.

</details>
