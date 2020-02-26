package ilapin.opengl_research.domain.engine

import ilapin.engine3d.GameObjectComponent
import ilapin.engine3d.TransformationComponent
import ilapin.opengl_research.domain.physics_engine.PhysicsEngine
import org.joml.Matrix4x3f
import org.joml.Quaternionf
import org.joml.Vector3f

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

    override fun update() {
        super.update()

        val transform = gameObject?.getComponent(TransformationComponent::class.java)
            ?: error("No transform found for game object ${gameObject?.name}")

        physicsEngine.getRigidBodyRotationAndPosition(rigidBodyName, rotationMatrix, position)

        rotationQuaternion.setFromUnnormalized(rotationMatrix)

        transform.position = position
        transform.rotation = rotationQuaternion
    }
}