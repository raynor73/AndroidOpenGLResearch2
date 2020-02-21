package ilapin.opengl_research.domain

import ilapin.common.android.log.L
import ilapin.common.input.TouchEvent
import ilapin.engine3d.GameObjectComponent
import ilapin.opengl_research.app.App.Companion.LOG_TAG

/**
 * @author raynor on 20.02.20.
 */
class GestureConsumerComponent(
    var priority: Int,
    var left: Int,
    var top: Int,
    var right: Int,
    var bottom: Int
) : GameObjectComponent() {

    private val _touchEvents = ArrayList<TouchEvent>()

    val touchEvents: List<TouchEvent>
        get() {
            L.d(LOG_TAG, "!@#: get touchEvents")
            return _touchEvents
        }

    fun onTouchEvent(touchEvent: TouchEvent) {
        _touchEvents += touchEvent
    }

    fun clearPrevTouchEvent() {
        L.d(LOG_TAG, "!@#: clearPrevTouchEvent")
        _touchEvents.clear()
    }

    fun toLocalX(x: Int): Int {
        return x - left
    }

    fun toLocalY(y: Int): Int {
        return y - bottom
    }
}