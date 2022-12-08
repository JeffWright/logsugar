package dev.jtbw.logsugar

import dev.jtbw.logsugar.LogSugarTiming.currentTime
import dev.jtbw.logsugar.LogSugarTiming.formatElapsedMessage

object LogSugarTiming {
  val startTime: Long = System.currentTimeMillis()
  val currentTime: Long
    get() = System.currentTimeMillis() - startTime

  internal val timeFmt: String
    get() = formatTime(currentTime)

  fun formatElapsedMessage(start: Long, end: Long, forr: Any?): String {
    val elapsed = end - start
    return buildString {
      append("⌛ ")
      append(formatTime(elapsed).colorized(ANSI_BRIGHT_RED))
      forr?.let {
        append(" for ")
        append(it.toString().colorized(ANSI_BRIGHT_BLUE))
      }
      append(" (started @ ${formatTime(start)})".colorized(ANSI_GREEN))
    }
  }
}

fun formatTime(time: Long): String {
  return (time.toFloat() / 1000f).let { "%.3fs".format(it) }
}

private val timers: MutableMap<Any, Long> = mutableMapOf()

fun startTiming(key: Any, alsoLog: Boolean = false) {
  timers[key] = currentTime
  if (alsoLog) {
    log("⌛ starting timer for ".colorized(ANSI_BLUE) + key.toString().colorized(ANSI_BRIGHT_BLUE))
  }
}

fun logTiming(key: Any): Long {
  if (!timers.containsKey(key)) {
    log("logTiming(): no timer was started for $key")
    return -1
  }

  val end = currentTime
  val start = timers[key]!!

  val elapsed = end - start
  log(formatElapsedMessage(start, end, key))
  return elapsed
}

fun <T> runTiming(details: Any? = null, block: () -> T): T {
  return runTiming(null, details, block)
}

fun <T> runTiming(tag: String?, details: Any?, block: () -> T): T {
  val start = currentTime
  return block().also {
    val end = currentTime
    log(tag = tag, details = formatElapsedMessage(start, end, details))
  }
}

suspend fun <T> runTimingSuspend(details: Any? = null, block: suspend () -> T): T {
  return runTimingSuspend(null, details, block)
}

suspend fun <T> runTimingSuspend(tag: String?, details: Any?, block: suspend () -> T): T {
  val start = currentTime
  return block().also {
    val end = currentTime
    log(tag = tag, details = formatElapsedMessage(start, end, details))
  }
}
