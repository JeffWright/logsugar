package dev.jtbw.logsugar

private val counts: MutableMap<Any, Long> = mutableMapOf()

fun countOccurrence(key: Any): Long {
  val c = counts.getOrDefault(key, 0) + 1
  counts[key] = c
  return c
}

fun getNumOccurrences(key: Any): Long {
  return counts.getOrDefault(key, 0)
}

fun logOccurrence(key: Any, details: String? = null): Long {
  val c = countOccurrence(key)
  log(
    buildString {
      append("#️⃣ N = $c".maybeColorized(ANSI_BRIGHT_RED))
      append(" for ")
      append(key.toString().maybeColorized(ANSI_BRIGHT_BLUE))
      details?.let {
        append(": ")
        append(it)
      }
    }
  )
  return c
}

fun resetNumOccurrences(key: Any) {
  counts.remove(key)
}
