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
 * Available game difficulty levels mapped to AI strategies.
 */
enum class Difficulty {
    I_AM_TOO_YOUNG_TO_DIE,
    HURT_ME_PLENTY,
    NIGHTMARE,
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
