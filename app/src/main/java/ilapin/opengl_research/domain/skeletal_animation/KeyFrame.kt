package ilapin.opengl_research.domain.skeletal_animation

/**
 * @author ilapin on 11.02.20.
 */
class KeyFrame(
    val time: Float,
    val jointTransforms: Map<String, JointTransform>
)