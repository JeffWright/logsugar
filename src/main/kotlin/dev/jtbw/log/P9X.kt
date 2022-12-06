package dev.jtbw.log

import java.util.PriorityQueue

interface RunningAggregate {
  fun get(): Int
  fun add(value: Int): Int

  class MatchLongest(private val maximum: Int) : RunningAggregate {
    private var runningMax: Int = 0
    override fun get(): Int {
      return runningMax
    }

    override fun add(value: Int): Int {
      runningMax = maxOf(runningMax, value).coerceAtMost(maximum)
      return get()
    }
  }

  class Constant(private val value: Int) : RunningAggregate {
    override fun get(): Int = value

    override fun add(value: Int): Int = value
  }

  class P9X(val historySize: Int = 100, val p: Float = 0.95f) : RunningAggregate {
    private val lowHeap = PriorityQueue<Int>(Comparator.reverseOrder()) // peek gives max
    private val highHeap = PriorityQueue<Int>() // peek gives min
    private val order = ArrayDeque<Int>()

    /** add [value] to the dataset, return the new p95 */
    override fun get(): Int {
      return when {
        lowHeap.isNotEmpty() -> lowHeap.peek()
        else -> 0
      }
    }

    /** add [value] to the dataset, return the new p95 */
    override fun add(value: Int): Int {
      order.addLast(value)
      if (value <= (lowHeap.peekOrNull() ?: 0)) {
        lowHeap.add(value)
      } else {
        highHeap.add(value)
      }

      if (order.size > historySize) {
        val oldValue = order.removeFirst()
        if (!lowHeap.remove(oldValue)) {
          highHeap.remove(oldValue)
        }
      }

      // rebalance
      val lowSize = (order.size * p).toInt()
      val highSize = (order.size - lowSize)

      while (highHeap.size > highSize) {
        lowHeap.add(highHeap.remove())
      }
      while (lowHeap.size > lowSize) {
        highHeap.add(lowHeap.remove())
      }

      return if (lowHeap.isEmpty()) {
        highHeap.peek()
      } else {
        lowHeap.peek()
      }
    }

    internal fun dump() {
      println("o: " + order.toList())
      println("l: " + lowHeap.toList().sorted())
      println("h: " + highHeap.toList().sorted())
      println("p95 = ${get()}")
    }
  }
}

private fun <E> PriorityQueue<E>.peekOrNull(): E? {
  return if (isNotEmpty()) {
    peek()
  } else {
    null
  }
}
