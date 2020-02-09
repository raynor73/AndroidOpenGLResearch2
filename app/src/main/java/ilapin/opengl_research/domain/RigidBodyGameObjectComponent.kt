package ilapin.opengl_research.domain

import ilapin.engine3d.GameObjectComponent
import ilapin.engine3d.TransformationComponent
import ilapin.opengl_research.domain.physics_engine.RigidBody

/**
 * @author raynor on 08.02.20.
 */
class RigidBodyGameObjectComponent(val rigidBody: RigidBody) : GameObjectComponent() {

    override fun update() {
        super.update()

        val transform = gameObject?.getComponent(TransformationComponent::class.java)
            ?: error("Transform not found for ${gameObject?.name}")

        transform.position = rigidBody.position
    }
}