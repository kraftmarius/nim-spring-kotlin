package com.nim.game.application

import com.nim.game.domain.model.Game
import com.nim.game.domain.model.GameId
import com.nim.game.domain.model.GameStatus
import com.nim.game.domain.model.HeapState
import com.nim.game.domain.model.Player
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class GameRepositoryTest {
    private val repository = GameRepository()

    @Test
    fun `totalGames counts all stored games`() {
        repository.save(Game(id = GameId.random(), heapState = HeapState.single(5), currentTurn = Player.HUMAN))
        repository.save(Game(id = GameId.random(), heapState = HeapState.single(3), currentTurn = Player.HUMAN))

        assertEquals(2, repository.totalGames())
    }

    @Test
    fun `activeGames excludes finished games`() {
        val activeGame = Game(id = GameId.random(), heapState = HeapState.single(5), currentTurn = Player.HUMAN)
        val finishedGame =
            Game(
                id = GameId.random(),
                heapState = HeapState(listOf(0)),
                currentTurn = Player.HUMAN,
                status = GameStatus.FINISHED,
                winner = Player.HUMAN,
            )

        repository.save(activeGame)
        repository.save(finishedGame)

        assertEquals(2, repository.totalGames())
        assertEquals(1, repository.activeGames())
    }

    @Test
    fun `findAll returns all stored games`() {
        val game1 = Game(id = GameId.random(), heapState = HeapState.single(5), currentTurn = Player.HUMAN)
        val game2 = Game(id = GameId.random(), heapState = HeapState.single(3), currentTurn = Player.COMPUTER)

        repository.save(game1)
        repository.save(game2)

        val all = repository.findAll()

        assertEquals(2, all.size)
        assertTrue(all.any { it.id == game1.id })
        assertTrue(all.any { it.id == game2.id })
    }
}
