package com.nim.game.application

import com.nim.game.domain.model.Difficulty
import com.nim.game.domain.strategy.AiStrategy
import com.nim.game.domain.strategy.OptimalStrategy
import com.nim.game.domain.strategy.RandomStrategy
import org.springframework.stereotype.Component
import java.util.random.RandomGenerator

@Component
class AiStrategyResolver(
    private val random: RandomGenerator = RandomGenerator.getDefault(),
) {
    private val randomStrategy = RandomStrategy(random)
    private val optimalStrategy = OptimalStrategy()

    /**
     * Resolves the AI strategy for the given difficulty.
     *
     * Every difficulty is a blend of optimal and random play, parameterized by
     * [Difficulty.optimalProbability]. At the poles (0.0 / 1.0) the corresponding
     * pure strategy is returned directly, so no randomness is consumed for
     * deterministic difficulties.
     */
    fun resolve(difficulty: Difficulty): AiStrategy {
        val probability = difficulty.optimalProbability

        return when {
            probability <= 0.0 -> {
                randomStrategy
            }

            probability >= 1.0 -> {
                optimalStrategy
            }

            else -> {
                AiStrategy { game ->
                    if (random.nextDouble() < probability) {
                        optimalStrategy.determineMove(game)
                    } else {
                        randomStrategy.determineMove(game)
                    }
                }
            }
        }
    }
}
