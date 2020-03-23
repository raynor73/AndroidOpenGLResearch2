package ilapin.opengl_research.domain.engine

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
    var isUnlit: Boolean = false,
    var isTranslucent: Boolean = false,
    var castShadows: Boolean = true,
    var receiveShadows: Boolean = true,
    var isSprite: Boolean = false,
    var textureCoordinatesModifier: TextureCoordinatesModifier = TextureCoordinatesModifier(0f, 0f, 1f, 1f)
) : GameObjectComponent() {

    override fun copy(): GameObjectComponent {
        return MaterialComponent(
            textureName,
            diffuseColor,
            isDoubleSided,
            isWireframe,
            isUnlit,
            isTranslucent,
            castShadows,
            receiveShadows,
            isSprite
        )
    }

    class TextureCoordinatesModifier(
        val uOffset: Float,
        val vOffset: Float,
        val uMultiplier: Float,
        val vMultiplier: Float
    )
}