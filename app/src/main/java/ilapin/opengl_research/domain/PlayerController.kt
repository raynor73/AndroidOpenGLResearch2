package ilapin.opengl_research.domain

import kotlin.math.abs

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
        _movingFraction = -(leftJoystick.position.y.takeIf { abs(it) >= THRESHOLD } ?: 0f)
        _strafingFraction = leftJoystick.position.x.takeIf { abs(it) >= THRESHOLD } ?: 0f

        _horizontalSteeringFraction = -(rightJoystick.position.x.takeIf { abs(it) >= THRESHOLD } ?: 0f)
        _verticalSteeringFraction = -(rightJoystick.position.y.takeIf { abs(it) >= THRESHOLD } ?: 0f)
    }

    val movingFraction: Float
        get() = _movingFraction

    val strafingFraction: Float
        get() = _strafingFraction

    val horizontalSteeringFraction: Float
        get() = _horizontalSteeringFraction

    val verticalSteeringFraction: Float
        get() = _verticalSteeringFraction

    companion object {

        private const val THRESHOLD = 0.01f
    }
}