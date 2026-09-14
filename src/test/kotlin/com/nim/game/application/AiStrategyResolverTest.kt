package com.nim.game.application

import com.nim.game.domain.model.Difficulty
import com.nim.game.domain.model.Game
import com.nim.game.domain.model.HeapState
import com.nim.game.domain.model.Player
import com.nim.game.domain.strategy.OptimalStrategy
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.Random
import java.util.random.RandomGenerator

class AiStrategyResolverTest {
    private val game =
        Game(
            heapState = HeapState.single(10),
            currentTurn = Player.COMPUTER,
        )

    @Test
    fun `NIGHTMARE always plays the optimal move`() {
        val strategy = AiStrategyResolver().resolve(Difficulty.NIGHTMARE)
        val optimalMove = OptimalStrategy().determineMove(game)

        repeat(20) {
            assertEquals(optimalMove, strategy.determineMove(game))
        }
    }

    @Test
    fun `I_AM_TOO_YOUNG_TO_DIE plays legal moves that vary between calls`() {
        val strategy = AiStrategyResolver(random = Random(0)).resolve(Difficulty.I_AM_TOO_YOUNG_TO_DIE)

        val moves = (1..20).map { strategy.determineMove(game) }

        assertTrue(moves.toSet().size > 1)
        moves.forEach { move ->
            assertEquals(0, move.heapIndex)
            assertTrue(move.matches in 1..game.rules.maxTake)
        }
    }

    @Test
    fun `HURT_ME_PLENTY blends optimal and random play`() {
        val strategy = AiStrategyResolver(random = Random(0)).resolve(Difficulty.HURT_ME_PLENTY)
        val optimalMove = OptimalStrategy().determineMove(game)

        val moves = (1..100).map { strategy.determineMove(game) }
        val optimalCount = moves.count { it == optimalMove }

        // Neither a pure-optimal resolver (~100) nor a pure-random one (~17 coincidences)
        // can land within this window.
        assertTrue(optimalCount in 35..90)
    }

    @Test
    fun `polar difficulties consume no coin flips, blended difficulties one per move`() {
        val counting = CountingRandomGenerator(Random(0))
        val resolver = AiStrategyResolver(random = counting)

        repeat(10) {
            resolver.resolve(Difficulty.NIGHTMARE).determineMove(game)
            resolver.resolve(Difficulty.I_AM_TOO_YOUNG_TO_DIE).determineMove(game)
        }
        assertEquals(0, counting.coinFlips)

        repeat(10) {
            resolver.resolve(Difficulty.HURT_ME_PLENTY).determineMove(game)
        }
        assertEquals(10, counting.coinFlips)
    }

    /**
     * RandomGenerator wrapper counting coin flips ([RandomGenerator.nextDouble]).
     */
    private class CountingRandomGenerator(
        private val delegate: RandomGenerator,
    ) : RandomGenerator by delegate {
        var coinFlips: Int = 0
            private set

        override fun nextDouble(): Double {
            coinFlips++
            return delegate.nextDouble()
        }
    }
}
