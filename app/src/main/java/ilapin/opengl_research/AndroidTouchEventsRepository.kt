package ilapin.opengl_research

import ilapin.common.input.TouchEvent
import ilapin.common.messagequeue.MessageQueue
import ilapin.opengl_research.domain.TouchEventsRepository
import io.reactivex.Observable
import io.reactivex.disposables.Disposable

/**
 * @author raynor on 07.02.20.
 */
class AndroidTouchEventsRepository(messageQueue: MessageQueue) : TouchEventsRepository {

    private val messageQueueSubscription = messageQueue.messages().flatMap { message ->
        if (message is TouchEvent) {
            Observable.just(message)
        } else {
            Observable.never()
        }
    }.subscribe { _touchEvents += it }

    private val _touchEvents = ArrayList<TouchEvent>()

    override val touchEvents: List<TouchEvent> = _touchEvents

    fun clearPrevEvents() {
        _touchEvents.clear()
    }

    fun deinit() {
        messageQueueSubscription.dispose()
    }
}