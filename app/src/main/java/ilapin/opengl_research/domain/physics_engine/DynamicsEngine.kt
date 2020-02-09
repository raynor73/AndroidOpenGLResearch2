package ilapin.opengl_research.domain.physics_engine

import org.joml.Vector3f

/**
 * @author raynor on 09.02.20.
 */
class DynamicsEngine(private val timeStep: Float) {

    private val tmpVector = Vector3f()

    private val rigidBodies = ArrayList<RigidBody>()

    fun addRigidBody(rigidBody: RigidBody) {
        rigidBodies += rigidBody
    }

    fun step() {
        rigidBodies.filter { it.type == RigidBody.Type.KINEMATIC }.forEach { rigidBody ->
            tmpVector.set(rigidBody.velocity)
            tmpVector.mul(timeStep)

            tmpVector.add(rigidBody.position)
            rigidBody.position = tmpVector

            rigidBody.resetVelocity()
        }
    }
}