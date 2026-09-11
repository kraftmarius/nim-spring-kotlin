package com.nim.game.domain.model

/**
 * Represents an action executed by a player.
 */
data class Move(
    val player: Player,
    val heapIndex: Int = 0,
    val matches: Int,
) {
    init {
        require(heapIndex >= 0) { "heapIndex must be non-negative." }
        require(matches >= 1) { "matches must be at least 1." }
    }
}

/**
 * Outcome of executing a move on the game aggregate.
 */
sealed interface MoveResult {
    data class Success(
        val game: Game,
    ) : MoveResult

    data class Failure(
        val error: InvalidMoveError,
    ) : MoveResult
}
