package com.nim.game.domain.strategy

import com.nim.game.domain.model.Game
import com.nim.game.domain.model.HeapState
import com.nim.game.domain.model.Move
import com.nim.game.domain.model.Player
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.Random

class ProbabilisticStrategyTest {
    private val game =
        Game(
            heapState = HeapState.single(10),
            currentTurn = Player.COMPUTER,
        )

    private val primaryMove = Move(Player.COMPUTER, 0, 3)
    private val fallbackMove = Move(Player.COMPUTER, 0, 1)

    private val primaryStub = AiStrategy { primaryMove }
    private val fallbackStub = AiStrategy { fallbackMove }

    @Test
    fun `should always delegate to primary when probability is 1`() {
        val strategy =
            ProbabilisticStrategy(
                primary = primaryStub,
                fallback = fallbackStub,
                primaryProbability = 1.0,
            )

        val move = strategy.determineMove(game)

        assertEquals(primaryMove, move)
    }

    @Test
    fun `should always delegate to fallback when probability is 0`() {
        val strategy =
            ProbabilisticStrategy(
                primary = primaryStub,
                fallback = fallbackStub,
                primaryProbability = 0.0,
            )

        val move = strategy.determineMove(game)

        assertEquals(fallbackMove, move)
    }

    @Test
    fun `should distribute moves according to probability threshold`() {
        // Deterministic sequence: Random(0) produces known sequence of nextDouble()
        val strategy =
            ProbabilisticStrategy(
                primary = primaryStub,
                fallback = fallbackStub,
                primaryProbability = 0.5,
                random = Random(0),
            )

        val moves = (1..100).map { strategy.determineMove(game) }

        val primaryCount = moves.count { it == primaryMove }
        val fallbackCount = moves.count { it == fallbackMove }

        // At 0.5 with 100 samples, both strategies must have been chosen
        assertTrue(primaryCount > 30)
        assertTrue(fallbackCount > 30)
        assertEquals(100, primaryCount + fallbackCount)
    }

    private fun assertTrue(condition: Boolean) {
        org.junit.jupiter.api.Assertions
            .assertTrue(condition)
    }
}
