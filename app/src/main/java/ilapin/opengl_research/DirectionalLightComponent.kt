package ilapin.opengl_research

import ilapin.engine3d.GameObjectComponent
import org.joml.Vector3f
import org.joml.Vector3fc

/**
 * @author raynor on 04.02.20.
 */
class DirectionalLightComponent(color: Vector3fc) : GameObjectComponent() {

    val color = Vector3f().apply { set(color) }
}