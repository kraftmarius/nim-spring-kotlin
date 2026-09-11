package com.nim.game.application

import com.nim.game.api.CreateGameRequest
import com.nim.game.domain.model.Difficulty
import com.nim.game.domain.model.Player
import com.nim.game.domain.strategy.UnsupportedStrategyException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class GameServiceTest {
    private lateinit var repository: GameRepository
    private lateinit var service: GameService

    @BeforeEach
    fun setUp() {
        repository = GameRepository()
        service =
            GameService(
                repository = repository,
                strategyResolver = AiStrategyResolver(),
                properties = NimProperties(),
            )
    }

    @Test
    fun `createGame with defaults initializes heap and saves to repository`() {
        val game = service.createGame(CreateGameRequest())

        assertEquals(13, game.heapState.totalMatches())
        assertEquals(Player.HUMAN, game.currentTurn)
        assertNotNull(repository.findById(game.id))
    }

    @Test
    fun `createGame with computer starting executes opening move immediately`() {
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
        val game = service.createGame(CreateGameRequest(heaps = listOf(10)))

        assertThrows<InvalidMoveException> {
            service.makeMove(game.id, heapIndex = 0, matches = 5)
        }
    }
}
