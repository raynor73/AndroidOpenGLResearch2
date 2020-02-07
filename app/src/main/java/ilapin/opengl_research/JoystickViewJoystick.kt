package ilapin.opengl_research

import ilapin.opengl_research.domain.Joystick

/**
 * @author raynor on 07.02.20.
 */
class JoystickViewJoystick : Joystick {

    private var _position = Joystick.Position(0f, 0f)

    override val position: Joystick.Position
        get() = _position

    fun onPositionChanged(position: Joystick.Position) {
        _position = position
    }
}