package ilapin.opengl_research.domain

/**
 * @author raynor on 07.02.20.
 */
class PlayerController(
    private val leftJoystick: Joystick,
    private val rightJoystick: Joystick
) {
    private var _movingFraction = 0f
    private var _strafingFraction = 0f
    private var _horizontalSteeringFraction = 0f
    private var _verticalSteeringFraction = 0f

    fun update() {
        _movingFraction = -leftJoystick.position.y
        _strafingFraction = leftJoystick.position.x

        _horizontalSteeringFraction = -rightJoystick.position.x
        _verticalSteeringFraction = -rightJoystick.position.y
    }

    val movingFraction: Float
        get() = _movingFraction

    val strafingFraction: Float
        get() = _strafingFraction

    val horizontalSteeringFraction: Float
        get() = _horizontalSteeringFraction

    val verticalSteeringFraction: Float
        get() = _verticalSteeringFraction
}