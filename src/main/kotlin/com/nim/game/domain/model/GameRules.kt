package com.nim.game.domain.model

/**
 * Invariant rules enforced during a game session.
 *
 * The minimum take is a fixed constant ([MIN_TAKE]) by the rules of Nim: a player must always
 * remove at least one match per move. Only the maximum take is configurable.
 */
data class GameRules(
    val maxTake: Int = NimDefaults.DEFAULT_MAX_TAKE,
    val mode: GameMode = NimDefaults.DEFAULT_MODE,
) {
    init {
        require(maxTake > MIN_TAKE) { "maxTake ($maxTake) must be greater than $MIN_TAKE" }
    }

    companion object {
        /** Minimum matches removable per move. Fixed invaraint by the rules of Nim. */
        const val MIN_TAKE: Int = 1
    }
}
