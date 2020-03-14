package ilapin.opengl_research.domain.skeletal_animation

import org.joml.*

/**
 * @author ilapin on 11.02.20.
 */
class JointLocalTransform(transform: Matrix4fc) {

    private val _transform = Matrix4f().apply { set(transform) }

    val position: Vector3fc = Vector3f().apply { _transform.getTranslation(this) }
    val rotation: Quaternionfc = Quaternionf().apply { _transform.getNormalizedRotation(this) }
    val transform: Matrix4fc = _transform

    constructor(position: Vector3fc, rotation: Quaternionfc) :
        this(Matrix4f().identity().rotate(rotation).translate(position))
}