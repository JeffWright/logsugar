package dev.jtbw.logsugar

fun <T, I : Collection<T>> I.inspectEach(
  msg: String? = null,
  toString: ((T) -> Any?) = { it.toString() }
): I {
  val size = this.size
  val clazz = this::class.simpleName
  log(
    msg,
    sequence {
      yield("($clazz, size = $size)".colorized(ANSI_BLUE))
      forEachIndexed { idx, item -> yield("  [${idx}] -> ".colorized(ANSI_BLUE) + toString(item)) }
    }
  )
  return this
}

fun <K, V, M : Map<K, V>> M.inspectEach(
  msg: String? = null,
  toString: ((V) -> Any?) = { it.toString() }
): M {
  val size = this.size
  val clazz = this::class.simpleName
  log(
    msg,
    sequence {
      yield("($clazz, size = $size)".colorized(ANSI_BLUE))
      entries.forEach { yield("  [${it.key}] -> ".colorized(ANSI_BLUE) + toString(it.value)) }
    }
  )
  return this
}
