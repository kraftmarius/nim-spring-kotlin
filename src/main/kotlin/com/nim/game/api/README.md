# API Layer — REST Transport & Contracts

This directory contains the inbound HTTP presentation layer for the Nim game engine. It exposes a clean, resource-oriented REST interface designed for straightforward interaction via `curl`, Postman, or frontend clients.

## Package Structure

```
com.nim.game.api
├── GameController.kt         # Spring WebMvc REST controller
├── GameDtos.kt               # Strongly typed Request/Response models
├── GlobalExceptionHandler.kt # RFC 9457 ProblemDetail error mapping
└── HealthController.kt       # Health check endpoint with game metrics
```

______________________________________________________________________

## Architectural Principles

1. **Strict Transport Decoupling:**

   - Domain models (`Game`, `HeapState`, `GameRules`) are never directly exposed over HTTP.
   - All payloads are serialized through dedicated Data Transfer Objects (`GameResponse`, `CreateGameRequest`, `MakeMoveRequest`).

1. **Standardized Error Semantics (RFC 9457):**

   - No ad-hoc error formats. Failures are mapped to Spring's standard `ProblemDetail` responses with informative `status`, `detail`, and `title` attributes.

1. **RESTful Resource Semantics:**

    - Resource collection: `/api/v1/games`
    - Individual session: `/api/v1/games/{id}`
    - Action sub-resource: `/api/v1/games/{id}/moves`
    - Health check: `/api/v1/health`

______________________________________________________________________

## Endpoint Specifications

### 1. Create Game

Initializes a new game session. All body parameters are optional and fall back to application defaults.

- **Method:** `POST`
- **Path:** `/api/v1/games`
- **Headers:** `Content-Type: application/json`
- **Request Body (Optional):**
  ```json
  {
    "heaps": [13],
    "maxTake": 3,
    "mode": "MISERE",
    "difficulty": "NIGHTMARE",
    "startingPlayer": "HUMAN"
  }
  ```
   - `heaps`: Optional list of heap sizes. Each value must be `>= 1`; non-positive values return `400 Bad Request`. A single-element list (e.g. `[13]`) is the classic Nim setup. Multi-heap lists (e.g. `[3, 4, 5]`) are only supported for `I_AM_TOO_YOUNG_TO_DIE` difficulty; using them with `HURT_ME_PLENTY` or `NIGHTMARE` returns `501 Not Implemented`. When omitted, a single heap is initialized with a uniform random size between `nim.default.random-heap-min` and `nim.default.random-heap-max` (inclusive).
  - `startingPlayer`: Optional. When omitted, `HUMAN` or `COMPUTER` is selected with equal probability.
- **Response:** `201 Created`
- **Response Headers:** `Location: /api/v1/games/{id}`
- **Response Body:** `GameResponse`

______________________________________________________________________

### 2. List All Games

Returns all stored game sessions.

- **Method:** `GET`
- **Path:** `/api/v1/games`
- **Response:** `200 OK`
- **Response Body:** `List<GameResponse>`

______________________________________________________________________

### 3. Get Game State

Retrieves the current state, heap counts, turn ownership, and move history of an existing game.

- **Method:** `GET`
- **Path:** `/api/v1/games/{id}`
- **Response:** `200 OK`
- **Response Body:** `GameResponse`

______________________________________________________________________

### 4. Make Move

Executes a player move on a designated heap. The server automatically evaluates the move, checks victory conditions, and triggers the AI counter-move within the same request.

- **Method:** `POST`
- **Path:** `/api/v1/games/{id}/moves`
- **Headers:** `Content-Type: application/json`
- **Request Body:**
  ```json
  {
    "heapIndex": 0,
    "matches": 2
  }
  ```
- **Validation:**
  - `matches`: Must be `>= 1`
  - `heapIndex`: Must be `>= 0`
- **Response:** `200 OK`
- **Response Body:** `GameResponse`

______________________________________________________________________

### 5. Health Check

Returns service liveness with current game metrics.

- **Method:** `GET`
- **Path:** `/api/v1/health`
- **Response:** `200 OK`
- **Response Body:** `HealthResponse`

```json
{
  "timestamp": "2026-09-14T12:00:00Z",
  "activeGames": 2,
  "totalGames": 5
}
```

______________________________________________________________________

## Response Structure (`GameResponse`)

```json
{
  "id": "c1f7a0c8-6627-4404-b9b5-c0e86b20fa35",
  "heaps": [9],
  "currentTurn": "HUMAN",
  "status": "IN_PROGRESS",
  "winner": null,
  "difficulty": "NIGHTMARE",
  "rules": {
    "maxTake": 3,
    "mode": "MISERE"
  },
  "lastComputerMove": {
    "player": "COMPUTER",
    "heapIndex": 0,
    "matches": 2
  },
  "history": [
    { "player": "HUMAN", "heapIndex": 0, "matches": 2 },
    { "player": "COMPUTER", "heapIndex": 0, "matches": 2 }
  ]
}
```

______________________________________________________________________

## Error Handling Matrix (RFC 9457)

All exceptions caught by `GlobalExceptionHandler` produce an RFC 9457 `application/problem+json` payload:

| Exception | HTTP Status | Problem Detail Description |
| :--- | :--- | :--- |
| `MethodArgumentNotValidException` | `400 Bad Request` | Bean validation errors (e.g. `matches: matches must be at least 1`). |
| `HttpMessageNotReadableException` | `400 Bad Request` | Malformed or unreadable JSON request payload. |
| `IllegalArgumentException` | `400 Bad Request` | Invalid request parameters (e.g. `maxTake` not greater than `1`). |
| `InvalidMoveException` | `400 Bad Request` | Domain rule violations (e.g. out of turn, take exceeds maxTake). |
| `GameNotFoundException` | `404 Not Found` | The requested `gameId` does not exist in memory. |
| `HttpRequestMethodNotSupportedException` | `405 Method Not Allowed` | The HTTP method is not supported for the requested path. |
| `UnsupportedStrategyException` | `501 Not Implemented` | Strategy cannot support the requested configuration (e.g. multi-heap on `HURT_ME_PLENTY` or `NIGHTMARE`). |

______________________________________________________________________

## Tactical Component Map

| Component | Responsibility |
| :--- | :--- |
| `GameController` | Spring `@RestController` handling `/api/v1/games` routing and HTTP status codes. |
| `HealthController` | Spring `@RestController` exposing `/api/v1/health` with active and total game counts. |
| `GlobalExceptionHandler` | `@RestControllerAdvice` mapping application and domain errors to RFC 9457 `ProblemDetail`. |
| `CreateGameRequest` | DTO capturing optional game creation parameters with defaults. |
| `MakeMoveRequest` | DTO capturing player actions with `@field:Min` constraints. |
| `GameResponse` | Primary response DTO presenting heap state, rules, status, and move history. |
| `HealthResponse` | DTO for the health endpoint: timestamp, active game count, total game count. |
| `MoveDto` | DTO representing individual player actions. |
| `GameRulesDto` | DTO representing configured session rules. |
