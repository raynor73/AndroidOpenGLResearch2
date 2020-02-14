package ilapin.opengl_research.domain.skeletal_animation

import org.joml.*

/**
 * @author ilapin on 11.02.20.
 */
class JointTransform(
    position: Vector3fc,
    rotation: Quaternionfc
) {
    private val _transform = Matrix4f()

    val position: Vector3fc = Vector3f(position)
    val rotation: Quaternionfc = Quaternionf().apply { set(rotation) }
    val transform: Matrix4fc = _transform

    init {
        _transform.translate(position).rotate(rotation)
    }
}