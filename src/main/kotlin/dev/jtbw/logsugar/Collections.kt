package dev.jtbw.logsugar

fun <T, I : Collection<T>> I.inspectEach(tag: String? = null, toString: ((T) -> Any?)? = null): I {
  val collection = this
  logMultiple(
    tag,
    buildList<String> {
      add(summarize(collection))
      if (toString != null) {
        collection.forEachIndexed { idx, value ->
          add(("  [$idx] -> ").colorized(ANSI_BLUE) + toString(value).toString())
        }
      } else {
        addAll(collection.describe(indentLevel = 1))
      }
    }
  )
  return this
}

fun <KEY, VALUE, MAP : Map<KEY, VALUE>> MAP.inspectEach(
  tag: String? = null,
  toString: ((VALUE) -> Any?)?
): MAP {
  if (toString == null) {
    return inspectEach(tag)
  }

  val map = this
  logMultiple(
    tag,
    buildList {
      add(summarize(map))
      map.forEach { (key, value) ->
        add(("  $key -> ").colorized(ANSI_BLUE) + toString(value).toString())
      }
    }
  )
  return this
}

internal fun <MAP : Map<*, *>> MAP.inspectEach(
  tag: String? = null,
): MAP {
  val map = this
  logMultiple(
    tag,
    buildList {
      add(summarize(map))
      addAll(map.describe(indentLevel = 1))
    }
  )
  return this
}

private fun summarize(obj: Any?): String {
  return when (obj) {
    is Collection<*> -> {
      val size = obj.size
      val clazz = obj::class.simpleName
      "($clazz, size = $size)".colorized(ANSI_BLUE)
    }
    is Map<*, *> -> {
      val size = obj.size
      val clazz = obj::class.simpleName
      "($clazz, size = $size)".colorized(ANSI_BLUE)
    }
    else -> error("cannot summarize $obj")
  }
}

private fun Collection<*>.describe(indentLevel: Int): List<String> {
  return this.mapIndexed { idx, v ->
      val label = "[$idx]"
      describeLines(label, v, indentLevel)
    }
    .flatten()
}

private fun Map<*, *>.describe(indentLevel: Int): List<String> {
  return entries
    .map {
      val v = it.value
      val label = it.key.toString()
      describeLines(label, v, indentLevel)
    }
    .flatten()
}

private fun describeLines(label: String, v: Any?, indentLevel: Int): List<String> {
  return when (v) {
    is Collection<*> -> {
      listOf(indent(indentLevel) + ("$label -> ").colorized(ANSI_BLUE) + summarize(v)) +
        v.describe(indentLevel + 1)
    }
    is Map<*, *> -> {
      listOf(indent(indentLevel) + ("$label -> ").colorized(ANSI_BLUE) + summarize(v)) +
        v.describe(indentLevel + 1)
    }
    else -> listOf(indent(indentLevel) + ("$label -> ").colorized(ANSI_BLUE) + (v))
  }
}

private fun indent(level: Int): String {
  return ("  |".repeat(level) + "-- ").colorized(ANSI_WHITE)
}
