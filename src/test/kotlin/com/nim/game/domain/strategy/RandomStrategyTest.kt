package com.nim.game.domain.strategy

import com.nim.game.domain.model.Game
import com.nim.game.domain.model.GameRules
import com.nim.game.domain.model.HeapState
import com.nim.game.domain.model.Player
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.Random

class RandomStrategyTest {
    @Test
    fun `should only select from non-empty heaps`() {
        val seededRandom = Random(12345)
        val strategy = RandomStrategy(seededRandom)

        // Heap 0 and 2 are exhausted; only index 1 has matches
        val game =
            Game(
                heapState = HeapState(listOf(0, 5, 0)),
                currentTurn = Player.COMPUTER,
            )

        repeat(20) {
            val move = strategy.determineMove(game)
            assertEquals(1, move.heapIndex)
            assertTrue(move.matches in 1..3)
            assertEquals(Player.COMPUTER, move.player)
        }
    }

    @Test
    fun `should strictly respect heap bounds when heap is smaller than maxTake`() {
        val seededRandom = Random(42)
        val strategy = RandomStrategy(seededRandom)

        val game =
            Game(
                heapState = HeapState.single(2),
                rules = GameRules(minTake = 1, maxTake = 3),
                currentTurn = Player.COMPUTER,
            )

        repeat(20) {
            val move = strategy.determineMove(game)
            assertTrue(move.matches in 1..2)
        }
    }
}
