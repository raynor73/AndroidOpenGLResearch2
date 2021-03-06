package ilapin.opengl_research.domain.engine

import ilapin.engine3d.GameObjectComponent
import ilapin.opengl_research.BYTES_IN_INT
import ilapin.opengl_research.domain.assets_management.TexturesManager
import ilapin.opengl_research.domain.text.TextRenderer
import org.joml.Vector4f
import org.joml.Vector4fc
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * @author ilapin on 01.03.20.
 */
class TextComponent(
    var text: String,
    var textSize: Float,
    color: Vector4fc,
    private val imageWidth: Int,
    private val imageHeight:Int,
    private val texturesManager: TexturesManager,
    private val textRenderer: TextRenderer
) : GameObjectComponent() {

    private var renderedText: String? = null

    private val buffer = ByteBuffer.allocateDirect(imageWidth * imageHeight * BYTES_IN_INT).apply {
        order(ByteOrder.nativeOrder())
    }

    private val color = Vector4f(color)

    override fun update() {
        super.update()

        if (text == renderedText) {
            return
        }
        renderedText = text

        val textureName = gameObject?.getComponent(MaterialComponent::class.java)?.textureName
            ?: error("Can't determine texture name")

        textRenderer.drawText(text, textSize, imageWidth, imageHeight, color, buffer)
        buffer.position(0)
        texturesManager.copyDataToTexture(textureName, buffer, false)
        buffer.position(0)
    }

    override fun copy(): GameObjectComponent {
        return TextComponent(text, textSize, color, imageWidth, imageHeight, texturesManager, textRenderer)
    }
}