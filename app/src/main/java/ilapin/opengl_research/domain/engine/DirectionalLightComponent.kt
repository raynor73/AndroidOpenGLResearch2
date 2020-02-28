package ilapin.opengl_research.domain.engine

import ilapin.engine3d.GameObjectComponent
import ilapin.engine3d.TransformationComponent
import ilapin.opengl_research.CAMERA_LOOK_AT_DIRECTION
import org.joml.Vector3f
import org.joml.Vector3fc

/**
 * @author raynor on 04.02.20.
 */
class DirectionalLightComponent(color: Vector3fc) : GameObjectComponent() {

    private val tmpVector = Vector3f()

    val color = Vector3f().apply { set(color) }

    val direction: Vector3fc
        get() {
            val transform = gameObject?.getComponent(TransformationComponent::class.java)
                    ?: error("Transform not found for directional light ${gameObject?.name}")
            tmpVector.set(CAMERA_LOOK_AT_DIRECTION)
            tmpVector.rotate(transform.rotation)
            return tmpVector
        }

    override fun copy(): GameObjectComponent {
        return DirectionalLightComponent(color)
    }
}