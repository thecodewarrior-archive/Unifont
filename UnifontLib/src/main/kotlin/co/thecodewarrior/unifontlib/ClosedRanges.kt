package co.thecodewarrior.unifontlib

import co.thecodewarrior.unifontlib.utils.max
import co.thecodewarrior.unifontlib.utils.min
import java.util.*

class IntRanges: ClosedRanges<Int>(
        increment = { it + 1 },
        decrement = { it - 1 }
)

class LongRanges: ClosedRanges<Long>(
        increment = { it + 1 },
        decrement = { it - 1 }
)

open class ClosedRanges<T: Comparable<T>>(
        val increment: (value: T) -> T,
        val decrement: (value: T) -> T
) {
    private val rangeSet = TreeSet<ClosedRange<T>>(Comparator {
        a, b -> a.start.compareTo(b.start)
    }) // ordered by start of range

    val ranges: Collection<ClosedRange<T>>
        get() = rangeSet

    fun add(range: ClosedRange<T>) {
        val overlapping = overlappingRanges(range)
        rangeSet.removeAll(overlapping)
        var start = range.start
        var end = range.endInclusive
        if(overlapping.isNotEmpty()) {
            start = min(start, overlapping.first().start)
            end = max(end, overlapping.last().endInclusive)
        }
        rangeSet.add(start..end)
    }

    fun addAll(ranges: Iterable<ClosedRange<T>>) {
        ranges.forEach {
            add(it)
        }
    }

    fun remove(range: ClosedRange<T>) {
        val overlapping = overlappingRanges(range)
        rangeSet.removeAll(overlapping)
        if(overlapping.isNotEmpty()) {
            val start = overlapping.first().start
            val end = overlapping.last().endInclusive
            if(start < range.start) {
                rangeSet.add(start..decrement(range.start))
            }
            if(end > range.endInclusive) {
                rangeSet.add(increment(range.endInclusive)..start)
            }
        }
    }

    fun removeAll(ranges: Iterable<ClosedRange<T>>) {
        ranges.forEach {
            remove(it)
        }
    }

    fun contains(range: ClosedRange<T>): Boolean {
        val previous = rangeSet.floor(range) ?: return false
        return previous.endInclusive >= range.endInclusive
    }

    fun overlaps(range: ClosedRange<T>): Boolean {
        return overlappingRanges(range).isNotEmpty()
    }

    fun clamping(range: ClosedRange<T>): List<ClosedRange<T>> {
        val overlapping = overlappingRanges(range).toMutableList()
        if(overlapping.isNotEmpty()) {
            overlapping[0] = max(range.start, overlapping[0].start) .. overlapping[0].endInclusive
            overlapping[overlapping.lastIndex] = overlapping[overlapping.lastIndex].start ..
                    min(range.endInclusive, overlapping[overlapping.lastIndex].endInclusive)
        }
        return overlapping
    }

    /**
     * Returns all ranges that overlap the passed range. The ranges are in increasing order of start value
     */
    fun overlappingRanges(range: ClosedRange<T>): List<ClosedRange<T>> {
        val candidates = rangeSet.headSet(range.endInclusive..range.endInclusive, true) // cut off values with starts that can't overlap
        val toMerge = mutableListOf<ClosedRange<T>>()

        for(value in candidates.descendingIterator()) { // work backward until the max doesn't overlap
            if(value.endInclusive >= range.start) {
                toMerge.add(value)
            } else {
                break
            }
        }

        return toMerge.reversed()
    }
}