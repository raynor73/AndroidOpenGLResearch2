package ilapin.opengl_research.domain

import ilapin.common.input.TouchEvent
import ilapin.engine3d.GameObjectComponent

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
            return _touchEvents
        }

    fun onTouchEvent(touchEvent: TouchEvent) {
        _touchEvents += touchEvent
    }

    fun clearPrevTouchEvent() {
        _touchEvents.clear()
    }

    fun toLocalX(x: Int): Int {
        return x - left
    }

    fun toLocalY(y: Int): Int {
        return y - bottom
    }
}