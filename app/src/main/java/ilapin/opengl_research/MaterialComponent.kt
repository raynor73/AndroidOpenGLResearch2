package ilapin.opengl_research

import ilapin.engine3d.GameObjectComponent
import org.joml.Vector4f

/**
 * @author raynor on 05.02.20.
 */
class MaterialComponent(
    var textureName: String?,
    val diffuseColor: Vector4f,
    var isDoubleSided: Boolean = false,
    var isWireframe: Boolean = false,
    var isUnlit: Boolean = false
) : GameObjectComponent()