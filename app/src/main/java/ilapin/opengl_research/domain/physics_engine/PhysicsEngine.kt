package ilapin.opengl_research.domain.physics_engine

import kotlin.math.ceil

/**
 * @author raynor on 08.02.20.
 */
class PhysicsEngine(private val timeStep: Float) {

    private val dynamicsEngine = DynamicsEngine(timeStep)

    fun addRigidBody(rigidBody: RigidBody) {
        dynamicsEngine.addRigidBody(rigidBody)
    }

    fun update(dt: Float) {
        repeat(ceil((dt / timeStep)).toInt()) { dynamicsEngine.step() }
    }
}