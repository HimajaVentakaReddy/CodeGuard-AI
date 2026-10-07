# CodeGuard AI API

CodeGuard AI exposes a JSON REST API for Java source analysis and read-only access to analysis summaries. The service listens on port `8080` by default.

## Interactive reference

The running application publishes an OpenAPI 3.1 document and Swagger UI:

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

All endpoints are unauthenticated. Analysis requests are persisted as summaries; source code and individual findings are not stored.

## Common response conventions

- JSON endpoints use `Content-Type: application/json`.
- Severity values are `CRITICAL`, `HIGH`, `MEDIUM`, `LOW`, or `INFO`.
- Risk levels are `CRITICAL`, `HIGH`, `MEDIUM`, `LOW`, or `GOOD`.
- Error responses use the standard Spring Boot error response. Clients should rely on the HTTP status and treat the response detail as diagnostic text.
- Successful analysis responses include every finding in the response, but history stores only the score, risk level, counts, and analysis timestamp.

## `GET /api/health`

Checks that the service is running.

### Request

No body or parameters.

### `200 OK`

```json
{
  "status": "UP",
  "service": "CodeGuard AI"
}
```

### Errors

- `500 Internal Server Error` — unexpected server error.

## `GET /api/info`

Returns public application metadata.

### Request

No body or parameters.

### `200 OK`

```json
{
  "name": "CodeGuard AI",
  "version": "1.0.0",
  "description": "Intelligent Code Quality & Vulnerability Analyzer"
}
```

### Errors

- `500 Internal Server Error` — unexpected server error.

## `POST /api/analyze`

Analyzes Java source sent as JSON. A successful analysis is added to history.

### Request

`Content-Type: application/json`

```json
{
  "code": "public class Example {\\n    public void run() {}\\n}"
}
```

`code` must be non-null, non-blank Java source and no longer than 100,000 characters.

### `200 OK`

```json
{
  "score": 100,
  "riskLevel": "GOOD",
  "totalIssues": 0,
  "criticalCount": 0,
  "highCount": 0,
  "mediumCount": 0,
  "lowCount": 0,
  "findings": []
}
```

Each finding contains `ruleId`, `title`, `description`, `category`, `severity`, `lineNumber`, and `recommendation`. `category` is the applicable issue category, such as `SECURITY`, `BUG`, or `STYLE`.

### Errors

- `400 Bad Request` — request body is malformed, `code` is missing or null, or source is blank.
- `413 Content Too Large` — source exceeds 100,000 characters or the request exceeds the configured request size.
- `500 Internal Server Error` — unexpected analysis or history-persistence error.

## `POST /api/analyze/file`

Analyzes an uploaded UTF-8 Java source file. A successful analysis is added to history.

### Request

`Content-Type: multipart/form-data`, with one file part named `file`. The filename must end in `.java`; the file must contain non-empty, valid UTF-8 source. The maximum file size is 100,000 bytes and the configured multipart request maximum is 120 KB.

```bash
curl -X POST http://localhost:8080/api/analyze/file \
  -F "file=@Example.java;type=text/plain"
```

### `200 OK`

Returns the same response shape as [`POST /api/analyze`](#post-apianalyze).

### Errors

- `400 Bad Request` — file is missing or empty, filename is not `.java`, uploaded content is not valid UTF-8, or decoded source is blank.
- `413 Content Too Large` — file exceeds 100,000 bytes or multipart request exceeds 120 KB.
- `500 Internal Server Error` — upload could not be read or the analysis could not be persisted.

## `GET /api/analyses`

Lists saved analysis summaries, newest first.

### Request

No body or parameters.

### `200 OK`

Returns an array. An empty history is returned as `[]`.

```json
[
  {
    "id": 42,
    "analyzedAt": "2026-10-07T12:30:00Z",
    "score": 85,
    "riskLevel": "GOOD",
    "totalIssues": 1,
    "criticalCount": 0,
    "highCount": 1,
    "mediumCount": 0,
    "lowCount": 0
  }
]
```

### Errors

- `500 Internal Server Error` — analysis history could not be read.

## `GET /api/analyses/{id}`

Returns one saved analysis summary. The path parameter `id` is a numeric analysis identifier.

### Request

No body. Example: `GET /api/analyses/42`.

### `200 OK`

Returns one summary with the same fields as an item in [`GET /api/analyses`](#get-apianalyses).

### Errors

- `400 Bad Request` — `id` is not a valid integer.
- `404 Not Found` — no saved analysis has that identifier.
- `500 Internal Server Error` — analysis history could not be read.
