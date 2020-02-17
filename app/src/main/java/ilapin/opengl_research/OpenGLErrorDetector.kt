package ilapin.opengl_research

import android.opengl.GLES20
import android.opengl.GLU
import ilapin.common.android.log.L
import ilapin.opengl_research.app.App.Companion.LOG_TAG
import javax.microedition.khronos.opengles.GL10

/**
 * @author ilapin on 25.01.2020.
 */
class OpenGLErrorDetector {

    private val tmpIntArray = IntArray(1)

    private val openGLErrorMap = mapOf(
        GLES20.GL_INVALID_ENUM to "GL_INVALID_ENUM",
        GLES20.GL_INVALID_VALUE to "GL_INVALID_VALUE",
        GLES20.GL_INVALID_OPERATION to "GL_INVALID_OPERATION",
        GL10.GL_STACK_OVERFLOW to "GL_STACK_OVERFLOW",
        GL10.GL_STACK_UNDERFLOW to "GL_STACK_UNDERFLOW",
        GLES20.GL_OUT_OF_MEMORY to "GL_OUT_OF_MEMORY",
        GLES20.GL_INVALID_FRAMEBUFFER_OPERATION to "GL_INVALID_FRAMEBUFFER_OPERATION"
    )

    private val framebufferStatusMap = mapOf(
        GLES20.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT to "GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT",
        GLES20.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT to "GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT",
        GLES20.GL_FRAMEBUFFER_UNSUPPORTED to "GL_FRAMEBUFFER_UNSUPPORTED"
    )

    private var _isOpenGLErrorDetected = false

    val isOpenGLErrorDetected: Boolean
        get() = _isOpenGLErrorDetected

    fun dispatchOpenGLErrors(locationName: String) {
        var error = GLES20.glGetError()
        while(error != GLES20.GL_NO_ERROR) {
            _isOpenGLErrorDetected = true
            val errorDescription = openGLErrorMap[error] ?: "Unknown error $error"
            L.d(LOG_TAG, "OpenGL error detected at $locationName: $errorDescription ${GLU.gluErrorString(error)}")
            error = GLES20.glGetError()
        }
    }

    fun dispatchShaderCompilationError(shader: Int, locationName: String) {
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, tmpIntArray, 0)
        if (tmpIntArray[0] == GLES20.GL_FALSE) {
            _isOpenGLErrorDetected = true
            L.d(LOG_TAG, "OpenGL shader compilation failure detected at $locationName: ${GLES20.glGetShaderInfoLog(shader)}")
        }
    }

    fun dispatchShaderLinkingError(shader: Int, locationName: String) {
        GLES20.glGetProgramiv(shader, GLES20.GL_LINK_STATUS, tmpIntArray, 0)
        if (tmpIntArray[0] == GLES20.GL_FALSE) {
            _isOpenGLErrorDetected = true
            L.d(LOG_TAG, "OpenGL shader linking failure detected: $locationName")
        }
    }

    fun checkFramebufferStatus(locationName: String) {
        val framebufferStatus = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER)
        if(framebufferStatus != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            _isOpenGLErrorDetected = true
            val statusDescription = framebufferStatusMap[framebufferStatus] ?: "Unknown status $framebufferStatus"
            L.d(LOG_TAG, "Incomplete framebuffer status at $locationName: $statusDescription")
        }
    }
}