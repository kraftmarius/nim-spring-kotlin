package com.nim.game.api

import com.nim.game.domain.model.Difficulty
import com.nim.game.domain.model.Game
import com.nim.game.domain.model.GameMode
import com.nim.game.domain.model.GameStatus
import com.nim.game.domain.model.Move
import com.nim.game.domain.model.Player
import jakarta.validation.constraints.Min
import java.util.UUID

data class CreateGameRequest(
    val heaps: List<Int>? = null,
    val minTake: Int? = null,
    val maxTake: Int? = null,
    val mode: GameMode? = null,
    val difficulty: Difficulty? = null,
    val startingPlayer: Player? = null,
)

data class MakeMoveRequest(
    @field:Min(value = 1, message = "matches must be at least 1")
    val matches: Int,
    @field:Min(value = 0, message = "heapIndex must be non-negative")
    val heapIndex: Int = 0,
)

data class MoveDto(
    val player: Player,
    val heapIndex: Int,
    val matches: Int,
) {
    companion object {
        fun from(move: Move): MoveDto =
            MoveDto(
                player = move.player,
                heapIndex = move.heapIndex,
                matches = move.matches,
            )
    }
}

data class GameRulesDto(
    val minTake: Int,
    val maxTake: Int,
    val mode: GameMode,
)

data class GameResponse(
    val id: UUID,
    val heaps: List<Int>,
    val currentTurn: Player,
    val status: GameStatus,
    val winner: Player?,
    val difficulty: Difficulty,
    val rules: GameRulesDto,
    val lastComputerMove: MoveDto?,
    val history: List<MoveDto>,
) {
    companion object {
        fun from(game: Game): GameResponse {
            val lastCompMove = game.history.lastOrNull { it.player == Player.COMPUTER }

            return GameResponse(
                id = game.id.value,
                heaps = game.heapState.heaps,
                currentTurn = game.currentTurn,
                status = game.status,
                winner = game.winner,
                difficulty = game.difficulty,
                rules =
                    GameRulesDto(
                        minTake = game.rules.minTake,
                        maxTake = game.rules.maxTake,
                        mode = game.rules.mode,
                    ),
                lastComputerMove = lastCompMove?.let { MoveDto.from(it) },
                history = game.history.map { MoveDto.from(it) },
            )
        }
    }
}
