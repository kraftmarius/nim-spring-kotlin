package com.nim.game.domain.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class GameRulesTest {
    @Test
    fun `should construct valid rules with defaults`() {
        val rules = GameRules()

        assertEquals(3, rules.maxTake)
        assertEquals(GameMode.MISERE, rules.mode)
    }

    @Test
    fun `should expose a fixed minimum take of one`() {
        assertEquals(1, GameRules.MIN_TAKE)
    }

    @Test
    fun `should fail when maxTake equals the minimum take`() {
        val exception =
            assertThrows<IllegalArgumentException> {
                GameRules(maxTake = 1)
            }

        assertEquals("maxTake (1) must be greater than 1", exception.message)
    }

    @Test
    fun `should fail when maxTake is less than the minimum take`() {
        val exception =
            assertThrows<IllegalArgumentException> {
                GameRules(maxTake = 0)
            }

        assertEquals("maxTake (0) must be greater than 1", exception.message)
    }
}
