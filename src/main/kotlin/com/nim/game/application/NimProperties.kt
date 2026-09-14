package com.nim.game.application

import com.nim.game.domain.model.Difficulty
import com.nim.game.domain.model.GameMode
import com.nim.game.domain.model.NimDefaults
import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Global game configuration properties.
 */
@ConfigurationProperties(prefix = "nim.default")
data class NimProperties(
    val randomHeapMin: Int = NimDefaults.RANDOM_HEAP_MIN,
    val randomHeapMax: Int = NimDefaults.RANDOM_HEAP_MAX,
    val maxTake: Int = NimDefaults.DEFAULT_MAX_TAKE,
    val mode: GameMode = NimDefaults.DEFAULT_MODE,
    val difficulty: Difficulty = NimDefaults.DEFAULT_DIFFICULTY,
)
