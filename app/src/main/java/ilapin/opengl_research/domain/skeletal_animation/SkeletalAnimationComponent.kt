package ilapin.opengl_research.domain.skeletal_animation

import ilapin.engine3d.GameObjectComponent
import org.joml.Matrix4f

/**
 * @author ilapin on 14.02.20.
 */
class SkeletalAnimationComponent(
    val rootJoint: Joint,
    val animation: SkeletalAnimation
) : GameObjectComponent() {

    init {
        rootJoint.calculateInvertedBindTransform(Matrix4f())
    }

    override fun copy(): GameObjectComponent {
        TODO("Not implemented")
    }
}