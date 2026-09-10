package com.nim.game.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class GameRulesTest {
    @Test
    fun `should construct valid rules with defaults`() {
        val rules = GameRules()

        assertEquals(1, rules.minTake)
        assertEquals(3, rules.maxTake)
        assertEquals(GameMode.MISERE, rules.mode)
    }

    @Test
    fun `should fail when minTake is less than 1`() {
        val exception =
            assertThrows<IllegalArgumentException> {
                GameRules(minTake = 0, maxTake = 3)
            }

        assertEquals("minTake must be at least 1, but was 0", exception.message)
    }

    @Test
    fun `should fail when maxTake equals minTake`() {
        val exception =
            assertThrows<IllegalArgumentException> {
                GameRules(minTake = 2, maxTake = 2)
            }

        assertEquals("maxTake (2) must be greater than minTake (2)", exception.message)
    }

    @Test
    fun `should fail when maxTake is strictly less than minTake`() {
        val exception =
            assertThrows<IllegalArgumentException> {
                GameRules(minTake = 4, maxTake = 2)
            }

        assertEquals("maxTake (2) must be greater than minTake (4)", exception.message)
    }
}
