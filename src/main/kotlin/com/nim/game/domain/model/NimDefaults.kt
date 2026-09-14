package com.nim.game.domain.model

/**
 * Baseline rules and stochastic boundaries for Nim game sessions.
 */
object NimDefaults {
    const val DEFAULT_MAX_TAKE: Int = 3
    val DEFAULT_MODE: GameMode = GameMode.MISERE
    val DEFAULT_DIFFICULTY: Difficulty = Difficulty.I_AM_TOO_YOUNG_TO_DIE

    // Stochastic fallback boundaries when heaps are omitted
    const val RANDOM_HEAP_MIN: Int = 10
    const val RANDOM_HEAP_MAX: Int = 21
}
