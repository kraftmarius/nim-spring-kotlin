package com.nim.game.domain.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PlayerTest {
    @Test
    fun `next should alternate between human and computer`() {
        assertEquals(Player.COMPUTER, Player.HUMAN.next())
        assertEquals(Player.HUMAN, Player.COMPUTER.next())
    }
}
