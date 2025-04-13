# Bootcamp Application

## Project Structure

### 1) _data-model_

This module contains the Data Transfer Objects (DTO) required to map the JSON data.

### 2) _data-ingest_

This module contains the batch processing code to :
1. Read data from bulk JSON files
2. Process and validate the data in those files
3. Persist the data into an H2 database

An alternative to this method would be to use a library like `jsonschema2pojo` to convert JSON Schema / data into Java POJOs (Plain Old Java Objects).
- It deserializes JSON into Java objects (via libraries like Jackson or Gson).
- It generates classes that represent data structures coming from REST APIs that use JSON.

## Input Specification

Add the path to your JSON files to the field `ref.inputDir` in `application-local.yml` file of data-ingest module. 
Then set the `spring.profiles.active=local` while running your Batch application.

## TODO

- [ ] API module: DTO & mappers