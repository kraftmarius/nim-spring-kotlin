package com.nim.game.domain.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class GameTest {
    @Test
    fun `should advance turn and reduce heap on valid human move`() {
        val game = Game(heapState = HeapState.single(10), currentTurn = Player.HUMAN)

        val result = game.applyMove(Move(player = Player.HUMAN, heapIndex = 0, matches = 3))

        val success = assertInstanceOf(MoveResult.Success::class.java, result)

        assertEquals(7, success.game.heapState.totalMatches())
        assertEquals(Player.COMPUTER, success.game.currentTurn)
        assertEquals(GameStatus.IN_PROGRESS, success.game.status)
        assertNull(success.game.winner)
    }

    @Test
    fun `should reject move when acting out of turn`() {
        val game = Game(heapState = HeapState.single(10), currentTurn = Player.HUMAN)

        val result = game.applyMove(Move(player = Player.COMPUTER, heapIndex = 0, matches = 1))

        val failure = assertInstanceOf(MoveResult.Failure::class.java, result)
        val error = assertInstanceOf(InvalidMoveError.NotPlayersTurn::class.java, failure.error)

        assertEquals(Player.HUMAN, error.expected)
        assertEquals(Player.COMPUTER, error.actual)
    }

    @Test
    fun `should reject move on invalid heap index`() {
        val game = Game(heapState = HeapState.single(10), currentTurn = Player.HUMAN)

        val result = game.applyMove(Move(player = Player.HUMAN, heapIndex = 1, matches = 1))

        val failure = assertInstanceOf(MoveResult.Failure::class.java, result)
        val error = assertInstanceOf(InvalidMoveError.InvalidHeapIndex::class.java, failure.error)

        assertEquals(1, error.index)
        assertEquals(1, error.totalHeaps)
    }

    @Test
    fun `should reject move on exhausted heap`() {
        val game =
            Game(
                heapState = HeapState(listOf(0, 5)),
                currentTurn = Player.HUMAN,
            )

        val result = game.applyMove(Move(player = Player.HUMAN, heapIndex = 0, matches = 1))

        val failure = assertInstanceOf(MoveResult.Failure::class.java, result)
        val error = assertInstanceOf(InvalidMoveError.HeapAlreadyEmpty::class.java, failure.error)

        assertEquals(0, error.index)
    }

    @Test
    fun `should reject move when matches exceed rules maxTake`() {
        val game =
            Game(
                heapState = HeapState.single(10),
                rules = GameRules(maxTake = 3),
                currentTurn = Player.HUMAN,
            )

        val result = game.applyMove(Move(player = Player.HUMAN, heapIndex = 0, matches = 4))

        val failure = assertInstanceOf(MoveResult.Failure::class.java, result)
        val error = assertInstanceOf(InvalidMoveError.TakeOutOfAllowedRange::class.java, failure.error)

        assertEquals(4, error.requested)
        assertEquals(1, error.minAllowed)
        assertEquals(3, error.maxAllowed)
    }

    @Test
    fun `should reject move when matches exceed remaining heap size even if within maxTake`() {
        val game =
            Game(
                heapState = HeapState.single(2),
                rules = GameRules(maxTake = 3),
                currentTurn = Player.HUMAN,
            )

        val result = game.applyMove(Move(player = Player.HUMAN, heapIndex = 0, matches = 3))

        val failure = assertInstanceOf(MoveResult.Failure::class.java, result)
        val error = assertInstanceOf(InvalidMoveError.TakeOutOfAllowedRange::class.java, failure.error)

        assertEquals(3, error.requested)
        assertEquals(1, error.minAllowed)
        assertEquals(2, error.maxAllowed)
    }

    @Test
    fun `should reject move on finished game`() {
        val finishedGame =
            Game(
                heapState = HeapState.single(0),
                status = GameStatus.FINISHED,
                winner = Player.COMPUTER,
            )

        val result = finishedGame.applyMove(Move(player = Player.HUMAN, heapIndex = 0, matches = 1))

        val failure = assertInstanceOf(MoveResult.Failure::class.java, result)

        assertInstanceOf(InvalidMoveError.GameAlreadyFinished::class.java, failure.error)
    }

    @Test
    fun `misere mode declares opponent as winner when last match is cleared`() {
        val game =
            Game(
                heapState = HeapState.single(2),
                rules = GameRules(maxTake = 3, mode = GameMode.MISERE),
                currentTurn = Player.HUMAN,
            )

        val result = game.applyMove(Move(player = Player.HUMAN, heapIndex = 0, matches = 2))

        val success = assertInstanceOf(MoveResult.Success::class.java, result)

        assertTrue(success.game.isOver())
        assertEquals(GameStatus.FINISHED, success.game.status)
        assertEquals(Player.COMPUTER, success.game.winner)
    }

    @Test
    fun `normal mode declares moving player as winner when last match is cleared`() {
        val game =
            Game(
                heapState = HeapState.single(2),
                rules = GameRules(maxTake = 3, mode = GameMode.NORMAL),
                currentTurn = Player.HUMAN,
            )

        val result = game.applyMove(Move(player = Player.HUMAN, heapIndex = 0, matches = 2))

        val success = assertInstanceOf(MoveResult.Success::class.java, result)

        assertTrue(success.game.isOver())
        assertEquals(GameStatus.FINISHED, success.game.status)
        assertEquals(Player.HUMAN, success.game.winner)
    }

    @Test
    fun `should fail instantiation if finished game has no winner`() {
        assertThrows<IllegalArgumentException> {
            Game(
                heapState = HeapState.single(0),
                status = GameStatus.FINISHED,
                winner = null,
            )
        }
    }
}
