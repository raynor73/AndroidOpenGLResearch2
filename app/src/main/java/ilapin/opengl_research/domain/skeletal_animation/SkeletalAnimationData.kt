package ilapin.opengl_research.domain.skeletal_animation

import org.joml.Matrix4f

/**
 * @author ilapin on 05.03.20.
 */
class SkeletalAnimationData(
    val rootJoint: Joint,
    val animation: SkeletalAnimation
) {
    init {
        rootJoint.calculateInvertedBindTransform(Matrix4f())
    }
}