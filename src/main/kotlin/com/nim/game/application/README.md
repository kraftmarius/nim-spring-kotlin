# Application Layer — Orchestration & State Management

This directory contains the application layer for the Nim game engine. It acts as the use-case orchestrator, coordinating domain entities, AI strategies, default configurations, and session persistence.

## Package Structure

```
com.nim.game.application
├── GameService.kt          # Primary application service (use-case orchestration)
├── GameRepository.kt       # Thread-safe in-memory session persistence
├── AiStrategyResolver.kt   # Strategy factory mapping difficulty to AI policies
├── NimProperties.kt        # Configuration properties binding defaults
└── GameExceptions.kt       # Application-level exceptions translated to HTTP
```

______________________________________________________________________

## Architectural Principles

1. **Use-Case Orchestration (No Business Rules Leakage):**

   - The service enforces execution workflows, not core game mechanics. State validation, turn verification, and victory resolution remain strictly inside the `domain` layer.
   - Converts external request commands into domain value objects and routes execution through the `Game` aggregate.

1. **Synchronous Turn-Cycle Coordination:**

   - To provide optimal CLI/REST ergonomics, a human move and the ensuing computer counter-move are executed sequentially within a single atomic service operation.
   - Prevents inconsistent intermediate states and reduces roundtrips for clients.

1. **Thread-Safe In-Memory State:**

   - Session storage relies on a `ConcurrentHashMap` inside `GameRepository`.
   - Thread safety is achieved through immutable aggregate instances: state updates replace references atomically.

1. **12-Factor Configuration Hierarchy:**

    - Default game parameters (`randomHeapMin`, `randomHeapMax`, `maxTake`, `mode`, `difficulty`) are declared in `NimProperties` via `@ConfigurationProperties(prefix = "nim.default")`.
    - Explicit request values take precedence. Omitted `heaps` and `startingPlayer` are resolved stochastically (random heap size, random opening player); other omitted fields fall back to configuration defaults.

______________________________________________________________________

## Turn Cycle Workflow

The `makeMove` workflow ensures that human actions immediately trigger AI responses unless the game reaches a terminal state:

```
+-----------------------------------------------------------------------------+
|                          GameService.makeMove(...)                          |
+-----------------------------------------------------------------------------+
                                       |
                                       v
                     [ 1. Load Game from GameRepository ]
                                       |
                                       v
                    [ 2. Apply Human Move: game.applyMove ]
                                       |
                   +-------------------+-------------------+
                   |                                       |
               [ Failure ]                             [ Success ]
                   |                                       |
                   v                                       v
         throw InvalidMoveException               [ 3. Is Game Over? ]
                                                   /                \
                                                (Yes)               (No)
                                                 /                    \
                                                v                      v
                                      [ Save & Return ]     [ 4. Resolve AI Strategy ]
                                                                       |
                                                                       v
                                                            [ 5. Compute AI Move ]
                                                                       |
                                                                       v
                                                            [ 6. Apply AI Move ]
                                                                       |
                                                                       v
                                                            [ 7. Save & Return Game ]
```

### Starting Player Resolution

When `startingPlayer` is explicitly set to `COMPUTER` or randomly resolved to `COMPUTER` (when omitted), the service immediately computes and applies the opening computer move before persisting the initial state.

______________________________________________________________________

## Strategy Resolution (`AiStrategyResolver`)

The resolver maps each `Difficulty` to a concrete `AiStrategy` based on a single parameter: `Difficulty.optimalProbability`. Blended difficulties (0.0 < p < 1.0) coin-flip between optimal math and random play per move; the poles short-circuit to the pure strategies, consuming no randomness.

| Difficulty | `optimalProbability` | Resolved Behavior |
| :--- | :--- | :--- |
| `I_AM_TOO_YOUNG_TO_DIE` | `0.0` | Pure `RandomStrategy` — uniform stochastic move selection across all non-empty heaps. Multi-heap supported. |
| `HURT_ME_PLENTY` | `0.5` | Blend: optimal move with probability 0.5, otherwise random. Single-heap only; multi-heap requests rejected with `UnsupportedStrategyException`. |
| `NIGHTMARE` | `1.0` | Pure `OptimalStrategy` — deterministic modulo arithmetic against Nim P-positions. Single-heap only; multi-heap requests rejected with `UnsupportedStrategyException`. |

______________________________________________________________________

## Tactical Component Map

| Component | Responsibility |
| :--- | :--- |
| `GameService` | Orchestrates game creation, moves, AI turn triggering, and repository updates. |
| `GameRepository` | In-memory `ConcurrentHashMap` store for active and completed game sessions. |
| `AiStrategyResolver` | Resolves `Difficulty` enums into `AiStrategy` instances: pure strategies at the probability poles, an optimal/random blend in between. |
| `NimProperties` | `@ConfigurationProperties` data class holding default game settings. |
| `GameNotFoundException` | Thrown when an operation targets a non-existent `GameId`. |
| `InvalidMoveException` | Wraps domain `InvalidMoveError` into an application-level exception. |
