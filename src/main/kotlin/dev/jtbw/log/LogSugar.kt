package dev.jtbw.log

object LogSugar {
  private const val NEXT_LINE = "↘"
  private const val DIVIDER = " \uD83C\uDF6C " // 🍬

  private data class Config(
    val maxLineWidth: Int = 4000,
    /**
     * Logs whose left section is above this value won't affect the running aggregate. (they will
     * NOT be truncated) TODO JTW better name
     */
    val maxLeftSectionWidth: Int = 75,
    /**
     * Controls how to pad the left section to try to keep messages aligned. Options are:
     * ```
     *    MatchLongest: match the longest one seen so far, up to a max
     *    Constant: constant width
     *    P9X: Track the p95 (configurable) of left section widths
     * ```
     */
    val leftSectionWidth: RunningAggregate = RunningAggregate.MatchLongest(75),
    val padding: Char = ' ',
    val getTag: (Throwable) -> String = ::getClassBreadcrumb,
    val writer: Writer = { tag, message -> println("$tag: $message") }
  )

  private var config: Config = Config()

  fun configure(
    maxLineWidth: Int = config.maxLineWidth,
    /**
     * Logs whose left section is above this value won't affect the running aggregate. (they will
     * NOT be truncated) TODO JTW better name
     */
    maxLeftSectionWidth: Int = config.maxLeftSectionWidth,
    /**
     * Controls how to pad the left section to try to keep messages aligned. Options are:
     * ```
     *    MatchLongest: match the longest one seen so far, up to a max
     *    Constant: constant width
     *    P9X: Track the p95 (configurable) of left section widths
     * ```
     */
    leftSectionWidth: RunningAggregate = config.leftSectionWidth,
    padding: Char = config.padding,
    getTag: (Throwable) -> String = config.getTag,
    writer: Writer = config.writer,
  ) {
    config =
      config.copy(
        maxLineWidth = maxLineWidth,
        maxLeftSectionWidth = maxLeftSectionWidth,
        leftSectionWidth = leftSectionWidth,
        padding = padding,
        getTag = getTag,
        writer = writer,
      )
  }

  private fun write(
    tag: String,
    infoLeft: String,
    padding: Int,
    infoRight: String,
    details: String
  ) {
    val paddingString = config.padding.toString().repeat(padding)
    val message = "$infoLeft$paddingString$infoRight$DIVIDER$details"
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
    val infoLeft = "${time}s"
    val infoRight = header

    val leftMinSize = infoLeft.length + 1 + infoRight.length
    val leftActualSize = updateSectionWidth(tag.length, leftMinSize)
    require(leftActualSize >= leftMinSize)

    val widthAvailableForDetails = config.maxLineWidth - leftActualSize - DIVIDER.length

    var isFirst = true
    lines(widthAvailableForDetails).forEach { line ->
      if (isFirst) {
        isFirst = false
        val padding = leftActualSize - infoLeft.length - infoRight.length
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

  private fun isAcceptable(element: StackTraceElement): Boolean {
    if (element.className.startsWith("kotlin")) {
      return false
    }
    return "dev.jtbw.log" !in element.className
  }

  private fun getClassBreadcrumb(breadcrumb: Throwable?): String {
    return (breadcrumb ?: Throwable())
      .stackTrace
      .firstOrNull { isAcceptable(it) }
      ?.let { element ->
        // Minimum set of syntax that will cause IntelliJ to recognize this as clickable
        ".(${element.fileName}:${element.lineNumber})"
      }
      ?: "<tag>"
  }

  private fun getFunctionBreadcrumb(): String {
    return Throwable()
      .stackTrace
      .let {
        it.firstOrNull { isAcceptable(it) && it.methodName != "invoke" }
          ?: it.firstOrNull { isAcceptable(it) }
      }
      ?.let { element ->
        if (element.methodName == "invoke") {
          element.className
        } else {
          "${element.methodName}()"
        }
      }
      ?: "<tag>"
  }
}

private typealias Writer = (tag: String, message: String) -> Unit

/** Print a debug message */
fun log(details: String? = "") = LogSugar.log(null, details)

/** Print a debug message pass null for [tag] to get autogenerated breadcrumb */
fun log(tag: String?, details: String?, breadcrumb: Throwable? = null) =
  LogSugar.log(tag, details, breadcrumb)

/** Print a debug message */
fun log(tag: String?, detailsSequence: Sequence<String>) = LogSugar.log(tag, detailsSequence)

/** Print a debug message */
fun log(err: Throwable) {
  log(err.stackTraceToString())
}

/**
 * Log the value of an object, returning the object e.g.:
 * someObject.getThing().logValue("thing").title
 */
fun <T> T.inspect(tag: String? = null, toString: ((T) -> Any?) = { it.toString() }): T {
  LogSugar.log(tag = tag, details = toString(this).toString())
  return this
}

fun logStackTrace(message: String?) = log(TracerException(message))

fun logStackTrace() = log(TracerException())

fun logDivider() = log("------------------------------------------------")

class TracerException(message: String? = null) : Throwable(message)

// TODO JTW:
// timing (delta)
// counts
