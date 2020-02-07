package ilapin.opengl_research

import ilapin.common.input.TouchEvent
import ilapin.opengl_research.domain.TouchEventsRepository

/**
 * @author raynor on 07.02.20.
 */
class AndroidTouchEventsRepository : TouchEventsRepository {

    private val _touchEvents = ArrayList<TouchEvent>()

    override val touchEvents: List<TouchEvent> = _touchEvents

    fun clear() {
        _touchEvents.clear()
    }

    fun addTouchEvent(touchEvent: TouchEvent) {
        _touchEvents += touchEvent
    }
}