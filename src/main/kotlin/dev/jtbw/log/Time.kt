package dev.jtbw.log

internal val startTime: Long = System.currentTimeMillis()
internal val time: String
  get() = ((System.currentTimeMillis() - startTime).toFloat() / 1000f).let { "%.3f".format(it) }
