# CodeGuard AI REST API

CodeGuard AI provides endpoints for service metadata, rule-based Java source analysis, and reading saved analysis summaries. The API listens on port `8080` by default.

## API reference

When the backend is running:

- Swagger UI: <http://localhost:8080/swagger-ui/index.html>
- OpenAPI JSON: <http://localhost:8080/v3/api-docs>

## General behavior

- Analysis requests return JSON. Send JSON requests with `Content-Type: application/json`.
- A successful analysis is saved to history as a summary. The source code and individual findings are not stored in history.
- Analysis summaries are returned newest first.
- Validation failures and other errors use Spring's standard error response format. The `detail` text may describe the failure; clients should use the HTTP status as the stable signal.
- The API has **no authentication or authorization**. CORS is explicitly enabled for `http://localhost:5500` and `http://127.0.0.1:5500` on the analysis and history controllers; CORS is not configured as an authentication mechanism.

## `GET /api/health`

Reports the service's current status. This endpoint does not check database connectivity.

### Request

No body or parameters.

```bash
curl http://localhost:8080/api/health
```

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

Returns the configured public name, version, and description of the application.

### Request

No body or parameters.

```bash
curl http://localhost:8080/api/info
```

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

Analyzes Java source text using the implemented source-pattern rules and saves an analysis summary.

### Request

Content type: `application/json`.

The body must be a JSON object with a non-null, non-blank `code` string. The source is limited to 100,000 characters.

```bash
curl -X POST http://localhost:8080/api/analyze \
  -H "Content-Type: application/json" \
  -d '{"code":"public class Example {\\n    private String password = \\"admin123\\";\\n}"}'
```

Request body:

```json
{
  "code": "public class Example {\n    private String password = \"admin123\";\n}"
}
```

### `200 OK`

Returns the score, risk level, issue counts, and all findings from this analysis. Finding line numbers are one-based.

```json
{
  "score": 85,
  "riskLevel": "GOOD",
  "totalIssues": 1,
  "criticalCount": 0,
  "highCount": 1,
  "mediumCount": 0,
  "lowCount": 0,
  "findings": [
    {
      "ruleId": "SEC001",
      "title": "Hardcoded password",
      "description": "A password-like variable is assigned a string literal.",
      "category": "SECURITY",
      "severity": "HIGH",
      "lineNumber": 2,
      "recommendation": "Load the password from a secure secret store or environment variable."
    }
  ]
}
```

The current rules recognize hardcoded password-like and API key/token/secret assignments, `Runtime.getRuntime().exec(...)`, empty catch blocks, TODO/FIXME comments, and `System.out.println(...)`. Risk scores are capped between 0 and 100; deductions are 25 per critical, 15 per high, 8 per medium, and 3 per low finding. INFO findings do not reduce the score.

### Errors

- `400 Bad Request` — `code` is missing or null, or its value is empty or whitespace-only. Malformed JSON is also rejected as a bad request.
- `413 Content Too Large` — source exceeds 100,000 characters.
- `500 Internal Server Error` — an unexpected analysis or history-persistence failure occurs.

## `POST /api/analyze/file`

Analyzes an uploaded Java source file as UTF-8 and saves an analysis summary.

### Request

Content type: `multipart/form-data`. Include one file part named `file`.

Requirements:

- Original filename ends in `.java` (case-insensitive).
- File is non-empty and contains valid UTF-8 text.
- File size does not exceed 100,000 bytes.
- Multipart request size does not exceed the configured 120 KB limit.
- Decoded source is non-blank and no longer than 100,000 characters.

Example:

```bash
curl -X POST http://localhost:8080/api/analyze/file \
  -F "file=@Example.java;type=text/plain"
```

### `200 OK`

Returns the same response structure as [`POST /api/analyze`](#post-apianalyze), including findings for this request.

### Errors

- `400 Bad Request` — file part is missing or empty, filename is not `.java`, content is invalid UTF-8, or decoded source is blank.
- `413 Content Too Large` — file exceeds 100,000 bytes, decoded source exceeds 100,000 characters, or multipart request exceeds 120 KB.
- `500 Internal Server Error` — uploaded contents cannot be read or an unexpected persistence error occurs.

## `GET /api/analyses`

Lists saved analysis summaries in most-recent-first order. The response does not include source code or findings.

### Request

No body or parameters.

```bash
curl http://localhost:8080/api/analyses
```

### `200 OK`

Returns a JSON array; no saved analyses is represented by an empty array.

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

- `500 Internal Server Error` — an unexpected history read failure occurs.

## `GET /api/analyses/{id}`

Returns one saved analysis summary by its numeric database identifier. Source code and individual findings are not included.

### Request

Path parameter:

| Name | Type | Required | Description |
| --- | --- | --- | --- |
| `id` | Integer (`Long`) | Yes | Identifier of the saved analysis |

```bash
curl http://localhost:8080/api/analyses/42
```

### `200 OK`

Returns one summary object with the same fields as an item from [`GET /api/analyses`](#get-apianalyses).

### Errors

- `400 Bad Request` — `id` cannot be converted to an integer.
- `404 Not Found` — no analysis exists for the supplied numeric identifier.
- `500 Internal Server Error` — an unexpected history read failure occurs.
