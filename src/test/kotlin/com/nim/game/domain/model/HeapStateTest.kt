package com.nim.game.domain.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class HeapStateTest {
    @Test
    fun `should correctly initialize single heap`() {
        val state = HeapState.single(13)

        assertEquals(1, state.heaps.size)
        assertEquals(13, state.totalMatches())
        assertFalse(state.isEmpty())
        assertEquals(listOf(0), state.remainingHeapIndices())
    }

    @Test
    fun `should fail on empty heap list`() {
        assertThrows<IllegalArgumentException> {
            HeapState(emptyList())
        }
    }

    @Test
    fun `should fail when any heap is negative`() {
        assertThrows<IllegalArgumentException> {
            HeapState(listOf(5, -1, 3))
        }
    }

    @Test
    fun `should produce new state without mutating original on take`() {
        val original = HeapState(listOf(5, 3))

        val updated = original.take(heapIndex = 0, matches = 2)

        assertEquals(listOf(5, 3), original.heaps)
        assertEquals(listOf(3, 3), updated.heaps)
        assertEquals(8, original.totalMatches())
        assertEquals(6, updated.totalMatches())
    }

    @Test
    fun `should fail take when heap index is out of bounds`() {
        val state = HeapState.single(5)

        assertThrows<IllegalArgumentException> {
            state.take(heapIndex = 1, matches = 1)
        }
    }

    @Test
    fun `should fail take when matches exceed current heap size`() {
        val state = HeapState.single(3)

        assertThrows<IllegalArgumentException> {
            state.take(heapIndex = 0, matches = 4)
        }
    }

    @Test
    fun `should report empty when all heaps reach zero`() {
        val state = HeapState(listOf(0, 0))

        assertTrue(state.isEmpty())
        assertEquals(0, state.totalMatches())
        assertTrue(state.remainingHeapIndices().isEmpty())
    }
}
