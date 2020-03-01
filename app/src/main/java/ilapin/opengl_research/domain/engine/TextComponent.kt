package ilapin.opengl_research.domain.engine

import ilapin.engine3d.GameObjectComponent
import ilapin.opengl_research.domain.assets_management.TexturesManager
import org.joml.Vector4fc

/**
 * @author ilapin on 01.03.20.
 */
class TextComponent(
    text: String,
    size: Float,
    texturesManager: TexturesManager,

) : GameObjectComponent() {

    private var _text = text
    private var _size = size

    val text: String
        get() = _text

    val size: Float
        get() = _size

    override fun copy(): GameObjectComponent {
        return TextComponent(_text, _size)
    }
}