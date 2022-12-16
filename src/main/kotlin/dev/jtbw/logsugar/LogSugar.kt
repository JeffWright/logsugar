package dev.jtbw.logsugar

import dev.jtbw.logsugar.LogSugarTiming.timeFmt

private const val NEXT_LINE = "↘"
private val CANDY = "\uD83C\uDF6C" // 🍬
private val DIVIDER = " $CANDY "
private const val COMBINING_UNDERSCORE = "̲"

object LogSugar {

  private data class Config(
    val maxLineWidth: Int = 4000,
    val maxLeftSectionWidth: Int = 75,
    val leftSectionWidth: RunningAggregate = RunningAggregate.MatchLongest(75),
    val padding: Char = ' ',
    val useColors: Boolean = false,
    val replaceAts: Boolean = false,
    val logUncaughtExceptions: Boolean = true,
    val writer: Writer = { tag, message -> println("$tag: $message") }
  )

  private var config: Config = Config()
  internal val useColors
    get() = config.useColors
  private var haveSetExceptionHandler = false

  fun configure(
    /** Logs longer than this will be wrapped */
    maxLineWidth: Int = config.maxLineWidth,
    /**
     * Logs whose left section is above this value won't affect the running aggregate. (they will
     * NOT be truncated)
     */
    maxLeftSectionWidth: Int = config.maxLeftSectionWidth,
    /**
     * Controls how to pad the left section to try to keep messages aligned. Options are:
     * ```
     *    MatchLongest: match the longest one seen so far, up to a max
     *    Constant: constant width
     *    P9X: Track the p95 (configurable) of left section widths
     * ```
     * If you want no padding/alignment, use
     * ```
     * RunningAggregate.Constant(0)
     * ```
     */
    leftSectionWidth: RunningAggregate = config.leftSectionWidth,
    /** Character with which the left section is padded */
    padding: Char = config.padding,
    useColors: Boolean = config.useColors,
    /**
     * having the word 'at' in your log messages will break the regex IntelliJ/Android Studio uses
     * to pick out clickable file names. If this is set to true, LogSugar will replace at with @
     */
    replaceAts: Boolean = config.replaceAts,
    logUncaughtExceptions: Boolean = config.logUncaughtExceptions,
    writer: Writer = config.writer,
  ) {
    config =
      config.copy(
        maxLineWidth = maxLineWidth,
        maxLeftSectionWidth = maxLeftSectionWidth,
        leftSectionWidth = leftSectionWidth,
        padding = padding,
        useColors = useColors,
        replaceAts = replaceAts,
        logUncaughtExceptions = logUncaughtExceptions,
        writer = writer,
      )

    if (logUncaughtExceptions && !haveSetExceptionHandler) {
      haveSetExceptionHandler = true
      val wrappedHandler: Thread.UncaughtExceptionHandler? =
        Thread.getDefaultUncaughtExceptionHandler()
      Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
        logWtf("FATAL EXCEPTION")
        log(throwable)
        wrappedHandler?.uncaughtException(thread, throwable) ?: throw throwable
      }
    }

    // Record starting time:
    LogSugarTiming.startTime
    if (!haveLoggedStart) {
      haveLoggedStart = true
      logDivider("Begin Session", 5)
    }
  }

  private var haveLoggedStart = false

  private fun write(
    tag: String,
    infoLeft: String,
    padding: Int,
    infoRight: String,
    details: String
  ) {
    val paddingString = config.padding.toString().repeat(padding)
    val message =
      "$infoLeft$paddingString$infoRight$DIVIDER$details".let {
        if (config.replaceAts) {
          it.replace(Regex("\\bat\\b"), "a̲t̲")
          // it.replace(Regex(" at "), " a̲t̲ ")
        } else {
          it
        }
      }

    config.writer(tag, message)
  }

  internal fun log(tag: String?, details: String?, breadcrumb: Throwable? = null) {
    logInternal(tag ?: getFunctionBreadcrumb(), details ?: "TODO", breadcrumb)
  }

  internal fun log(
    tag: String? = null,
    detailsSequence: Sequence<String>,
    breadcrumb: Throwable? = null,
  ) {
    val seq = detailsSequence
    rawLogInternal(tag ?: getFunctionBreadcrumb(), breadcrumb) { chunkSize ->
      seq.flatMap { it.windowedSequence(size = chunkSize, step = chunkSize, partialWindows = true) }
    }
  }

  private fun logInternal(tag: String, details: String, breadcrumb: Throwable?) {
    rawLogInternal(tag, breadcrumb) { chunkSize ->
      if (details.isBlank()) {
        return@rawLogInternal sequenceOf("")
      }

      details
        .split("\n") // TODO JTW: using .split drops multiple sequential newlines
        .asSequence()
        .flatMap { it.windowedSequence(size = chunkSize, step = chunkSize, partialWindows = true) }
    }
  }

  private fun rawLogInternal(
    header: String,
    breadcrumb: Throwable?,
    lines: (chunkSize: Int) -> Sequence<String>,
  ) {

    val tag = getClassBreadcrumb(breadcrumb)
    val infoLeft = timeFmt.colorized(ANSI_YELLOW)
    val infoRight = header.colorized(ANSI_BRIGHT_GREEN)

    val infoLeftWidth = infoLeft.width()
    val infoRightWidth = infoRight.width()

    val leftMinSize = infoLeft.width() + 1 + infoRight.width()
    val leftActualSize = updateSectionWidth(tagWidth = tag.length, leftWidth = leftMinSize)
    require(leftActualSize >= leftMinSize)
    // println("leftActualSize = $leftActualSize")
    // println("iL = ${infoLeft.width()} : >$infoLeft<")
    // println("iR = ${infoRight.width()} : >$infoRight<")

    val widthAvailableForDetails = config.maxLineWidth - leftActualSize - DIVIDER.length

    var isFirst = true
    lines(widthAvailableForDetails).forEach { line ->
      if (isFirst) {
        isFirst = false
        val padding = leftActualSize - infoLeftWidth - infoRightWidth
        write(tag, infoLeft, padding, infoRight, line)
      } else {
        val padding = leftActualSize
        val blankTag = " ".repeat(tag.length - 1) + NEXT_LINE // Android will strip trailing spaces
        write(blankTag, "", padding, "", line)
      }
    }
  }

  /** update sectionWidth, and return the width to use */
  private fun updateSectionWidth(tagWidth: Int, leftWidth: Int): Int {
    val width = tagWidth + leftWidth
    var sectionWidth: Int = width
    if (width <= config.maxLeftSectionWidth) {
      sectionWidth = config.leftSectionWidth.add(width)
    }
    return maxOf(sectionWidth, width) - tagWidth
  }

  private fun isPreferred(element: StackTraceElement): Boolean {
    val prefixesToIgnore = listOf("kotlin", "io.reactivex", "java.lang", "dev.jtbw.log")

    return !prefixesToIgnore.any { prefix -> element.className.startsWith(prefix) }
  }

  private fun isAcceptable(element: StackTraceElement): Boolean {
    val prefixesToIgnore = listOf("dev.jtbw.log")

    return !prefixesToIgnore.any { prefix -> element.className.startsWith(prefix) }
  }

  private fun getClassBreadcrumb(breadcrumb: Throwable?): String {
    val element =
      (breadcrumb ?: Throwable()).stackTrace.let { elements ->
        // First acceptable, or first if none are acceptable
        elements.firstOrNull { isPreferred(it) }
          ?: elements.firstOrNull { isAcceptable(it) } ?: elements.first()
      }

    // Minimum set of syntax that will cause IntelliJ to recognize this as clickable
    return ".(${element.fileName}:${element.lineNumber})"
  }

  private fun getFunctionBreadcrumb(): String {
    val element =
      Throwable().stackTrace.let {
        it.firstOrNull { isPreferred(it) && it.methodName != "invoke" }
          ?: it.firstOrNull { isPreferred(it) } ?: it.firstOrNull { isAcceptable(it) } ?: it.first()
      }

    return if (element.methodName == "invoke") {
      element.className
    } else {
      "${element.methodName}()"
    }
  }
}

private typealias Writer = (tag: String, message: String) -> Unit

/** Print a debug message */
fun log(details: String? = "") = LogSugar.log(null, details)

/** Print a debug message (pass null for [tag] to get autogenerated breadcrumb) */
fun log(tag: String?, details: String?, breadcrumb: Throwable? = null) =
  LogSugar.log(tag, details, breadcrumb)

/** Print a multi-line debug message */
fun log(tag: String?, detailsSequence: Sequence<String>) = LogSugar.log(tag, detailsSequence)

/** Print a debug message indicating something is wrong. It will be very visible */
fun logWtf(details: String? = null) = logWtf(null, details)

/** Print a debug message indicating something is wrong. It will be very visible */
fun logWtf(tag: String?, details: String?, breadcrumb: Throwable? = null) =
  if (LogSugar.useColors) {
    log(tag, (details ?: "        WTF        ").colorizedWtf(), breadcrumb)
  } else {
    val msg = (details ?: "WTF")
    log(tag, "")
    log(tag, "!!!!!!!!!!!!!!!! $msg !!!!!!!!!!!!!!!!", breadcrumb)
    log(tag, "")
  }

private fun String.colorizedWtf(): String = colorized(ANSI_RED_BG, ANSI_BRIGHT_BLACK)

/** Print a stack trace */
fun log(err: Throwable) = log(null, err)

/** Print a stack trace */
fun log(tag: String? = null, err: Throwable) {
  log(
    tag,
    err.stackTraceToString().let { stackTrace ->
      val firstLine = stackTrace.substringBefore("\n").colorized(ANSI_BRIGHT_RED)
      firstLine + "\n" + stackTrace.substringAfter("\n")
    }
  )
}

/**
 * Print the value of an object, returning the object e.g.:
 * someObject.getThing().inspect("thing").title
 */
fun <T> T.inspect(tag: String? = null, toString: ((T) -> Any?) = { it.toString() }): T {
  LogSugar.log(tag = tag, details = toString(this).toString())
  return this
}

fun Throwable.inspect(tag: String? = null) = log(tag, this)

fun logStackTrace(message: String? = null) = log(null, TracerException(message))

/** Print a divider [weight] in 0-5 */
fun logDivider(message: String? = null, weight: Int = 3) {
  val formattedMessage =
    message?.let {
      when (weight) {
        0 -> ". $it ."
        1 -> "◁ $it ▷     "
        2 -> "◀ $it ▶"
        3 -> "◀ $it ▶".colorized(ANSI_YELLOW)
        4 -> "  $it  ".colorized(ANSI_BRIGHT_YELLOW)
        5 -> "  $it  ".colorized(ANSI_BRIGHT_YELLOW_BG, ANSI_BRIGHT_BLACK)
        else -> error("Please report this to the maintainer of LogSugar")
      }
    }
      ?: ""
  var ruleColor: String? = null
  val rule =
    when (weight.coerceIn(0, 5)) {
      0 -> "      "
      1 -> ".     "
      2 -> "- - - "
      3 -> "——————".also { ruleColor = ANSI_YELLOW }
      4 ->
        if (LogSugar.useColors) {
          "      ".also { ruleColor = ANSI_WHITE_BG }
        } else {
          "▄▄▄▄▄▄"
        }
      5 ->
        if (LogSugar.useColors) {
          "      ".also { ruleColor = ANSI_BRIGHT_YELLOW_BG }
        } else {
          "██████"
        }
      else -> error("Please report this to the maintainer of LogSugar")
    }

  val pre = rule.repeat(3).colorized(ruleColor)
  val text = formattedMessage
  val post = rule.repeat(10).colorized(ruleColor)
  log("$pre$text$post")
}

class TracerException(message: String? = null) : Throwable(message)
