package com.nim.game.domain.strategy

import com.nim.game.domain.model.Game
import com.nim.game.domain.model.Move
import java.util.random.RandomGenerator

/**
 * Stochastic strategy selecting a uniform random legal move across available heaps.
 */
class RandomStrategy(
    private val random: RandomGenerator = RandomGenerator.getDefault(),
) : AiStrategy {
    override fun determineMove(game: Game): Move {
        check(!game.isOver()) { "Cannot determine move for a finished game." }

        val nonZeroIndices = game.heapState.remainingHeapIndices()
        check(nonZeroIndices.isNotEmpty()) { "Cannot determine move when no matches remain." }

        val targetHeapIndex = nonZeroIndices[random.nextInt(nonZeroIndices.size)]
        val currentHeapSize = game.heapState.heaps[targetHeapIndex]

        val minAllowed = game.rules.minTake
        val maxAllowed = minOf(game.rules.maxTake, currentHeapSize)

        val matchesToTake =
            if (minAllowed == maxAllowed) {
                minAllowed
            } else {
                random.nextInt(minAllowed, maxAllowed + 1)
            }

        return Move(
            player = game.currentTurn,
            heapIndex = targetHeapIndex,
            matches = matchesToTake,
        )
    }
}
