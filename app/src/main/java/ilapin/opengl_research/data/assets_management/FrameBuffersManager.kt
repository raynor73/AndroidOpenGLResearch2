package ilapin.opengl_research.data.assets_management

import android.opengl.GLES20
import ilapin.opengl_research.FrameBufferInfo
import ilapin.opengl_research.OpenGLErrorDetector
import ilapin.opengl_research.TextureInfo

/**
 * @author raynor on 18.02.20.
 */
class FrameBuffersManager(
    private val texturesManager: OpenGLTexturesManager,
    private val openGLErrorDetector: OpenGLErrorDetector
) {
    private val tmpIntArray = IntArray(1)

    private val frameBuffersCreationParams = ArrayList<FrameBufferCreationParams>()

    private val frameBuffers = HashMap<String, FrameBufferInfo>()

    fun findFrameBuffer(name: String) = frameBuffers[name]

    fun restoreFrameBuffers() {
        frameBuffers.clear()

        frameBuffersCreationParams.forEach { params ->
            when (params) {
                is FrameBufferCreationParams.RenderingTarget -> createFramebuffer(
                    params.name,
                    params.width,
                    params.height
                )

                is FrameBufferCreationParams.DepthOnly -> createDepthOnlyFramebuffer(
                    params.name,
                    params.width,
                    params.height
                )
            }
        }
    }

    fun createDepthOnlyFramebuffer(name: String, width: Int, height: Int) {
        if (frameBuffers.containsKey(name)) {
            throw IllegalArgumentException("Frame buffer $name already exists")
        }

        frameBuffersCreationParams += FrameBufferCreationParams.DepthOnly(name, width, height)

        GLES20.glGenFramebuffers(1, tmpIntArray, 0)
        val framebuffer = tmpIntArray[0]

        // Try to use a texture depth component
        GLES20.glGenTextures(1, tmpIntArray, 0)
        val texture = tmpIntArray[0]
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture)

        // GL_LINEAR does not make sense for depth texture. However, next tutorial shows usage of GL_LINEAR and PCF. Using GL_NEAREST
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST)
        // Remove artifact on the edges of the shadowmap
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, framebuffer)

        // Use a depth texture
        GLES20.glTexImage2D(
            GLES20.GL_TEXTURE_2D,
            0,
            GLES20.GL_DEPTH_COMPONENT,
            width,
            height,
            0,
            GLES20.GL_DEPTH_COMPONENT,
            GLES20.GL_UNSIGNED_INT,
            null
        )
        // Attach the depth texture to FBO depth attachment point
        GLES20.glFramebufferTexture2D(
            GLES20.GL_FRAMEBUFFER,
            GLES20.GL_DEPTH_ATTACHMENT,
            GLES20.GL_TEXTURE_2D,
            texture,
            0
        )

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)

        val textureInfo = TextureInfo(texture, width, height)
        texturesManager.putTexture(name, textureInfo)
        frameBuffers[name] = FrameBufferInfo.DepthFrameBufferInfo(framebuffer, textureInfo)

        openGLErrorDetector.dispatchOpenGLErrors("createDepthOnlyFramebuffer")
        openGLErrorDetector.checkFramebufferStatus("createDepthOnlyFramebuffer")
    }

    fun createFramebuffer(name: String, width: Int, height: Int) {
        if (frameBuffers.containsKey(name)) {
            throw IllegalArgumentException("FBO $name already exists")
        }

        frameBuffersCreationParams += FrameBufferCreationParams.RenderingTarget(name, width, height)

        // Create a frame buffer
        GLES20.glGenFramebuffers(1, tmpIntArray, 0)
        val framebuffer = tmpIntArray[0]

        // Generate a texture to hold the colour buffer
        GLES20.glGenTextures(1, tmpIntArray, 0)
        val colorTexture = tmpIntArray[0]
        texturesManager.putTexture(name, TextureInfo(colorTexture, width, height))
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, colorTexture)
        // Width and height do not have to be a power of two
        GLES20.glTexImage2D(
            GLES20.GL_TEXTURE_2D,
            0,
            GLES20.GL_RGBA,
            width, height,
            0,
            GLES20.GL_RGBA,
            GLES20.GL_UNSIGNED_BYTE,
            null
        )

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST)

        // Probably just paranoia
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)

        // Create a texture to hold the depth buffer
        GLES20.glGenTextures(1, tmpIntArray, 0)
        val depthTexture = tmpIntArray[0]
        texturesManager.putTexture(name + DEPTH_COMPONENT_POSTFIX, TextureInfo(depthTexture, width, height))
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, depthTexture)

        GLES20.glTexImage2D(
            GLES20.GL_TEXTURE_2D,
            0,
            GLES20.GL_DEPTH_COMPONENT,
            width, height,
            0,
            GLES20.GL_DEPTH_COMPONENT,
            GLES20.GL_UNSIGNED_SHORT,
            null
        )

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST)

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, framebuffer)

        GLES20.glFramebufferTexture2D(
            GLES20.GL_FRAMEBUFFER,
            GLES20.GL_COLOR_ATTACHMENT0,
            GLES20.GL_TEXTURE_2D,
            colorTexture,
            0
        )

        GLES20.glFramebufferTexture2D(
            GLES20.GL_FRAMEBUFFER,
            GLES20.GL_DEPTH_ATTACHMENT,
            GLES20.GL_TEXTURE_2D,
            depthTexture,
            0
        )

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)

        frameBuffers[name] = FrameBufferInfo.RenderTargetFrameBufferInfo(
            framebuffer,
            TextureInfo(colorTexture, width, height)
        )

        openGLErrorDetector.dispatchOpenGLErrors("createFramebuffer")
    }

    fun removeFrameBuffer(name: String) {
        val frameBufferInfo = frameBuffers.remove(name) ?: error("Frame buffer $name not found")
        tmpIntArray[0] = frameBufferInfo.frameBuffer
        GLES20.glDeleteFramebuffers(1, tmpIntArray, 0)

        openGLErrorDetector.dispatchOpenGLErrors("removeFrameBuffer")

        texturesManager.removeTexture(name)

        if (frameBufferInfo is FrameBufferInfo.RenderTargetFrameBufferInfo) {
            texturesManager.removeTexture(name + DEPTH_COMPONENT_POSTFIX)
        }
    }

    fun removeAllFrameBuffers() {
        ArrayList<String>().apply { addAll(frameBuffers.keys) }.forEach { name -> removeFrameBuffer(name) }
    }

    private sealed class FrameBufferCreationParams(val name: String, val width: Int, val height: Int) {
        class RenderingTarget(name: String, width: Int, height: Int) : FrameBufferCreationParams(name, width, height)
        class DepthOnly(name: String, width: Int, height: Int) : FrameBufferCreationParams(name, width, height)
    }

    companion object {

        private const val DEPTH_COMPONENT_POSTFIX = "_depth"
    }
}