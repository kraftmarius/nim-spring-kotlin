package com.nim.game.domain.model

/**
 * Aggregate root encapsulating game state, validation, turn resolution and move history.
 */
data class Game(
    val id: GameId = GameId.random(),
    val heapState: HeapState,
    val rules: GameRules = GameRules(),
    val difficulty: Difficulty = Difficulty.I_AM_TOO_YOUNG_TO_DIE,
    val currentTurn: Player = Player.HUMAN,
    val status: GameStatus = GameStatus.IN_PROGRESS,
    val winner: Player? = null,
    val history: List<Move> = emptyList(),
) {
    init {
        if (heapState.isEmpty()) {
            require(status == GameStatus.FINISHED) { "Game with empty heaps must be marked as FINISHED." }
            require(winner != null) { "Finished game must declare a winner." }
        }
    }

    fun isOver(): Boolean = status == GameStatus.FINISHED

    /**
     * Applies a player move against the current game state.
     */
    fun applyMove(move: Move): MoveResult {
        if (isOver()) {
            return MoveResult.Failure(InvalidMoveError.GameAlreadyFinished)
        }

        if (move.player != currentTurn) {
            return MoveResult.Failure(InvalidMoveError.NotPlayersTurn(currentTurn, move.player))
        }

        if (move.heapIndex !in heapState.heaps.indices) {
            return MoveResult.Failure(InvalidMoveError.InvalidHeapIndex(move.heapIndex, heapState.heaps.size))
        }

        val currentHeapSize = heapState.heaps[move.heapIndex]
        if (currentHeapSize == 0) {
            return MoveResult.Failure(InvalidMoveError.HeapAlreadyEmpty(move.heapIndex))
        }

        val maxAllowed = minOf(rules.maxTake, currentHeapSize)
        val minAllowed = GameRules.MIN_TAKE

        if (move.matches !in minAllowed..maxAllowed) {
            return MoveResult.Failure(InvalidMoveError.TakeOutOfAllowedRange(move.matches, minAllowed, maxAllowed))
        }

        val updatedHeapState = heapState.take(move.heapIndex, move.matches)
        val updatedHistory = history + move

        return if (updatedHeapState.isEmpty()) {
            val resolvedWinner =
                when (rules.mode) {
                    GameMode.MISERE -> move.player.next()
                    GameMode.NORMAL -> move.player
                }

            MoveResult.Success(
                copy(
                    heapState = updatedHeapState,
                    status = GameStatus.FINISHED,
                    winner = resolvedWinner,
                    history = updatedHistory,
                ),
            )
        } else {
            MoveResult.Success(
                copy(
                    heapState = updatedHeapState,
                    currentTurn = currentTurn.next(),
                    history = updatedHistory,
                ),
            )
        }
    }
}
