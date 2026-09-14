package com.nim.game.domain.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DifficultyTest {
    @Test
    fun `optimalProbability is within valid bounds for all difficulties`() {
        Difficulty.entries.forEach { difficulty ->
            assertTrue(difficulty.optimalProbability in 0.0..1.0) {
                "optimalProbability for $difficulty must be within [0.0, 1.0]"
            }
        }
    }

    @Test
    fun `difficulty ordering matches increasing optimal probability`() {
        val byStrength = Difficulty.entries.sortedBy { it.optimalProbability }

        assertEquals(Difficulty.entries, byStrength)
    }

    @Test
    fun `extreme difficulties map to pure strategies`() {
        assertEquals(0.0, Difficulty.I_AM_TOO_YOUNG_TO_DIE.optimalProbability)
        assertEquals(1.0, Difficulty.NIGHTMARE.optimalProbability)
    }
}
