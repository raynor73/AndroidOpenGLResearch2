package ilapin.opengl_research.domain.engine

import ilapin.engine3d.GameObjectComponent
import ilapin.engine3d.TransformationComponent
import ilapin.opengl_research.domain.physics_engine.PhysicsEngine
import org.joml.*

/**
 * @author raynor on 08.02.20.
 */
class RigidBodyGameObjectComponent(
    private val physicsEngine: PhysicsEngine,
    private val rigidBodyName: String
) : GameObjectComponent() {

    private val rotationMatrix = Matrix4x3f()
    private val rotationQuaternion = Quaternionf()
    private val position = Vector3f()

    fun setRotation(rotation: Quaternionfc) {
        physicsEngine.setRotation(rigidBodyName, rotation)
    }

    fun setPosition(position: Vector3fc) {
        physicsEngine.setPosition(rigidBodyName, position)
    }

    fun setVelocityViaMotor(velocity: Vector3fc) {
        physicsEngine.setVelocityViaMotor(rigidBodyName, velocity)
    }

    fun setAngularVelocityViaMotor(velocity: Vector3fc) {
        physicsEngine.setAngularVelocityViaMotor(rigidBodyName, velocity)
    }

    fun setVelocityDirectly(velocity: Vector3fc) {
        physicsEngine.setVelocityDirectly(rigidBodyName, velocity)
    }

    fun setAngularVelocityDirectly(angularVelocity: Vector3fc) {
        physicsEngine.setAngularVelocityDirectly(rigidBodyName, angularVelocity)
    }

    fun addForce(force: Vector3fc) {
        physicsEngine.addForce(rigidBodyName, force)
    }

    fun addTorque(torque: Vector3fc) {
        physicsEngine.addTorque(rigidBodyName, torque)
    }

    override fun update() {
        super.update()

        val transform = gameObject?.getComponent(TransformationComponent::class.java)
            ?: error("No transform found for game object ${gameObject?.name}")

        physicsEngine.getRigidBodyRotationAndPosition(rigidBodyName, rotationMatrix, position)

        rotationQuaternion.setFromUnnormalized(rotationMatrix)

        transform.position = position
        transform.rotation = rotationQuaternion
    }

    override fun deinit() {
        super.deinit()

        physicsEngine.removeRigidBody(rigidBodyName)
    }
}