package dev.jtbw.logsugar

internal fun String.maybeColorized(vararg color: String?): String {
  return if (!LogSugar.useColors) {
    this
  } else {
    colorized(*color)
  }
}

fun String.colorized(vararg color: String?): String {
  return if (color.isEmpty()) {
    this
  } else {
    "${color.filterNotNull().joinToString("")}$this${ANSI_RESET}"
  }
}

fun String.width(): Int {
  return this.uncolorize().length
}

fun String.uncolorize(): String {
  return replace(Regex("""\u001b\[.*?m"""), "")
}

const val ANSI_RESET = "\u001B[0m"

const val ANSI_BLACK = "\u001B[30m"
const val ANSI_BRIGHT_BLACK = "\u001B[30;1m"
const val ANSI_BLACK_BG = "\u001B[40m"
const val ANSI_BRIGHT_BLACK_BG = "\u001B[40;1m"

const val ANSI_WHITE = "\u001B[37m"
const val ANSI_BRIGHT_WHITE = "\u001B[37;1m"
const val ANSI_WHITE_BG = "\u001B[47m"
const val ANSI_BRIGHT_WHITE_BG = "\u001B[47;1m"

const val ANSI_CYAN = "\u001B[36m"
const val ANSI_BRIGHT_CYAN = "\u001B[36;1m"
const val ANSI_CYAN_BG = "\u001B[46m"
const val ANSI_BRIGHT_CYAN_BG = "\u001B[46;1m"

const val ANSI_YELLOW = "\u001B[33m"
const val ANSI_BRIGHT_YELLOW = "\u001B[33;1m"
const val ANSI_YELLOW_BG = "\u001B[43m"
const val ANSI_BRIGHT_YELLOW_BG = "\u001B[43;1m"

const val ANSI_BLUE = "\u001B[34m"
const val ANSI_BRIGHT_BLUE = "\u001B[34;1m"

const val ANSI_GREEN = "\u001B[32m"
const val ANSI_BRIGHT_GREEN = "\u001B[32;1m"

const val ANSI_RED = "\u001B[31m"
const val ANSI_BRIGHT_RED = "\u001B[31;1m"
const val ANSI_RED_BG = "\u001B[41m"
const val ANSI_BRIGHT_RED_BG = "\u001B[41;1m"
