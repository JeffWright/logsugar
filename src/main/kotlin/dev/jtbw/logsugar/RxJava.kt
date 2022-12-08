package dev.jtbw.logsugar

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

/** Log everything that comes out of this Observable */
fun <T> Observable<T>.inspectEach(
  tag: String? = null,
  includeEvents: Boolean = true,
  toString: ((T) -> String?) = { it.toString() }
): Observable<T> {
  var count = 0
  val breadcrumb = Throwable()
  return this
    .doOnEach { notification ->
      when {
        notification.isOnNext -> {
          val item = notification.value!!
          log(tag, "[$count] -> ".colorized(ANSI_BLUE) + toString(item), breadcrumb)
          count++
        }
        notification.isOnError -> {
          log(tag, "Error in Observable!")
          log(tag, notification.error!!)
        }
        notification.isOnComplete -> {
          if(includeEvents) {
            log(tag, "Observable completed ($count items)")
          }
        }
      }
    }
    .doOnSubscribe {
      if (includeEvents) {
        log(tag, "Subscribed to")
      }
    }
    .doOnDispose {
      if (includeEvents) {
        log(tag, "Disposed")
      }
    }
}

/** Log everything that comes out of this Single */
fun <T> Single<T>.inspectEach(
  tag: String? = null,
  includeEvents: Boolean = true,
  toString: ((T) -> String?) = { it.toString() }
): Single<T> {
  val breadcrumb = Throwable()
  return this
    .doOnSuccess { item ->
      log(tag, "Single -> ".colorized(ANSI_BLUE) + toString(item), breadcrumb)
    }
    .doOnError { error ->
      log(tag, "Error in Single!")
      log(tag, error)
    }
    .doOnDispose {
      log(tag, "Single was disposed")
    }
    .doOnSubscribe {
      if (includeEvents) {
        log(tag, "Subscribed to")
      }
    }
    .doOnDispose {
      if (includeEvents) {
        log(tag, "Disposed")
      }
    }
}

/** Log everything that comes out of this Single */
fun Completable.inspectEach(
  tag: String? = null,
  includeEvents: Boolean = true
): Completable {
  val breadcrumb = Throwable()
  return this
    .doOnComplete {
      log(tag, "Completed!", breadcrumb)
    }
    .doOnError { error ->
      log(tag, "Error in Completable!")
      log(tag, error)
    }
    .doOnDispose {
      log(tag, "Completable was disposed")
    }
    .doOnSubscribe {
      if (includeEvents) {
        log(tag, "Subscribed to")
      }
    }
    .doOnDispose {
      if (includeEvents) {
        log(tag, "Disposed")
      }
    }
}
