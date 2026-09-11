package com.nim.game.application

import com.nim.game.domain.model.Difficulty
import com.nim.game.domain.model.GameMode
import com.nim.game.domain.model.Player
import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Default configurations loaded from environment or application properties.
 */
@ConfigurationProperties(prefix = "nim.default")
data class NimProperties(
    val initialMatches: Int = 13,
    val minTake: Int = 1,
    val maxTake: Int = 3,
    val mode: GameMode = GameMode.MISERE,
    val difficulty: Difficulty = Difficulty.I_AM_TOO_YOUNG_TO_DIE,
    val startingPlayer: Player = Player.HUMAN,
)
