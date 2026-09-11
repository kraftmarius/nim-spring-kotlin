package com.nim.game.application

import com.nim.game.domain.model.Difficulty
import com.nim.game.domain.strategy.AiStrategy
import com.nim.game.domain.strategy.OptimalStrategy
import com.nim.game.domain.strategy.ProbabilisticStrategy
import com.nim.game.domain.strategy.RandomStrategy
import org.springframework.stereotype.Component

@Component
class AiStrategyResolver {
    private val randomStrategy = RandomStrategy()
    private val optimalStrategy = OptimalStrategy()
    private val probabilisticStrategy =
        ProbabilisticStrategy(
            primary = optimalStrategy,
            fallback = randomStrategy,
            primaryProbability = 0.5,
        )

    fun resolve(difficulty: Difficulty): AiStrategy =
        when (difficulty) {
            Difficulty.I_AM_TOO_YOUNG_TO_DIE -> randomStrategy
            Difficulty.HURT_ME_PLENTY -> probabilisticStrategy
            Difficulty.NIGHTMARE -> optimalStrategy
        }
}
