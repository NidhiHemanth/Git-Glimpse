# Bootcamp Application

Bootcamp is a full-stack project which uses ReactJS for the frontend, SpringBoot in the backend and MariaDB SQL for the Database.

## Project Structure

### 1) _data-model_

This module contains the Data Transfer Objects (DTO) required to map the JSON data.

### 2) _data-ingest_

This module contains the batch processing code to :
1. Read data from bulk JSON files
2. Process and validate the data in those files
3. Persist the data into an MariaDB (SQL) database

Make sure you have MariaDB set up, with a database called `github`.
Execute the following command to change the character set of the Database to support all Unicode characters (in case your data has emojis or special symbols).

`ALTER DATABASE github CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci;`

An alternative to this method would be to use a library like `jsonschema2pojo` to convert JSON Schema / data into Java POJOs (Plain Old Java Objects).
- It deserializes JSON into Java objects (via libraries like Jackson or Gson).
- It generates classes that represent data structures coming from REST APIs that use JSON.

### 3) _app-backend_

This module contains APIs to perform various operations on the GitHub Users data.
- It allows filtering of data on selected fields
- It allows sorting of the resultant data
- The API supports infinite scroll feature, using Cursor based Pagination

The application lets you choose the type of database connection using the `SQL_IMPLEMENTATION_TYPE` configuration in `application-local.yml`
1. If enabled, then Spring JDBC (JDBC Templates) are used to connect to the database
2. If disabled, then Spring Data JPA (Java Persistence API) will be used

<details>
<summary>Click to learn more about SpringBoot terminology</summary>

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
  - EntityManager is JPA’s core interface for persistence.
  - Criteria API builds queries like a tree structure using builders
  - More flexible than repositories — great for complex/dynamic queries.

Refer here for [Spring Data JPA](https://youtu.be/GkkcZXF-mD8),
[JPA vs Entity Manager](https://medium.com/@mgm06bm/jparepository-and-entitymanager-in-spring-data-jpa-e12daad579a7).

</details>

The APIs are exposed on Swagger UI / can also be accessed over Postman.
OAS3 documentation will also be available for the APIs.

This Backend has a monolithic architecture (as opposed to a Microservices based architecture).

### 4) _app-frontend_

This module contains the UI implementation done using React JS.
- It displays the data in a table with infinite scroll
- You can apply filters on the columns
  - username / full name
  - followers / following
  - checkbox for bio (filter records where bio exists)
- Sort the records being displayed based on the columns
  - username / full name
  - followers / following
- Add a new Github User to the database (if the username is valid & exists)
- Upon clicking a user record (row)
    - A summary of all the user's Github projects are displayed (this is cached)
    - You are also given an option to delete the user's record from the database
- Export the entire database content to an Excel sheet (not just the one displayed)

This table is being implemented using [AG-Grid](https://www.ag-grid.com/react-data-grid/deep-dive/) because of its features:
1. Selective column filtering & sorting
2. Custom column filtering & sorting logic
3. In-built support for pagination & infinite scroll
4. Supports large datasets

_**NOTE**:_ Sorting & Filtering are being done on the backend (not frontend) since 
we don't have all the data available because infinite scroll loads data in chunks.

## Input Specification

Add the path to your JSON files to the field `ref.inputDir` in `application-local.yml` file of data-ingest module. 
Then set the `spring.profiles.active=local` while running your Batch application.

## TODO

- [ ] TBD