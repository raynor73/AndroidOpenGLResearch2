package ilapin.opengl_research.data.assets_management

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.opengl.GLES20
import android.opengl.GLUtils
import ilapin.opengl_research.OpenGLErrorDetector
import ilapin.opengl_research.TextureInfo
import ilapin.opengl_research.domain.assets_management.TexturesManager

/**
 * @author raynor on 18.02.20.
 */
class OpenGLTexturesManager(
    private val context: Context,
    private val openGLErrorDetector: OpenGLErrorDetector
) : TexturesManager {
    private val tmpIntArray = IntArray(1)

    private val texturesCreationParams = ArrayList<TextureCreationParams>()

    private val textures = HashMap<String, TextureInfo>()

    fun findTexture(name: String) = textures[name]

    fun putTexture(name: String, textureInfo: TextureInfo) {
        if (textures.containsKey(name)) {
            throw IllegalArgumentException("Texture $name already exists")
        }
        textures[name] = textureInfo
    }

    fun restoreTextures() {
        textures.clear()

        texturesCreationParams.forEach { params ->
            when (params) {
                is TextureCreationParams.FromPath -> createTexture(params.name, params.path)
                is TextureCreationParams.FromData -> createTexture(
                    params.name,
                    params.width,
                    params.height,
                    params.data
                )
            }
        }
    }

    override fun createTexture(name: String, path: String) {
        if (textures.containsKey(name)) {
            throw IllegalArgumentException("Texture $name already exists")
        }

        texturesCreationParams += TextureCreationParams.FromPath(name, path)

        GLES20.glGenTextures(1, tmpIntArray, 0)
        val texture = tmpIntArray[0]

        val bitmapStream = context.assets.open(path)
        val originalBitmap = BitmapFactory.decodeStream(bitmapStream)
        val flipVerticallyMatrix = Matrix()
        flipVerticallyMatrix.postScale(1f, -1f, originalBitmap.width / 2f, originalBitmap.height / 2f)
        val bitmap = Bitmap.createBitmap(
            originalBitmap,
            0,
            0,
            originalBitmap.width,
            originalBitmap.height,
            flipVerticallyMatrix,
            true
        )
        originalBitmap.recycle()
        bitmapStream.close()

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT)
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)

        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)

        textures[name] = TextureInfo(texture, bitmap.width, bitmap.height)

        bitmap.recycle()

        openGLErrorDetector.dispatchOpenGLErrors("createTexture from path")
    }

    override fun createTexture(name: String, width: Int, height: Int, data: IntArray) {
        if (textures.containsKey(name)) {
            throw IllegalArgumentException("Texture $name already exists")
        }

        texturesCreationParams += TextureCreationParams.FromData(
            name,
            width,
            height,
            IntArray(data.size) { i -> data[i] }
        )

        GLES20.glGenTextures(1, tmpIntArray, 0)
        val texture = tmpIntArray[0]

        val bitmap = Bitmap.createBitmap(data, width, height, Bitmap.Config.ARGB_8888)

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)

        bitmap.recycle()

        textures[name] = TextureInfo(texture, width, height)

        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)

        openGLErrorDetector.dispatchOpenGLErrors("createTexture from data")
    }

    private sealed class TextureCreationParams(val name: String) {
        class FromPath(name: String, val path: String) : TextureCreationParams(name)
        class FromData(name: String, val width: Int, val height: Int, val data: IntArray) : TextureCreationParams(name)
    }
}