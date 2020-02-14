package ilapin.opengl_research.domain.skeletal_animation

import ilapin.engine3d.GameObjectComponent

/**
 * @author ilapin on 14.02.20.
 */
class SkeletalAnimationComponent(
    val rootJoint: Joint,
    val animation: SkeletalAnimation
) : GameObjectComponent()