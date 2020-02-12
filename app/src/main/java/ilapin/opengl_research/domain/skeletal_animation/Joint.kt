package ilapin.opengl_research.domain.skeletal_animation

import org.joml.Matrix4f
import org.joml.Matrix4fc


/**
 * @author raynor on 11.02.20.
 */
class Joint(
    val name: String
) {
    private val _children = ArrayList<Joint>()

    private lateinit var inverseBindTransform: Matrix4f

    val children: List<Joint> = _children

    fun addChild(joint: Joint) {
        _children += joint
    }

    /**
     * This is called during set-up, after the joints hierarchy has been
     * created. This calculates the model-space bind transform of this joint
     * like so:
     *
     * `bindTransform = parentBindTransform * localBindTransform`
     *
     * where "bindTransform" is the model-space bind transform of this joint,
     * "parentBindTransform" is the model-space bind transform of the parent
     * joint, and "localBindTransform" is the bone-space bind transform of this
     * joint. It then calculates and stores the inverse of this model-space bind
     * transform, for use when calculating the final animation transform each
     * frame. It then recursively calls the method for all of the children
     * joints, so that they too calculate and store their inverse bind-pose
     * transform.
     *
     * @param parentBindTransform
     * - the model-space bind transform of the parent joint.
     */
    fun calcInverseBindTransform(parentBindTransform: Matrix4fc) {
        val bindTransform = Matrix4f.mul(parentBindTransform, localBindTransform, null)
        Matrix4f.invert(bindTransform, inverseBindTransform)
        for (child in children) {
            child.calcInverseBindTransform(bindTransform)
        }
    }
}