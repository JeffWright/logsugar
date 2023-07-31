package dev.jtbw.logsugar

/*
 * These are just aliases to make calling from Java a little nicer, since Java doesn't support
 * optional arguments or extension functions
 */
fun <T> inspectJava(
  item: T,
  tag: String,
): T {
  return item.inspect(tag = tag)
}

fun <T> inspectJava(item: T): T {
  return item.inspect()
}
