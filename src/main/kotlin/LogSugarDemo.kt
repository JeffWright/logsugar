import dev.jtbw.log.LogSugar
import dev.jtbw.log.inspect
import dev.jtbw.log.inspectEach
import dev.jtbw.log.log
import dev.jtbw.log.logDivider
import dev.jtbw.log.logStackTrace
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.runBlocking

internal fun main() {
  EntryPoint().main()
}

internal class EntryPoint {
  fun main() {
    LogSugar.configure(maxLineWidth = 300, maxLeftSectionWidth = 75) { tag, message ->
      println("com.some.package.name D/$tag: $message")
      // Or if you're on Android:
      // Log.d(tag, message)
    }

    // Basic logging:
    log(tag = "LogSugar Demo", details = "Basic Logging!")
    // tag is optional, defaults to the name of the surrounding function
    log("Just some details")
    log() // Useful breadcrumb even with no parameters

    logDivider()
    log(".inspect() extension")
    // .inspect() extension for logging values:
    val obj = "string"
    obj.inspect(tag = "obj")
    // Tag is still optional
    obj.inspect()
    // You can provide a transform to construct the message
    Person("Jeff", 42).inspect("name") { it.name }
    // .inspect() returns this, so you can chain it
    val initial = Person("Jeff", 42).name.inspect("name inline").first()

    logDivider()
    log(".inspectEach() extension")
    // .inspectEach for logging Collections...
    listOf(10, 11, 12).inspectEach("some list")
    listOf(listOf(1, 2), listOf(3, 4), listOf(5, 6)).inspectEach("another list") { it.first() }
    // ... and Maps
    mapOf("A" to 1, "B" to 2, "C" to 3).inspectEach("some map")

    // ... and Flows
    runBlocking {
      flowOf(1, 2, 3).inspectEach("some flow") { "item from flow: $it" }.launchIn(this)
    }

    logDivider()
    // Wondering how we got to this line?
    logStackTrace("How did I get here?")

    logDivider()
    // Wraps lines if the message length exceeds max:
    log("lorem", loremIpsum)
  }
}

internal data class Person(val name: String, val age: Int)

private val loremIpsum =
  """
Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.

Why do we use it?
It is a long established fact that a reader will be distracted by the readable content of a page when looking at its layout. The point of using Lorem Ipsum is that it has a more-or-less normal distribution of letters, as opposed to using 'Content here, content here', making it look like readable English. Many desktop publishing packages and web page editors now use Lorem Ipsum as their default model text, and a search for 'lorem ipsum' will uncover many web sites still in their infancy. Various versions have evolved over the years, sometimes by accident, sometimes on purpose (injected humour and the like).
"""
