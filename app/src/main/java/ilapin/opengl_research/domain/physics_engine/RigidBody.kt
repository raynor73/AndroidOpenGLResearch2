package ilapin.opengl_research.domain.physics_engine

import org.joml.Vector3f
import org.joml.Vector3fc

/**
 * @author raynor on 09.02.20.
 */
class RigidBody(
    position: Vector3fc,
    val type: Type
) {
    private val _position = Vector3f(position)
    private val _velocity = Vector3f()

    var position: Vector3fc
        get() = _position
        set(value) {
            _position.set(value)
        }

    val velocity: Vector3fc = _velocity

    fun addVelocity(additionalVelocity: Vector3fc) {
        _velocity.add(additionalVelocity)
    }

    fun resetVelocity() {
        _velocity.set(0f, 0f, 0f)
    }

    enum class Type {
        STATIC, KINEMATIC
    }
}