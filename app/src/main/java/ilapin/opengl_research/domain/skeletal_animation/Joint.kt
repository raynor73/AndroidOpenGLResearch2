package ilapin.opengl_research.domain.skeletal_animation

import org.joml.Matrix4f
import org.joml.Matrix4fc

/**
 * @author raynor on 11.02.20.
 */
class Joint(
    val index: Int,
    val name: String,
    localBindTransform: Matrix4fc
) {
    private val _children = ArrayList<Joint>()
    private val localBindTransform: Matrix4fc = Matrix4f(localBindTransform)

    lateinit var invertedBindTransform: Matrix4fc

    val children: List<Joint> = _children

    fun addChild(joint: Joint) {
        _children += joint
    }

    fun calculateInvertedBindTransform(parentBindTransform: Matrix4fc) {
        val bindTransform = Matrix4f()
        val invertedBindTransform = Matrix4f()

        parentBindTransform.mul(localBindTransform, bindTransform)
        bindTransform.invert(invertedBindTransform)
        this.invertedBindTransform = invertedBindTransform

        children.forEach { child -> child.calculateInvertedBindTransform(bindTransform) }
    }
}