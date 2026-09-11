package com.nim.game.domain.model

/**
 * Represents the state of all match heaps.
 */
data class HeapState(
    val heaps: List<Int>,
) {
    init {
        require(heaps.isNotEmpty()) { "HeapState must contain at least one heap." }
        require(heaps.all { it >= 0 }) { "Heap sizes must be non-negative: $heaps" }
    }

    fun totalMatches(): Int = heaps.sum()

    fun isEmpty(): Boolean = totalMatches() == 0

    fun remainingHeapIndices(): List<Int> = heaps.indices.filter { heaps[it] > 0 }

    /**
     * Reduces the target heap by the given match count.
     */
    fun take(
        heapIndex: Int,
        matches: Int,
    ): HeapState {
        require(heapIndex in heaps.indices) { "Heap index $heapIndex out of bounds." }
        require(matches in 1..heaps[heapIndex]) { "Cannot take $matches matches from heap with size ${heaps[heapIndex]}." }

        val updated =
            heaps.toMutableList().also {
                it[heapIndex] = it[heapIndex] - matches
            }
        return HeapState(updated)
    }

    companion object {
        fun single(matches: Int): HeapState = HeapState(listOf(matches))
    }
}
