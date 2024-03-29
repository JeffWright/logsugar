package dev.jtbw.logsugar

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

/** Log everything that comes out of this Observable */
fun <T> Observable<T>.inspectEach(
  tag: String? = null,
  includeEvents: Boolean = false,
  toString: ((T) -> String?) = { it.toString() }
): Observable<T> {
  var count = 0
  val breadcrumb = Throwable()
  return this.doOnEach { notification ->
      when {
        notification.isOnNext -> {
          val item = notification.value!!
          log(tag, "[$count] -> ".maybeColorized(ANSI_BLUE) + toString(item), breadcrumb)
          count++
        }
        notification.isOnError -> {
          log(tag, "Error in Observable!", breadcrumb)
          log(tag, notification.error!!)
        }
        notification.isOnComplete -> {
          if (includeEvents) {
            log(tag, "Observable completed ($count items)", breadcrumb)
          }
        }
      }
    }
    .doOnSubscribe {
      if (includeEvents) {
        log(tag, "Subscribed to", breadcrumb)
      }
    }
    .doOnDispose {
      if (includeEvents) {
        log(tag, "Disposed", breadcrumb)
      }
    }
}

/** Log everything that comes out of this Single */
fun <T> Single<T>.inspectEach(
  tag: String? = null,
  includeEvents: Boolean = false,
  toString: ((T) -> String?) = { it.toString() }
): Single<T> {
  val breadcrumb = Throwable()
  return this.doOnSuccess { item ->
      log(tag, "Single -> ".maybeColorized(ANSI_BLUE) + toString(item), breadcrumb)
    }
    .doOnError { error ->
      log(tag, "Error in Single!", breadcrumb)
      log(tag, error)
    }
    .doOnDispose { log(tag, "Single was disposed", breadcrumb) }
    .doOnSubscribe {
      if (includeEvents) {
        log(tag, "Subscribed to", breadcrumb)
      }
    }
    .doOnDispose {
      if (includeEvents) {
        log(tag, "Disposed", breadcrumb)
      }
    }
}

/** Log everything that comes out of this Single */
fun Completable.inspectEach(tag: String? = null, includeEvents: Boolean = false): Completable {
  val breadcrumb = Throwable()
  return this.doOnComplete { log(tag, "Completed!", breadcrumb) }
    .doOnError { error ->
      log(tag, "Error in Completable!", breadcrumb)
      log(tag, error)
    }
    .doOnDispose { log(tag, "Completable was disposed", breadcrumb) }
    .doOnSubscribe {
      if (includeEvents) {
        log(tag, "Subscribed to", breadcrumb)
      }
    }
    .doOnDispose {
      if (includeEvents) {
        log(tag, "Disposed", breadcrumb)
      }
    }
}
