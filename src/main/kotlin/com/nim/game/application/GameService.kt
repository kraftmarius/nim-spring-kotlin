package com.nim.game.application

import com.nim.game.api.CreateGameRequest
import com.nim.game.domain.model.ConfigurationError
import com.nim.game.domain.model.Game
import com.nim.game.domain.model.GameId
import com.nim.game.domain.model.GameRules
import com.nim.game.domain.model.HeapState
import com.nim.game.domain.model.Move
import com.nim.game.domain.model.MoveResult
import com.nim.game.domain.model.Player
import com.nim.game.domain.strategy.UnsupportedStrategyException
import org.springframework.stereotype.Service
import java.util.random.RandomGenerator

@Service
class GameService(
    private val repository: GameRepository,
    private val strategyResolver: AiStrategyResolver,
    private val properties: NimProperties,
    private val random: RandomGenerator = RandomGenerator.getDefault(),
) {
    fun createGame(request: CreateGameRequest): Game {
        val resolvedHeaps =
            request.heaps
                ?: listOf(random.nextInt(properties.randomHeapMin, properties.randomHeapMax + 1))

        val resolvedStartingPlayer =
            request.startingPlayer
                ?: if (random.nextBoolean()) Player.HUMAN else Player.COMPUTER

        val maxTake = request.maxTake ?: properties.maxTake
        val mode = request.mode ?: properties.mode
        val difficulty = request.difficulty ?: properties.difficulty

        // Reject degenerate initial configurations: a fresh game cannot contain an empty heap
        require(resolvedHeaps.all { it > 0 }) { "Initial heaps must be positive: $resolvedHeaps" }

        // Reject unsupported multi-heap configurations immediately:
        // multi-heap play is unsupported whenever optimal play may occur
        if (resolvedHeaps.size > 1 && difficulty.optimalProbability > 0.0) {
            throw UnsupportedStrategyException(
                ConfigurationError.MultiHeapAiNotSupported(difficulty, resolvedHeaps.size),
            )
        }

        val initialGame =
            Game(
                heapState = HeapState(resolvedHeaps),
                currentTurn = resolvedStartingPlayer,
                rules = GameRules(maxTake = maxTake, mode = mode),
                difficulty = difficulty,
            )

        val gameToSave =
            if (resolvedStartingPlayer == Player.COMPUTER) {
                val strategy = strategyResolver.resolve(difficulty)
                val computerMove = strategy.determineMove(initialGame)

                when (val result = initialGame.applyMove(computerMove)) {
                    is MoveResult.Success -> result.game
                    is MoveResult.Failure -> throw InvalidMoveException(result.error)
                }
            } else {
                initialGame
            }

        return repository.save(gameToSave)
    }

    fun getAllGames(): List<Game> = repository.findAll()

    fun getGame(id: GameId): Game = repository.findById(id) ?: throw GameNotFoundException(id)

    fun makeMove(
        id: GameId,
        heapIndex: Int,
        matches: Int,
    ): Game {
        val currentGame = getGame(id)

        val humanMove = Move(player = Player.HUMAN, heapIndex = heapIndex, matches = matches)
        val humanResult = currentGame.applyMove(humanMove)

        val afterHumanGame =
            when (humanResult) {
                is MoveResult.Success -> humanResult.game
                is MoveResult.Failure -> throw InvalidMoveException(humanResult.error)
            }

        if (afterHumanGame.isOver()) {
            return repository.save(afterHumanGame)
        }

        val strategy = strategyResolver.resolve(afterHumanGame.difficulty)
        val computerMove = strategy.determineMove(afterHumanGame)

        val afterComputerGame =
            when (val computerResult = afterHumanGame.applyMove(computerMove)) {
                is MoveResult.Success -> computerResult.game
                is MoveResult.Failure -> throw InvalidMoveException(computerResult.error)
            }

        return repository.save(afterComputerGame)
    }
}
