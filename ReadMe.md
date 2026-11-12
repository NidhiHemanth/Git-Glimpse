# Git-Glimpse Application

Git Glimpse is a full-stack project which uses ReactJS for the frontend, SpringBoot in the backend and MariaDB SQL for the Database.

## How to start the Application

1. Start the Backend SpringBoot application
2. Start the Frontend React application

Setting up JWT authentication, CORS, WebSocket, and custom security in this project provides:

- Stateless authentication: JWT allows secure, scalable, stateless user sessions (no server-side session storage). 
- API security: Only authenticated users can access protected endpoints; tokens are validated on every request. 
- WebSocket security: Ensures only authenticated users can establish and use real-time connections. 
- CORS configuration: Allows safe cross-origin requests from your frontend (e.g., React app). 
- Customizability: You control authentication, authorization, and session management, unlike default Spring Boot setups.

A normal Spring Boot project without these would be less secure, less scalable, and not ready for modern SPA frontends or real-time features.

## Project Structure

### 1) _data-model_

This module contains the Entities that are stored in Database.

### 2) _data-ingest_

This module contains the batch processing code to :
1. Read data from bulk JSON files
2. Process and validate the data in those files as objects of the defined Entitites
3. Persist the data into an MariaDB (SQL) database

Add the path to your JSON files to the field `ref.inputDir` in `application-local.yml` file of data-ingest module.

Add the following Environment Variables while running the SpringBoot Batch application:
- `spring.profiles.active=local`
- `DB_USERNAME=<root>` this is the default username for your database
- `DB_PASSWORD=<password>` add your database password

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
- The API supports infinite scroll feature, using ~~Cursor~~ Pagination

Add the following Environment Variables while running the SpringBoot Batch application:
- `spring.profiles.active=local`
- `DB_USERNAME=<root>` this is the default username for your database
- `DB_PASSWORD=<password>` add your database password
- `GITHUB_API_TOKEN=<personal access token>` this is optional

Keycloak is used for authentication & authorization.

<details>
<summary>Click for instructions to set up Keycloak</summary>

&emsp; To be updated

</details>

Start the SpringBoot Application and access Swagger [here](http://localhost:8081/swagger-ui/index.html).

The application lets you choose the type of database connection using the `SQL_IMPLEMENTATION_TYPE` configuration in `application-local.yml`
1. If enabled, then Spring JDBC (JDBC Templates) are used to connect to the database
2. If disabled, then Spring Data JPA (Java Persistence API) will be used

[Click here](Terminologies.md) to read more about SpringBoot terminology.

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

## TODO

- [x] Authenticate GitHub API calls using personal access token
- [x] Call OpenAI API endpoints to summarize a user - FAIL
- [x] Create Front-End application & Add AG-Grid to the frontend
- [x] Make custom API calls for filter & sorting in AG-Grid 
- [x] Fix tool tip display for email & bio
- [x] Fix construction of request for multiple column filter
- [x] Display NO RECORDS FOUND if response is empty
- [x] Add a button to clear all filters
- [x] Fix sorting of columns
- [x] Add input field & button to add a new user
- [x] Implement infinite scroll
- [x] Add a search bar to find a user, if user not present give an option to add user
- [x] Change implementation of infinite scroll; cursor based pagination doesn't work well with sorting
- [x] Add button to export data to CSV
- [x] Try alternative free AI APIs instead of OpenAI (Hugging AI / Gemini AI)
- [ ] KeyCloak authentication implementation 
- [x] Criteria API + Predicate builder implementation for API
- [ ] Use GitHub's [GraphQL](https://docs.github.com/en/graphql/overview/explorer) API to fetch data for summarizer
- [ ] Update Summarizer Service to create a short precise prompt about the user & use a better AI model for summarization

## New Architecture

#### 1. Authentication / Roles

Implement authentication and authorization using Spring Security with JWT-based authentication.

- admin: can add / delete a user's profile
  - user can be notified via email (optional)
  - user's followers need to be notified
- user: can log in, browse, follow / unfollow

#### 2. User Follow / Unfollow system

- a user can follow / unfollow other users
- the user should be notified when they were followed / unfollowed

#### 3. GitHub polling scheduler

Scheduled polling service that periodically monitors the GitHub API for repository updates,
persists the changes in database & publishes notification

- keeps polling to check if a user has new repository
- if new repository found
  - update user record in database
  - create & push the repo-creation event on the Kafka Stream

#### 4. Notification system

A fan-out service that will handle sending out notifications to all followers,
persists notification in database & uses real-time delivery for frontend updates.

- kafka repo-create events consumer
  - reads the repo-creation event
  - finds the list of followers for the user
  - using Web Sockets / Server-Sent Events (SSE) the notifications are relayed to the frontend in real-time
  - persist notification in database (if a user is offline notifications must remain available)
- user can see how many unread notifications
- user can mark notification as read

#### 5. Frontend Enhancements

- login for admin / user
- admin:
  - has option to add / delete a user
- user:
  - can view the users
  - can follow / unfollow users
  - have notification widget
- notification
  - widget displays number of unread
  - opens new page with separate READ / UNREAD messages
  - fetches realtime notification updates from backend

#### 6. Scalability considerations

- cache repository metadata locally (Redis caching)
- use scheduled polling intervals
- poll once per distinct GitHub username 
- email notifications
- GitHub OAuth login
- WebSocket clustering support 
- distributed scheduler support
- repository activity tracking (stars, forks, releases)