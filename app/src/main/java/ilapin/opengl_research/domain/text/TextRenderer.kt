package ilapin.opengl_research.domain.text

import org.joml.Vector4fc
import java.nio.Buffer

/**
 * @author ilapin on 01.03.20.
 */
interface TextRenderer {

    fun drawText(text: String, textSize: Float, imageWidth: Int, imageHeight: Int, color: Vector4fc, buffer: Buffer)
}