package com.nim.game.domain.strategy

import com.nim.game.domain.model.ConfigurationError
import com.nim.game.domain.model.Game
import com.nim.game.domain.model.Move

/**
 * Domain policy interface for calculating automated player moves.
 */
fun interface AiStrategy {
    /**
     * Computes the next move for the active player based on the current game state.
     *
     * @param game the current immutable game aggregate
     * @return the determined Move
     * @throws UnsupportedStrategyException if the strategy cannot handle the given game configuration
     * @throws IllegalStateException if the game is already finished or in an invalid state
     */
    fun determineMove(game: Game): Move
}

/**
 * Thrown when an AI strategy encounters an unsupported game configuration.
 */
class UnsupportedStrategyException(
    val error: ConfigurationError,
) : RuntimeException(error.message)
