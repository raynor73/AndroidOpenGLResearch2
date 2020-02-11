package ilapin.opengl_research.domain.skeletal_animation

/**
 * @author ilapin on 11.02.20.
 */
class SkeletalAnimation(
    val length: Float,
    keyFrames: List<KeyFrame>
) {
    val keyFrames: List<KeyFrame> = ArrayList<KeyFrame>().apply { addAll(keyFrames) }
}