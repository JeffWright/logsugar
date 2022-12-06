package dev.jtbw.log

fun <T, I : Collection<T>> I.inspectEach(
  msg: String? = null,
  toString: ((T) -> Any?) = { it.toString() }
): I {
  val size = this.size
  val clazz = this::class.simpleName
  log(
    msg,
    sequence {
      yield("($clazz, size=$size)")
      forEachIndexed { idx, item -> yield("  [${idx}] -> ${toString(item)}") }
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
      yield("($clazz, size=$size)")
      entries.forEach { yield("  [${it.key}] -> ${toString(it.value)}") }
    }
  )
  return this
}
