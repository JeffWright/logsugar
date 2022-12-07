package dev.jtbw.logsugar

import dev.jtbw.logsugar.inspect as ktinspect
import dev.jtbw.logsugar.log as ktlog

/*
 * These are just aliases to make calling from Java a little nicer
 */
object LogSugarJava {
  fun <T> T.inspect(
    tag: String,
  ): T {
    return this.ktinspect(tag = tag)
  }

  fun <T> T.inspect(): T {
    return this.ktinspect()
  }

  fun log(details: String?) {
    return ktlog(details = details)
  }

  fun log(tag: String, details: String?) {
    return ktlog(tag = tag, details = details)
  }
}
