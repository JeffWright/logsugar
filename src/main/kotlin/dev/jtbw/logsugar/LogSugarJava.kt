package dev.jtbw.logsugar

/*
 * These are just aliases to make calling from Java a little nicer, since Java doesn't support
 * optional arguments or extension functions
 */
object LogSugarJava {
  fun <T> inspect(
    item: T,
    tag: String,
  ): T {
    return item.inspect(tag = tag)
  }

  fun <T> inspect(item: T): T {
    return item.inspect()
  }
}
