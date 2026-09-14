# Nim Game API — Design & Specification

A RESTful API for the **Nim** game, supporting both the **normal** and **misère** variants across one or more heaps, built with **Kotlin** and **Spring Boot**.

______________________________________________________________________

## Game Theory & Specification

The system implements the subtraction game **Nim** across one or more heaps, with both the **normal** and **misère** variants:

- **Rules:** Two players (Human vs. Computer) take turns removing matches from a single heap per turn. Each move removes between **1** (fixed minimum) and a configurable maximum (default: **3**) matches, as defined by `GameRules`.
- **Normal Condition:** The player who takes the **last match wins**.
- **Misère Condition:** The player forced to take the **last match loses**.

______________________________________________________________________

## API Endpoints

All endpoints are versioned under `/api/v1` and exchange JSON. All errors follow RFC 9457 (`application/problem+json`).

| Method | Path | Description |
| :--- | :--- | :--- |
| `POST` | `/api/v1/games` | Create a game session. All request fields are optional. Omitted `heaps` and `startingPlayer` are randomized; other omitted fields fall back to configuration defaults. |
| `GET` | `/api/v1/games/{id}` | Retrieve heap state, current turn, status, and move history for a game. |
| `POST` | `/api/v1/games/{id}/moves` | Submit a player move; the AI counter-move resolves within the same request. |

**Difficulty & heap support:**

| Difficulty | Behavior | Multi-heap |
| :--- | :--- | :--- |
| `I_AM_TOO_YOUNG_TO_DIE` | Uniform random play | Supported |
| `HURT_ME_PLENTY` | 50/50 optimal/random composite | Rejected (`501 Not Implemented`) |
| `NIGHTMARE` | Optimal P-position play | Rejected (`501 Not Implemented`) |

Full request/response schemas and the error-handling matrix are documented in [`api/README.md`](src/main/kotlin/com/nim/game/api/README.md).

______________________________________________________________________

## Technical Stack & Architectural Decisions

- **Kotlin & JVM 25 (LTS):** Chosen for modern language ergonomics (immutability, data classes, null safety) on the LTS runtime.
- **Spring Boot 4.1.1:** Latest release track, avoiding milestones or unstable snapshots.
- **Minimal Dependencies:** Core framework starters limited to `org.springframework.boot:spring-boot-starter-webmvc` and `org.springframework.boot:spring-boot-starter-validation`. `kotlin-reflect` and `jackson-module-kotlin` are included for Kotlin serialization support.
- **Dev Environment Isolation (NixOS / Flakes):** The project includes a `flake.nix` providing JDK 25, `just`, and `ktlint`. Gradle caches are scoped strictly to `.gradle-home/` to ensure zero pollution of the host environment.

______________________________________________________________________

## Project Architecture

The codebase follows a strict layered architecture with unidirectional dependency flow — outer layers depend on inner layers, never the reverse:

| Layer | Package | Responsibility |
| :--- | :--- | :--- |
| **API** (Presentation) | `com.nim.game.api` | Inbound REST transport, request/response DTOs, and RFC 9457 error mapping. |
| **Application** | `com.nim.game.application` | Use-case orchestration, session persistence, strategy resolution, and configuration binding. |
| **Domain** | `com.nim.game.domain` | Pure game mechanics, invariants, state transitions, and AI strategies (zero framework dependencies). |

Each layer documents its own contract in a dedicated `README.md` within its package.

______________________________________________________________________

## Configuration

Game defaults are externalized under the `nim.default` prefix and overridable via environment variables or `application.properties`. Explicit request fields always take precedence over these defaults.

| Property | Default | Description |
| :--- | :--- | :--- |
| `nim.default.random-heap-min` | `10` | Lower bound (inclusive) for the randomized heap size when `heaps` is omitted. |
| `nim.default.random-heap-max` | `21` | Upper bound (inclusive) for the randomized heap size when `heaps` is omitted. |
| `nim.default.max-take` | `3` | Maximum matches removable per move. The minimum is fixed at `1`. |
| `nim.default.mode` | `MISERE` | Win condition: `NORMAL` or `MISERE`. |
| `nim.default.difficulty` | `I_AM_TOO_YOUNG_TO_DIE` | AI difficulty level. |

> **Stochastic defaults:** When `heaps` is omitted, a single heap is initialized with a uniform random size in `[random-heap-min, random-heap-max]`. When `startingPlayer` is omitted, `HUMAN` or `COMPUTER` is selected with equal probability.

______________________________________________________________________

## Prerequisites

| Tool | Version | Required for |
|------|---------|--------------|
| JDK | 25 (LTS) | Compilation & runtime |
| `just` | any recent | Command runner |
| `ktlint` | latest | Lint / format |

> **Gradle** is not required — the project ships a Gradle wrapper (`./gradlew`).

**Nix users:** The `flake.nix` provisions all of the above automatically.

- **With [`direnv`](https://direnv.net) (recommended):** Run `direnv allow` once, and the `.envrc` auto-activates the dev shell (via `use flake`) whenever you `cd` into the project. Any `.env` / `.env.local` file is also loaded.
- **Without direnv:** Enter the shell manually via `nix develop`.

**Non-Nix users:** Install the tools independently. The JDK must be available on `$JAVA_HOME` or `$PATH` for the Gradle wrapper to resolve the toolchain.

______________________________________________________________________

## Available Commands

Build and development tasks are managed via [`just`](https://github.com/casey/just). Run `just` or `just list` to display all available recipes.

| Recipe | Description |
|--------|-------------|
| `just list` | List all available recipes |
| `just lint` | Check code style and formatting without modifying files |
| `just fix` | Automatically format code and apply safe style fixes in-place |
| `just test [args]` | Run the test suite (forwards extra Gradle args, e.g. `just test --rerun`) |
| `just run` | Start the application locally |
| `just clean` | Clean build artifacts |
| `just check` | Complete verification: lint check followed by tests |

______________________________________________________________________

## Testing

The JUnit 5 test suite spans all three layers:

- **Domain** (`com.nim.game.domain`): `GameRules` validation, `Game` move resolution and win detection, `HeapState` immutability, `Player` turn alternation, `Difficulty` probability parameterization, and the AI strategy layer (`OptimalStrategy` P-position correctness, `RandomStrategy` legal move bounds).
- **Application** (`com.nim.game.application`): `GameService` orchestration — game creation, AI opening move, the human + AI turn cycle, illegal-move rejection, and `AiStrategyResolver` optimal/random blend resolution.
- **API** (`com.nim.game.api`): `GameController` contract via `@WebMvcTest` — status codes, `Location` header, and RFC 9457 error mapping.

Gradle is configured to log `PASSED` / `SKIPPED` / `FAILED` events with full exception traces. Run the suite via `just test` (or `just check` for lint + tests).

______________________________________________________________________

## Project Inception (Scaffolding)

The initial baseline was generated via the official Spring Initializr API:

```bash
curl https://start.spring.io/starter.tgz \
  -d type=gradle-project-kotlin \
  -d language=kotlin \
  -d bootVersion=4.1.1 \
  -d baseDir=. \
  -d groupId=com.nim \
  -d artifactId=nim-spring-kotlin \
  -d name=nim-spring-kotlin \
  -d packageName=com.nim.game \
  -d javaVersion=25 \
  -d dependencies=web,validation \
  | tar -xzvf -
```
