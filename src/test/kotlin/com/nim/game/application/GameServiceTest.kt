package com.nim.game.application

import com.nim.game.api.CreateGameRequest
import com.nim.game.domain.model.Difficulty
import com.nim.game.domain.model.Player
import com.nim.game.domain.strategy.UnsupportedStrategyException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.Random

class GameServiceTest {
    private lateinit var repository: GameRepository
    private lateinit var properties: NimProperties

    @BeforeEach
    fun setUp() {
        repository = GameRepository()
        properties = NimProperties()
    }

    @Test
    fun `createGame with empty request randomizes heap and starting player`() {
        val service =
            GameService(
                repository = repository,
                strategyResolver = AiStrategyResolver(),
                properties = properties,
                random = Random(42),
            )

        val game = service.createGame(CreateGameRequest())

        // Initial matches before any computer opening move
        val initialMatches = game.heapState.totalMatches() + game.history.sumOf { it.matches }
        assertTrue(initialMatches in properties.randomHeapMin..properties.randomHeapMax)
        assertNotNull(repository.findById(game.id))

        // If computer was chosen to start, it must have already executed its move
        if (game.history.isNotEmpty()) {
            assertEquals(1, game.history.size)
            assertEquals(Player.COMPUTER, game.history[0].player)
            assertEquals(Player.HUMAN, game.currentTurn)
        }
    }

    @Test
    fun `createGame with explicit parameters disables randomization`() {
        val service =
            GameService(
                repository = repository,
                strategyResolver = AiStrategyResolver(),
                properties = properties,
                random = Random(0),
            )

        val game =
            service.createGame(
                CreateGameRequest(
                    heaps = listOf(7),
                    startingPlayer = Player.HUMAN,
                ),
            )

        assertEquals(listOf(7), game.heapState.heaps)
        assertEquals(Player.HUMAN, game.currentTurn)
        assertEquals(0, game.history.size)
    }

    @Test
    fun `createGame with computer starting executes opening move immediately`() {
        val service =
            GameService(
                repository = repository,
                strategyResolver = AiStrategyResolver(),
                properties = properties,
            )

        val game =
            service.createGame(
                CreateGameRequest(
                    heaps = listOf(10),
                    difficulty = Difficulty.NIGHTMARE,
                    startingPlayer = Player.COMPUTER,
                ),
            )

        assertEquals(Player.HUMAN, game.currentTurn)
        assertEquals(1, game.history.size)
        assertEquals(Player.COMPUTER, game.history[0].player)
    }

    @Test
    fun `createGame rejects multi-heap with NIGHTMARE immediately`() {
        val service =
            GameService(
                repository = repository,
                strategyResolver = AiStrategyResolver(),
                properties = properties,
            )

        val request =
            CreateGameRequest(
                heaps = listOf(3, 4, 5),
                difficulty = Difficulty.NIGHTMARE,
            )

        assertThrows<UnsupportedStrategyException> {
            service.createGame(request)
        }
    }

    @Test
    fun `makeMove executes human move followed by computer counter-move`() {
        val service =
            GameService(
                repository = repository,
                strategyResolver = AiStrategyResolver(),
                properties = properties,
            )

        val game =
            service.createGame(
                CreateGameRequest(
                    heaps = listOf(10),
                    difficulty = Difficulty.NIGHTMARE,
                    startingPlayer = Player.HUMAN,
                ),
            )

        val updatedGame = service.makeMove(game.id, heapIndex = 0, matches = 1)

        assertEquals(2, updatedGame.history.size)
        assertEquals(Player.HUMAN, updatedGame.history[0].player)
        assertEquals(Player.COMPUTER, updatedGame.history[1].player)
        assertEquals(Player.HUMAN, updatedGame.currentTurn)
    }

    @Test
    fun `makeMove throws InvalidMoveException on illegal take`() {
        val service =
            GameService(
                repository = repository,
                strategyResolver = AiStrategyResolver(),
                properties = properties,
            )

        val game =
            service.createGame(
                CreateGameRequest(
                    heaps = listOf(10),
                    startingPlayer = Player.HUMAN,
                ),
            )

        assertThrows<InvalidMoveException> {
            service.makeMove(game.id, heapIndex = 0, matches = 5)
        }
    }
}
