package ilapin.opengl_research.data.text

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import ilapin.opengl_research.domain.text.TextRenderer
import org.joml.Vector4fc
import java.nio.ByteBuffer

/**
 * @author ilapin on 01.03.20.
 */
class AndroidTextRenderer : TextRenderer {

    private val preAllocatedRequisite = HashMap<String, PreAllocatedRequisite>()

    override fun drawText(
        text: String,
        textSize: Float,
        imageWidth: Int,
        imageHeight: Int,
        color: Vector4fc
    ): IntArray {
        val bitmap = acquireBitmap(imageWidth, imageHeight)
        val canvas = preAllocatedCanvases.getOrElse(bitmap) { error("No canvas") }

        //bitmap.get
    }

    private fun acquireBitmap(width: Int, height: Int): Bitmap {
        val key = "${width}x${height}"
        return preAllocatedBitmaps[key] ?: run {
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            preAllocatedBitmaps[key] = bitmap
            preAllocatedCanvases[bitmap] = Canvas(bitmap)
            bitmap
        }
    }

    private class PreAllocatedRequisite(
        val bitmap: Bitmap,
        val canvas: Canvas,
        val buffer: ByteBuffer
    )
}