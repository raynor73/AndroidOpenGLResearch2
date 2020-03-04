package ilapin.opengl_research.domain.skeletal_animation

import ilapin.engine3d.GameObjectComponent
import org.joml.Matrix4f

/**
 * @author ilapin on 14.02.20.
 */
class SkeletalAnimationComponent(
    animations: List<SkeletalAnimationData>
) : GameObjectComponent() {

    private val animations = ArrayList<SkeletalAnimationData>().apply { addAll(animations) }

    private var currentAnimation: SkeletalAnimationData = this.animations[0]

    val rootJoint: Joint
        get() = currentAnimation.rootJoint

    val animation: SkeletalAnimation
        get()= currentAnimation.animation

    fun selectAnimation(index: Int) {
        currentAnimation = animations[index]
    }

    override fun copy(): GameObjectComponent {
        return SkeletalAnimationComponent(animations)
    }
}