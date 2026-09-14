package com.nim.game.domain.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class NimDefaultsTest {
    @Test
    fun `defaults satisfy domain invariants`() {
        assertTrue(NimDefaults.DEFAULT_MAX_TAKE > GameRules.MIN_TAKE)
        assertEquals(GameMode.MISERE, NimDefaults.DEFAULT_MODE)
        assertEquals(Difficulty.I_AM_TOO_YOUNG_TO_DIE, NimDefaults.DEFAULT_DIFFICULTY)
        assertTrue(NimDefaults.RANDOM_HEAP_MIN > 0)
        assertTrue(NimDefaults.RANDOM_HEAP_MAX >= NimDefaults.RANDOM_HEAP_MIN)
    }

    @Test
    fun `GameRules default constructor aligns with NimDefaults`() {
        val rules = GameRules()

        assertEquals(GameRules.MIN_TAKE, GameRules.MIN_TAKE)
        assertEquals(NimDefaults.DEFAULT_MAX_TAKE, rules.maxTake)
        assertEquals(NimDefaults.DEFAULT_MODE, rules.mode)
    }
}
