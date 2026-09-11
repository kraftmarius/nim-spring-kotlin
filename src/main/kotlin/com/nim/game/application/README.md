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

   - Default game rules (`initialMatches`, `minTake`, `maxTake`, `mode`, `difficulty`, `startingPlayer`) are declared in `NimProperties` via `@ConfigurationProperties(prefix = "nim.default")`.
   - Explicit request values take precedence; omitted fields seamlessly fall back to configuration defaults.

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

When `createGame` is invoked with `startingPlayer == Player.COMPUTER`, the service immediately computes and applies the opening computer move before persisting the initial state.

______________________________________________________________________

## Strategy Resolution (`AiStrategyResolver`)

The resolver maps domain `Difficulty` profiles to concrete `AiStrategy` instances:

| Difficulty | Resolved Strategy | Behavior |
| :--- | :--- | :--- |
| `I_AM_TOO_YOUNG_TO_DIE` | `RandomStrategy` | Uniform stochastic move selection across all non-empty heaps. Multi-heap supported. |
| `HURT_ME_PLENTY` | `ProbabilisticStrategy` | 50/50 composite delegating between optimal math and random play. Single-heap only; multi-heap requests rejected with `UnsupportedStrategyException`. |
| `NIGHTMARE` | `OptimalStrategy` | Pure modulo arithmetic against Nim P-positions. Single-heap only; multi-heap requests rejected with `UnsupportedStrategyException`. |

______________________________________________________________________

## Tactical Component Map

| Component | Responsibility |
| :--- | :--- |
| `GameService` | Orchestrates game creation, moves, AI turn triggering, and repository updates. |
| `GameRepository` | In-memory `ConcurrentHashMap` store for active and completed game sessions. |
| `AiStrategyResolver` | Factory resolving `Difficulty` enums into stateless `AiStrategy` instances. |
| `NimProperties` | `@ConfigurationProperties` data class holding default game settings. |
| `GameNotFoundException` | Thrown when an operation targets a non-existent `GameId`. |
| `InvalidMoveException` | Wraps domain `InvalidMoveError` into an application-level exception. |
