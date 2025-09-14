# SpringBoot Terminologies

```text
+--------------------+
|      Web Layer     |  ← Spring MVC (Controllers, REST APIs)
+--------------------+
|    Service Layer   |  ← Business logic (optional, but common)
+--------------------+
|    Data Layer      |  ← JDBC, JPA, Hibernate, DAOs, Repositories
+--------------------+
```

### 1) Web / Controller Layer

**Spring MVC** (Model-View-Controller) is part of the web layer of the Spring Framework. It’s used to build web applications, especially RESTful APIs or traditional server-side rendered web apps (like Thymeleaf-based apps).

At its core, Spring MVC handles HTTP requests/responses and maps them to Java methods.

### 2) Data Layer

- **JDBC** : Low-level API to connect and interact with relational databases (like MySQL, PostgreSQL etc).
- **Spring JDBC** : Simplifies JDBC by handling connection management, exception translation, and boilerplate code.
- **ORM** : A programming technique that maps database tables to Java objects. Goals is to avoid writing SQL & work with objects instead.
- **DAO** : DAO is a design pattern used to separate the low-level data accessing logic from the business logic.
- **JPA** : Java Persistence API
    - A standard/specification for ORM in Java (part of Jakarta EE). Not an implementation, but defines how ORM should work.
    - Spring Data JPA provides ready-to-use interfaces to work with databases.
- **Hibernate** : A popular ORM framework for Java. Hibernate implements JPA, so using JPA with Hibernate is very common.
    - Hibernate uses JDBC in the background
- **JPA Repository** - Abstracts all the common database operations.
- **Entity Manager + Criteria API**
    - EntityManager is JPA’s core interface for persistence, and it provides access to Criteria Builder.
    - Criteria API builds queries like a tree structure using builders
    - Predicate objects are used to dynamically build conditions
    - More flexible than repositories — great for complex/dynamic queries.

&emsp; Refer here for [Spring Data JPA](https://youtu.be/GkkcZXF-mD8),
[JPA vs Entity Manager](https://medium.com/@mgm06bm/jparepository-and-entitymanager-in-spring-data-jpa-e12daad579a7).

### 3) Service Layer
