package ilapin.opengl_research.domain

import ilapin.common.input.TouchEvent
import ilapin.common.kotlin.safeLet

/**
 * @author raynor on 07.02.20.
 */
class ScrollController(private val touchEventsRepository: TouchEventsRepository) {

    private var prevTouchEvent: TouchEvent? = null

    var scrollEvent: ScrollEvent? = null

    fun update() {
        scrollEvent = null
        val touchEvent = touchEventsRepository.touchEvents.firstOrNull()
        safeLet(prevTouchEvent, touchEvent) { prev, current ->
            scrollEvent = ScrollEvent(current.x - prev.x, current.y - prev.y)
        }
        prevTouchEvent = touchEvent
    }
}