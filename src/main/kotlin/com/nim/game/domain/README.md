# Domain Layer — Nim Architecture & Specifications

This directory contains the pure domain layer for the Nim game engine. It encapsulates all core game mechanics, invariants, state transitions, and AI strategy logic with **zero framework dependencies**.

## Package Structure

```
com.nim.game.domain
├── model/          # Core domain model (aggregate root, value objects, types)
└── strategy/       # Pluggable AI strategy implementations
```

---

## Architectural Principles

1. **Pure Kotlin (Zero Framework Pollution):**
   - No Spring annotations (`@Component`, `@Service`), persistence decorators, or serialization tags (`@JsonProperty`).
   - The domain remains completely decoupled from HTTP transport layers and serialization formats.

2. **Immutability & Persistent State:**
   - Every domain model is an immutable `data class` or `@JvmInline value class`.
   - State transformations are pure functions. Instead of mutating internal variables in-place, operations (such as `applyMove`) yield a new instance via Kotlin's compiler-generated `copy()` method.
   - Concurrency safety is guaranteed at the model level, eliminating shared mutable state.

3. **Total Functions & Typed Error Handling:**
   - Business operations do not use unchecked exceptions (`IllegalArgumentException`) for expected user mistakes.
   - Operations return explicit, sealed outcome types (`MoveResult`). This forces the calling application service to handle both success and error branches exhaustively at compile-time.

---

## Invariant Validation Hierarchy

Strictly separate **intrinsic structural validity** from **contextual business rules**:

```
  +---------------------------------------------------------------+
  |                      Game (Aggregate Root)                    |
  | Contextual Rules: turn matching, maxTake constraint,          |
  | win/loss conditions, active/finished game state               |
  +-------------------------------+-------------------------------+
                                  |
          +-----------------------+-----------------------+
          |                                               |
+---------v-------------+                       +---------v-------------+
|       HeapState       |                       |         Move          |
| Structural:           |                       | Structural:           |
| Non-negative match    |                       | Positive target heap, |
| counts, physical      |                       | at least 1 match      |
| exhaustion bounds     |                       | (No rule awareness)   |
+-----------------------+                       +-----------------------+
```

### 1. Structural Invariants (Value Objects)
- **`HeapState`:** Enforces physical properties of match piles. A heap cannot contain negative matches, and players cannot draw more matches than physically exist in a given heap. It remains agnostic of turn bounds or game modes.
- **`Move`:** Represents an execution intent. Enforces that a move targets a non-negative heap index and requests `>= 1` matches. It intentionally does not enforce rules (such as `maxTake`), allowing the application layer to return descriptive domain errors rather than crashing on constructor initialization.

### 2. Contextual Invariants (Aggregate Root)
- **`Game`:** Owns the combined context of `HeapState`, `GameRules`, and player turns.
- Only the `Game` aggregate decides whether a move is legal relative to `rules.maxTake` (with the minimum take fixed at `GameRules.MIN_TAKE = 1`), current game status, and player identity.
- Validation failures at this level produce typed `InvalidMoveError` representations for API translation (RFC 9457 Problem Details).

---

## Game Rules & Victory Conditions

The domain aggregate enforces the terminal state solely based on the remaining matches and the configured `GameMode`:

* **Terminal Condition:** The game terminates immediately when `heapState.isEmpty()` (`totalMatches == 0`).
* **Misère Play (Default):** The player who executed the move that reduced the matches to `0` **loses**. The opposing player is declared the winner (`winner = move.player.next()`).
* **Normal Play:** The player who executed the move that reduced the matches to `0` **wins** (`winner = move.player`).

State validation inside `Game.applyMove(...)` evaluates only **legality** (range `1..maxTake`, active turn, valid heap index).

### State Transitions (`applyMove`)
1. **Validation:** Checks game lifecycle (`IN_PROGRESS`), active turn (`currentTurn == move.player`), and rule boundaries.
2. **Execution:** Computes `updatedHeapState = heapState.take(move.heapIndex, move.matches)`.
3. **End-Condition Evaluation:**
   - If `updatedHeapState.isEmpty()`:
     - Marks status as `FINISHED`.
     - In `MISERE` mode: `winner = move.player.next()` (opposing player wins).
     - In `NORMAL` mode: `winner = move.player` (moving player wins).
   - If matches remain:
     - Preserves `IN_PROGRESS`.
     - Toggles turn: `currentTurn = currentTurn.next()`.

---

## Strategy Layer (`strategy/`)

The `AiStrategy` fun interface defines the contract for computing automated player moves. Implementations are pluggable and stateless. The application-layer `AiStrategyResolver` composes them into difficulty behavior: every `Difficulty` is a blend of optimal and random play parameterized by `Difficulty.optimalProbability`.

| Strategy | Algorithm | Role in Difficulty resolution |
| :--- | :--- | :--- |
| `OptimalStrategy` | Modulo arithmetic against P-positions (single-heap only); stochastic fallback in losing P-positions. Multi-heap is guarded by a `check` invariant (`IllegalStateException`) — user-facing rejection is raised upstream by the application layer. | Pure pole (`NIGHTMARE`, probability 1.0) and optimal component of blended difficulties. |
| `RandomStrategy` | Uniform random selection across legal moves (heap index + take count bounded by rules and physical heap size). | Pure pole (`I_AM_TOO_YOUNG_TO_DIE`, probability 0.0) and random component of blended difficulties. |

### Error Contract

Strategies enforce supported-scope invariants via `check(...)` (throwing `IllegalStateException`); the application layer surfaces `UnsupportedStrategyException` (wrapping a `ConfigurationError`) to callers when a configuration falls outside a strategy's supported scope (e.g. multi-heap with optimal play). This is distinct from `InvalidMoveError`, which represents illegal *player* actions.

---

## Tactical Component Map

| Component | Package | Responsibility |
| :--- | :--- | :--- |
| `GameId` | `model` | `@JvmInline` value class wrapping `UUID` for type-safe identity. |
| `GameRules` | `model` | Value object encapsulating `maxTake` and `GameMode` (`maxTake > 1`). The minimum take is a fixed constant `MIN_TAKE = 1`. |
| `HeapState` | `model` | Value object tracking match quantities per heap index. |
| `Move` | `model` | Value object capturing player action intent. |
| `Game` | `model` | Aggregate Root orchestrating gameplay and state transitions. |
| `DomainError` | `model` | Sealed hierarchy of domain and rule violations (`InvalidMoveError`, `ConfigurationError`). |
| `Difficulty` | `model` | Difficulty level selector. Each member carries `optimalProbability` — the probability the AI plays the optimal move (0.0 = pure random, 1.0 = pure optimal). |
| `AiStrategy` | `strategy` | Fun interface defining the contract for automated move computation. |
| `OptimalStrategy` | `strategy` | Mathematically optimal move via P-position targeting (single-heap). |
| `RandomStrategy` | `strategy` | Uniform random legal move selection. |
| `UnsupportedStrategyException` | `strategy` | Thrown when a strategy cannot handle the game configuration. |
