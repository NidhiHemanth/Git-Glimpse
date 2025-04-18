# Bootcamp Application

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
- The API supports infinite scroll feature of ag-grid

The APIs are exposed on Swagger UI / can also be accessed over Postman.
OAS3 documentation will also be available for the APIs.

## Input Specification

Add the path to your JSON files to the field `ref.inputDir` in `application-local.yml` file of data-ingest module. 
Then set the `spring.profiles.active=local` while running your Batch application.

## TODO

- [ ] API module: DTO & mappers