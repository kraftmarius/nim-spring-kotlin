package com.nim.game.domain.model

import java.util.UUID

/**
 * Represents the participants of the game.
 */
enum class Player {
    HUMAN,
    COMPUTER,
    ;

    /**
     * Toggles to the next player.
     */
    fun next(): Player =
        when (this) {
            HUMAN -> COMPUTER
            COMPUTER -> HUMAN
        }
}

/**
 * Rule configuration for the end-game win condition.
 */
enum class GameMode {
    MISERE,
    NORMAL,
}

/**
 * Available game difficulty levels.
 *
 * Each difficulty is a point on a single skill axis: [optimalProbability] is the probability
 * that the AI plays the optimal move on any given turn. 0.0 always plays randomly,
 * 1.0 always plays optimally.
 */
enum class Difficulty(
    val optimalProbability: Double,
) {
    I_AM_TOO_YOUNG_TO_DIE(0.0),
    HURT_ME_PLENTY(0.5),
    NIGHTMARE(1.0),
}

/**
 * Lifecycle state of a match.
 */
enum class GameStatus {
    IN_PROGRESS,
    FINISHED,
}

/**
 * Strongly typed game identifier.
 */
@JvmInline
value class GameId(
    val value: UUID,
) {
    companion object {
        fun random(): GameId = GameId(UUID.randomUUID())
    }
}
