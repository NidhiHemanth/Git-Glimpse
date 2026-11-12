# app-frontend

This is a React App with React 19 and TypeScript `strict`. The dev server listens on port **3000**.

## Proxy

A **proxy** is a stand-in: the browser talks to the dev server, and that host passes the request on.
- This keeps the browser on one origin, so the API does not need a CORS allowlist for port 3000. 
- The dev server, is a Node process started by npm start.
  - The proxy applies only to `npm start`. `npm run build` does not include it.
- The API host lives in `package.json` instead of in the client. 
  - `package.json` sets `"proxy": "http://localhost:8081"`.
- `src/api/axios.ts` uses the relative base URL `/api/github` so the proxy is actually used. 

**CORS** is a rule the browser applies to JavaScript. The *dev server* is not a browser.
- The browser never calls port 8081 directly, so the browser sends that request with no CORS check.
- The UI is loaded from http://localhost:3000. The grid then requests http://localhost:3000/api/github/fetch. Same scheme, host and port means same origin.
- This second hop is a server-to-server call. Node does not enforce CORS.

AG Grid Enterprise is registered in `src/components/agGridModules.ts` (`ServerSideRowModelModule` plus community filter, tooltip, and CSV modules). Grid options that matter:

- `rowModelType="serverSide"`, `cacheBlockSize={10}`, `maxBlocksInCache={2}`, `maxConcurrentDatasourceRequests={1}`.
- The datasource in `DataTable` builds a `FetchRequest` and calls `fetchUsers` (`POST /fetch`).
- Sorting and filtering are server-side. The client does not hold the full table.

Column defs in `src/components/config/ColumnDefs.tsx`: username, full name, email, bio, followers, following, top project. Username and top project render links. Email and bio use tooltips.

“Add User” calls `GET /user/{username}` and, on a null result, `GET /user/add/{username}`, then filters the grid to that username. “Clear Filters” resets the filter model and sort, then `refreshServerSide({ purge: true })`. CSV export uses `exportDataAsCsv` on rows the grid has already loaded.

`defaultHeaders` in `src/api/github/getHeader.ts` is only `Content-Type: application/json`. No auth header is attached.

`src/App.test.tsx` still asserts the Create React App string `learn react`. The header text is `Github User Data`, so `npm test` fails on that test until it is updated.

## Start

`app-backend` must already be running on port 8081.

```bash
cd app-frontend
npm install
npm start
```

Open http://localhost:3000.

Other scripts from `package.json`:

| Script | Command | What it does |
| --- | --- | --- |
| `start` | `react-scripts start` | Dev server on port 3000 with reload. |
| `build` | `react-scripts build` | Production bundle in `build/`. |
| `test` | `react-scripts test` | Jest. `npm test -- --watchAll=false` runs once. |
| `eject` | `react-scripts eject` | Copies the CRA config into this project. One way. Do not run it as part of normal startup. |

## Swagger

This app does not serve Swagger. The API it calls documents itself at http://localhost:8081/swagger-ui/index.html.

`summary-service` has its own Swagger on port 8082. This UI does not call that service.

## Configuration

No `.env` file is required.

| Place | Value | What it does |
| --- | --- | --- |
| `package.json` `proxy` | `http://localhost:8081` | Dev server forwards `/api/github` to the API. |
| `src/api/axios.ts` `baseURL` | `/api/github` | Every grid request, relative to the page. |
| `src/api/axios.ts` headers | `Accept: application/json` | Axios instance default. |
| `src/api/github/getHeader.ts` `defaultHeaders` | `Content-Type: application/json` | Headers passed by the API helpers. |
| `tsconfig.json` | `strict` | TypeScript strict mode. |
| `package.json` `browserslist` | CRA defaults | Which browsers the production build targets. |

Request body types for `POST /fetch` live in `src/api/types/FetchRequest.ts`. Shared row type: `src/api/types/GithubUser.ts`.

## Dependencies

All of these are in `dependencies` in `package.json`. None are under `devDependencies`. Versions below are the ranges in that file. `package-lock.json` pins the installed versions.

<details>
<summary><code>react</code> ^19.1.0</summary>

UI library. Components are functions (`App`, `DataTable`). React 19 is what `react-scripts` 5 starts.

</details>

<details>
<summary><code>react-dom</code> ^19.1.0</summary>

Renderer. `src/index.tsx` mounts the app into the DOM.

</details>

<details>
<summary><code>react-scripts</code> 5.0.1</summary>

Create React App toolchain: webpack dev server, production build, and Jest. Pinned to `5.0.1` (no caret). `npm start`, `npm run build`, and `npm test` all go through this package. This is the frontend equivalent of a run plugin: it is what actually starts the app.

</details>

<details>
<summary><code>typescript</code> ^4.9.5</summary>

TypeScript compiler used by CRA. `strict` is on in `tsconfig.json`. CRA 5 does not use TypeScript 5.

</details>

<details>
<summary><code>ag-grid-community</code> ^33.2.4</summary>

AG Grid community modules: column filters, tooltips, and CSV export registered from `agGridModules.ts`.

</details>

<details>
<summary><code>ag-grid-enterprise</code> ^33.2.4</summary>

Enterprise modules. The server-side row model (`ServerSideRowModelModule`) comes from this package. That is what pages the grid through `POST /api/github/fetch` instead of holding every row in the browser.

</details>

<details>
<summary><code>ag-grid-react</code> ^33.2.4</summary>

React bindings for AG Grid. `DataTable` renders the grid through this package. Keep the three `ag-grid-*` packages on the same version.

</details>

<details>
<summary><code>axios</code> ^1.9.0</summary>

HTTP client. One instance in `src/api/axios.ts` with `baseURL` pointed at the backend. Helpers under `src/api/github` (`fetchUsers`, `findGithubUser`, `addNewUser`) use it.

</details>

<details>
<summary><code>@types/react</code> ^19.1.2</summary>

TypeScript types for React 19.

</details>

<details>
<summary><code>@types/react-dom</code> ^19.1.3</summary>

TypeScript types for `react-dom`.

</details>

<details>
<summary><code>@types/node</code> ^16.18.126</summary>

Node.js types for the CRA toolchain and Jest.

</details>

<details>
<summary><code>@types/jest</code> ^27.5.2</summary>

Jest types for `src/App.test.tsx` and `src/setupTests.ts`.

</details>

<details>
<summary><code>@testing-library/react</code> ^16.3.0</summary>

Render helpers for component tests.

</details>

<details>
<summary><code>@testing-library/dom</code> ^10.4.0</summary>

DOM queries used by Testing Library.

</details>

<details>
<summary><code>@testing-library/jest-dom</code> ^6.6.3</summary>

Custom Jest matchers for DOM nodes. Loaded from the test setup.

</details>

<details>
<summary><code>@testing-library/user-event</code> ^13.5.0</summary>

Simulated user input for tests. The committed `App.test.tsx` does not drive the grid with it. It still asserts the CRA string `learn react`.

</details>

<details>
<summary><code>web-vitals</code> ^2.1.4</summary>

Create React App's web-vitals helper. Nothing under `src/` imports it. It is still installed because it is listed in `package.json`.

</details>
