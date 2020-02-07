package ilapin.opengl_research.domain

import ilapin.common.input.TouchEvent

/**
 * @author raynor on 07.02.20.
 */
interface TouchEventsRepository {

    val touchEvents: List<TouchEvent>
}