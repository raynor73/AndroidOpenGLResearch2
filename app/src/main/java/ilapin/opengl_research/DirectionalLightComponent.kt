package ilapin.opengl_research

import ilapin.engine3d.GameObjectComponent
import ilapin.engine3d.TransformationComponent
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
            tmpVector.set(INITIAL_DIRECTION)
            tmpVector.rotate(transform.rotation)
            return tmpVector
        }

    companion object {

        private val INITIAL_DIRECTION: Vector3fc = Vector3f(0f, -1f, 0f)
    }
}