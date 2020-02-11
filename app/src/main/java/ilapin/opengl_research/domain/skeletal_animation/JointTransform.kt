package ilapin.opengl_research.domain.skeletal_animation

import org.joml.Quaternionf
import org.joml.Quaternionfc
import org.joml.Vector3f
import org.joml.Vector3fc

/**
 * @author ilapin on 11.02.20.
 */
class JointTransform(
    position: Vector3fc,
    rotation: Quaternionfc
) {
    val position: Vector3fc = Vector3f(position)
    val rotation: Quaternionfc = Quaternionf().apply { set(rotation) }
}