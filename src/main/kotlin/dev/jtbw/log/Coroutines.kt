package dev.jtbw.log

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach

/** Log everything that comes out of this Flow */
fun <T> Flow<T>.inspectEach(
  tag: String? = null,
  toString: ((T) -> String?) = { it.toString() }
): Flow<T> {
  var count = 0
  val breadcrumb = Throwable()
  return this.onEach {
    log(tag, "[$count] -> ${toString(it)}", breadcrumb)
    count++
  }
}
