package ilapin.opengl_research

import ilapin.common.messagequeue.MessageQueue
import ilapin.opengl_research.domain.Joystick
import io.reactivex.Observable

/**
 * @author raynor on 07.02.20.
 */
class JoystickViewJoystick(messageQueue: MessageQueue, joystickId: Int) : Joystick {

    private val messageQueueSubscription = messageQueue.messages().flatMap { message ->
        if (message is JoystickPositionEvent && message.joystickId == joystickId) {
            Observable.just(message)
        } else {
            Observable.never()
        }
    }.subscribe { _position = it.position }

    private var _position = Joystick.Position(0f, 0f)

    override val position: Joystick.Position
        get() = _position

    fun deinit() {
        messageQueueSubscription.dispose()
    }
}