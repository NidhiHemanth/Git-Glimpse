# data-model

Shared library for the GitHub user row. `data-ingest` and `app-backend` depend on artifact `org.bootcamp.com:data-model:1.0-SNAPSHOT`. This module has no `main` method, no HTTP port, and no `application.yml`.

Java package: `org.bootcamp.com`.

`GithubUser` maps to table `GITHUB_USER`. `id` is `GenerationType.IDENTITY`. `username` is unique and is not part of the primary key. `GithubProject` is `@Embeddable` and is stored as columns on the same row (`project_name`, `project_description`, `project_language`, `project_stargazers`, `project_forks`, `project_url`). Only the user's single most-starred repository is stored.

`@Version` is also `@Transient`, so optimistic locking is not active. Bean validation group `OnCreate` exists. The batch processor validates with the default group, so `@NotBlank(groups = OnCreate.class)` on `username` does not run during ingest.

JDBC SQL in `app-backend` uses the lowercase name `github_user`. On a case-sensitive MariaDB install that name is a different table from `GITHUB_USER`.

## Start

There is nothing to start. Build or install the jar from the repo root:

```bash
mvn -pl data-model test
mvn -pl data-model install
```

`data-ingest` and `app-backend` pull this module in with Maven `-am`, so a separate `install` is only needed when you build one of those modules without `-am`.

This POM does not parent `spring-boot-starter-parent`. It imports the `spring-boot-dependencies` BOM at `3.3.10` so dependency versions resolve. It does not declare `spring-boot-maven-plugin`, so `mvn spring-boot:run` does not apply here.

## Swagger

None. This module does not serve HTTP.

## Configuration

No runtime config. Compile settings in `pom.xml`:

| Property | Value | What it does |
| --- | --- | --- |
| `java.version` | `21` | Intended JDK. |
| `maven.compiler.source` / `target` | `23` | POM properties. The compiler plugin below overrides these. |
| `maven-compiler-plugin` `source` / `target` | `21` | The values that actually compile this module. Use JDK 21. |

## Dependencies

<details>
<summary><code>spring-boot-starter-data-jpa</code></summary>

Spring Data JPA and Hibernate. This is what `GithubUser`, `GithubProject`, and `GithubRepository` (`JpaRepository`) are written against. Version comes from the imported Spring Boot `3.3.10` BOM. The module does not open a database connection by itself. The process that depends on it (`data-ingest` or `app-backend`) supplies the MariaDB datasource.

</details>

<details>
<summary><code>spring-boot-starter-web</code></summary>

Servlet stack, Spring MVC, and Jackson. Declared on this library. The sources in this module do not define a controller or a `main` method. `app-backend` also declares this starter itself. `data-ingest` does not, so it receives this starter transitively through `data-model`.

</details>

<details>
<summary><code>spring-boot-starter-validation</code></summary>

Jakarta Bean Validation (`jakarta.validation`). Supplies `@NotBlank`, `@Email`, and `@Size` on `GithubUser`, and the `OnCreate` validation group.

</details>

<details>
<summary><code>lombok</code> (optional)</summary>

Compile-time annotations on the entity: `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`. `optional` means dependents do not inherit Lombok on their compile classpath. The annotation processor is wired on `maven-compiler-plugin` at version `1.18.38`, not by this dependency's version field. The BOM supplies the dependency version.

</details>

## Build plugins

<details>
<summary><code>maven-compiler-plugin</code> 3.11.0</summary>

Compiles this module with source and target **21**, regardless of the `23` values in `maven.compiler.source` and `maven.compiler.target`. `annotationProcessorPaths` points at Lombok `1.18.38` so the entity annotations are expanded during compilation.

</details>
