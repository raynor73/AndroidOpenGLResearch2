package ilapin.opengl_research.domain.skeletal_animation

/**
 * @author raynor on 11.02.20.
 */
class Joint(
    val name: String
) {
    private val _children = ArrayList<Joint>()

    val children: List<Joint> = _children

    fun addChild(joint: Joint) {
        _children += joint
    }
}