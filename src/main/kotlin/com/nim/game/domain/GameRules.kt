package com.nim.game.domain

/**
 * Invariant rules enforced during a game session.
 */
data class GameRules(
    val minTake: Int = 1,
    val maxTake: Int = 3,
    val mode: GameMode = GameMode.MISERE,
) {
    init {
        require(minTake >= 1) { "minTake must be at least 1, but was $minTake" }
        require(maxTake > minTake) { "maxTake ($maxTake) must be greater than minTake ($minTake)" }
    }
}
