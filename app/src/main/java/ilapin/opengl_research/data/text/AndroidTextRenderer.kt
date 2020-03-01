package ilapin.opengl_research.data.text

import android.content.Context
import android.graphics.*
import ilapin.opengl_research.domain.text.TextRenderer
import ilapin.opengl_research.toArgb
import org.joml.Vector4fc
import java.nio.Buffer

/**
 * @author ilapin on 01.03.20.
 */
class AndroidTextRenderer(private val context: Context) : TextRenderer {

    private val preAllocatedRequisites = HashMap<String, TextRenderingRequisite>()

    override fun drawText(
        text: String,
        textSize: Float,
        imageWidth: Int,
        imageHeight: Int,
        color: Vector4fc,
        buffer: Buffer
    ) {
        val requisite = acquireRequisite(imageWidth, imageHeight)

        requisite.paint.apply {
            this.textSize = textSize
            this.color = color.toArgb()
        }

        requisite.canvas.drawColor(0xffffffff.toInt(), PorterDuff.Mode.CLEAR)
        requisite.canvas.drawText(
            text, 0f, textSize, requisite.paint
        )

        requisite.flippedBitmapCanvas.drawColor(0xffffffff.toInt(), PorterDuff.Mode.CLEAR)
        requisite.flippedBitmapCanvas.drawBitmap(requisite.bitmap, requisite.flipVerticallyMatrix, null)

        requisite.flippedBitmap.copyPixelsToBuffer(buffer)
    }

    override fun clear() {
        preAllocatedRequisites.clear()
    }

    private fun acquireRequisite(width: Int, height: Int): TextRenderingRequisite {
        val key = "${width}x${height}"
        return preAllocatedRequisites[key] ?: run {
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val flippedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val flipVerticallyMatrix = Matrix().apply {
                postScale(1f, -1f, width / 2f, height / 2f)
            }
            val canvas = Canvas(bitmap)
            val flippedBitmapCanvas = Canvas(flippedBitmap)
            val typeface = Typeface.createFromAsset(context.assets, "fonts/roboto/Roboto-Regular.ttf")
            val paint = Paint().apply {
                isAntiAlias = true
                style = Paint.Style.FILL
                textAlign = Paint.Align.LEFT
                setTypeface(typeface)
            }
            val requisite = TextRenderingRequisite(
                bitmap,
                flippedBitmap,
                flipVerticallyMatrix,
                canvas,
                flippedBitmapCanvas,
                paint
            )
            preAllocatedRequisites[key] = requisite
            requisite
        }
    }

    private class TextRenderingRequisite(
        val bitmap: Bitmap,
        val flippedBitmap: Bitmap,
        val flipVerticallyMatrix: Matrix,
        val canvas: Canvas,
        val flippedBitmapCanvas: Canvas,
        val paint: Paint
    )
}