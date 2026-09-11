package com.nim.game.domain.strategy

import com.nim.game.domain.model.Game
import com.nim.game.domain.model.Move
import java.util.random.RandomGenerator

/**
 * Composite strategy delegating between two strategies based on a probability threshold.
 * Represents heuristic/adaptive difficulty profiles (e.g., HURT_ME_PLENTY).
 */
class ProbabilisticStrategy(
    private val primary: AiStrategy,
    private val fallback: AiStrategy,
    private val primaryProbability: Double = 0.5,
    private val random: RandomGenerator = RandomGenerator.getDefault(),
) : AiStrategy {
    init {
        require(primaryProbability in 0.0..1.0) {
            "Probability must be within [0.0, 1.0], but was $primaryProbability"
        }
    }

    override fun determineMove(game: Game): Move {
        val selectedStrategy =
            if (random.nextDouble() < primaryProbability) {
                primary
            } else {
                fallback
            }

        return selectedStrategy.determineMove(game)
    }
}
