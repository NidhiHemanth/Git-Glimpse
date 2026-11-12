# data-model

Shared library for the GitHub user row in MariaDB. 
- `data-ingest` and `app-backend` depend on artifact `org.bootcamp.com:data-model:1.0-SNAPSHOT`. 
- This module has no `main` method, no HTTP port, and no `application.yml`.

### Row mapping

`GithubUser` is a JPA `@Entity` on table `GITHUB_USER`. One object is one row. Hibernate creates or updates the table on startup (`ddl-auto: update`).

- `id` is the primary key (`Long`).
  - `@Id` marks that column as the primary key.
  - `@GeneratedValue(strategy = GenerationType.IDENTITY)` makes MariaDB assign it (`AUTO_INCREMENT`). The application does not set it.
  - A composite primary key was avoided because Spring Data JPA does not support `GenerationType.IDENTITY` with `@IdClass` or `@EmbeddedId`.
- `username` is the GitHub login and the value the API looks up.
  - `unique = true` adds a unique constraint. It is not part of the primary key.
  - `@NotBlank(groups = OnCreate.class)` runs only when validation is asked for the `OnCreate` group. 
  - `GithubItemMapper` should call `beanValidator.validate(githubUserItem)` with a group argument. Default group does not run the check.
- `email` is `unique = true` and `@Email`. 
  - It is not part of the primary key, and the column allows null.
- Other columns on the same row: `full_name` (max 100), `bio` (length 500), `followers`, `following`, `profile_url`, `avatar_url`.
- `version` is a `long` that stays in memory.
  - Optimistic locking is on. With it on, Hibernate would store a counter and update with `WHERE id = ? AND version = ?`, rejecting a write when that counter had already changed. `@Transient` leaves nothing to compare, and `version` is not in the JDBC insert column list, so a second save of the same `id` overwrites the row.
  - `@Builder.Default` sets the in-memory value to `-1` when Lombok builds an instance.
  - `@Version` would be Hibernate's optimistic-lock counter.
  - `@Transient` keeps the field out of the table, so no `version` column is created and updates are not checked against it.
  - `@Column` is present on the same field and is not mapped.
- `mostStarredProject` is a `GithubProject`.
  - `@Embeddable` stores it as columns on the `GITHUB_USER` row. It has no table and no primary key.
  - Those columns are `project_name`, `project_description`, `project_language`, `project_stargazers`, `project_forks`, `project_url`.
  - Only the user's single most-starred repository is stored.

### Start

There is no application to start. Hence, this module does not serve HTTP.
> This POM does not parent `spring-boot-starter-parent`. It imports the `spring-boot-dependencies` BOM at `3.3.10` so dependency versions resolve. It does not declare `spring-boot-maven-plugin`, so `mvn spring-boot:run` does not apply here.

Build or install the jar from the repo root:

```bash
mvn -pl data-model test
mvn -pl data-model install
```

`data-ingest` and `app-backend` pull this module in with Maven `-am`, so a separate `install` is only needed when you build one of those modules without `-am`.

### Configuration

No runtime config. Compile settings in `pom.xml`:

| Property | Value | What it does |
| --- | --- | --- |
| `java.version` | `21` | Intended JDK. |
| `maven.compiler.source` / `target` | `23` | POM properties. The compiler plugin below overrides these. |
| `maven-compiler-plugin` `source` / `target` | `21` | The values that actually compile this module. Use JDK 21. |

### Dependencies

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

### Build plugins

<details>
<summary><code>maven-compiler-plugin</code> 3.11.0</summary>

Compiles this module with source and target **21**, regardless of the `23` values in `maven.compiler.source` and `maven.compiler.target`. `annotationProcessorPaths` points at Lombok `1.18.38` so the entity annotations are expanded during compilation.

</details>
