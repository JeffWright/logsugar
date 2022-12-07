## 🍬 LogSugar: syntax sugar for debug logs 🍬

Debuggers are all well and good, but when you have to break out the debug log statements, they should be powerful!

LogSugar is a small library intended to make debugging via log statements easier.

## Features
1. Click to jump to where you logged from (like you'd see in stack traces)
2. Extension functions for inspecting values
3. Measure elapsed time & count occurrences
4. ...etc

## Demo

See [LogSugarDemo.kt](src/main/kotlin/LogSugarDemo.kt)

## Result:
[View full size](img/result.png)

![](img/result.png)

## Usage:

### Configure
Call this from wherever you want. `configure()` has additional options.
```kotlin
LogSugar.configure { tag, message ->
  println("com.some.package.name D/$tag: $message")
  // Or if you're on Android:
  // Log.d(tag, message)
}
```

Author: Jeff Wright
