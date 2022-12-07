import dev.jtbw.logsugar.LogSugar
import dev.jtbw.logsugar.countOccurrence
import dev.jtbw.logsugar.getNumOccurrences
import dev.jtbw.logsugar.inspect
import dev.jtbw.logsugar.inspectEach
import dev.jtbw.logsugar.log
import dev.jtbw.logsugar.logDivider
import dev.jtbw.logsugar.logOccurrence
import dev.jtbw.logsugar.logStackTrace
import dev.jtbw.logsugar.logTiming
import dev.jtbw.logsugar.logWtf
import dev.jtbw.logsugar.runTiming
import dev.jtbw.logsugar.runTimingSuspend
import dev.jtbw.logsugar.startTiming
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.runBlocking

internal fun main() {
  EntryPoint().main()
}

internal class EntryPoint {
  fun main() {
    LogSugar.configure(maxLineWidth = 200, maxLeftSectionWidth = 75) { tag, message ->
      println("com.some.package.name D/$tag: $message")
      // Or if you're on Android:
      // Log.d(tag, message)
    }

    logDivider("Basic Logging: log()")
    log(tag = "Custom left tag", details = "You can provide both the left tag and the details")
    log("\uD83D\uDC48 But tag is optional and defaults to the name of the surrounding function")
    log()
    log("☝️but even just log() is useful, giving you a clickable link to the class & function name")

    logDivider(".inspect(): effortlessly log the value of variables")
    val person = Person("Jeff", 42)
    person.inspect(tag = "optional tag")
    log("You can provide a transform to construct the message:")
    person.inspect("name") { "Custom: name = ${it.name}" }
    val firstInitial =
      person.name
        .inspect("name inline") { "$it -- notice .inspect() is chainable" }
        .first()
        .inspect("first initial")

    logDivider(".inspectEach() extension")
    log("On Collections...")
    listOf(10, 11, 12).inspectEach("some list")
    log("On Maps...")
    mapOf("A" to 1, "B" to 2, "C" to 3).inspectEach("some map")

    log("On Flows...")
    runBlocking { flowOf(1, 2, 3).inspectEach("some flow").launchIn(this) }
    log("All versions of .inspectEach() still have the optional transform:")
    runBlocking { flowOf(1, 2, 3).inspectEach("some flow") { "item was $it" }.launchIn(this) }

    logDivider("Exceptions & Stack Traces")
    runCatching { null!! }.exceptionOrNull()?.inspect("oops!")
    log("Wondering how we got to this line?")
    logStackTrace("How did I get here?")

    logDivider("Wraps long lines automatically")
    log("lorem", loremIpsum)

    logDivider("Dividers are useful!")
    log("Multiple weights for all your divider-ing needs!")
    (5 downTo 0).forEach { w ->
      logDivider("Weight $w", weight = w)
      log()
    }

    logDivider("Timing")
    log("Start & check a stopwatch by key:")
    val key = "timingKey" // can be any type that supports ==
    startTiming(key, alsoLog = true)
    Thread.sleep(200)
    logTiming(key)

    log("Or, time a function or block of code:")
    runTiming { Thread.sleep(500) }
    runTiming("sleep") { Thread.sleep(500) }
    runTiming("sleep", "a sleepy little thread") { Thread.sleep(500) }

    log("Of course it also supports suspend functions")
    runBlocking { runTimingSuspend("suspend delay") { delay(300) } }

    logDivider("use logWtf() for highly visible log statements")
    log("Uh oh, something went wrong...")
    logWtf("This should never happen")
    log("Message is optional, because we know sometimes you just need to throw out a good ol'")
    logWtf()

    logDivider("Counting")
    log("Sometimes you want to track how many times something occurs")
    (5 downTo 0)
      .toList()
      .sortedWith { a, b ->
        logOccurrence("comparison", "$a <=> $b")
        a.compareTo(b)
      }
      .inspect("sorted!")

    log("Or maybe you don't want to log every single one")
    (5 downTo 0)
      .toList()
      .sortedWith { a, b ->
        countOccurrence("B")
        a.compareTo(b)
      }
      .inspect("sorted again!") { "$it sorted in ${getNumOccurrences("B")} steps" }
  }
}

internal data class Person(val name: String, val age: Int)

private val loremIpsum =
  """
Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.

Why do we use it?
It is a long established fact that a reader will be distracted by the readable content of a page when looking at its layout. The point of using Lorem Ipsum is that it has a more-or-less normal distribution of letters, as opposed to using 'Content here, content here', making it look like readable English. Many desktop publishing packages and web page editors now use Lorem Ipsum as their default model text, and a search for 'lorem ipsum' will uncover many web sites still in their infancy. Various versions have evolved over the years, sometimes by accident, sometimes on purpose (injected humour and the like).
"""
