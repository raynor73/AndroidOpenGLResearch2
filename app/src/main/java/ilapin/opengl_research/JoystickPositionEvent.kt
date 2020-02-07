package ilapin.opengl_research

import ilapin.opengl_research.domain.Joystick

/**
 * @author raynor on 07.02.20.
 */
class JoystickPositionEvent(
    val joystickId: Int,
    val position: Joystick.Position
)