package ilapin.opengl_research

import android.opengl.GLES20

/**
 * @author raynor on 05.02.20.
 */
sealed class ShaderProgramInfo(
    openGLErrorDetector: OpenGLErrorDetector,
    val shaderProgram: Int
) {
    val vertexCoordinateAttribute = GLES20.glGetAttribLocation(shaderProgram, "vertexCoordinateAttribute")
    val normalAttribute = GLES20.glGetAttribLocation(shaderProgram, "normalAttribute")
    val uvAttribute = GLES20.glGetAttribLocation(shaderProgram, "uvAttribute")

    val textureUniform = GLES20.glGetUniformLocation(shaderProgram, "textureUniform")
    val diffuseColorUniform = GLES20.glGetUniformLocation(shaderProgram, "diffuseColorUniform")
    val useDiffuseColorUniform = GLES20.glGetUniformLocation(shaderProgram, "useDiffuseColorUniform")
    val shadowMapUniform = GLES20.glGetUniformLocation(shaderProgram, "shadowMapUniform")
    val receiveShadows = GLES20.glGetUniformLocation(shaderProgram, "receiveShadows")
    val mvpMatrixUniform = GLES20.glGetUniformLocation(shaderProgram, "mvpMatrixUniform")
    val modelMatrixUniform = GLES20.glGetUniformLocation(shaderProgram, "modelMatrixUniform")
    val lightMvpMatrixUniform = GLES20.glGetUniformLocation(shaderProgram, "lightMvpMatrixUniform")
    val biasMatrixUniform = GLES20.glGetUniformLocation(shaderProgram, "biasMatrixUniform")

    init {
        openGLErrorDetector.dispatchOpenGLErrors("ShaderProgramInfo.init")
    }

    class AmbientLightShaderProgram(
        openGLErrorDetector: OpenGLErrorDetector,
        shaderProgram: Int
    ) : ShaderProgramInfo(openGLErrorDetector, shaderProgram) {
        val ambientColorUniform = GLES20.glGetUniformLocation(shaderProgram, "ambientColorUniform")

        init {
            openGLErrorDetector.dispatchOpenGLErrors("AmbientLightShaderProgram.init")
        }
    }

    class UnlitShaderProgram(
        openGLErrorDetector: OpenGLErrorDetector,
        shaderProgram: Int
    ) : ShaderProgramInfo(openGLErrorDetector, shaderProgram)

    class DirectionalLightShaderProgram(
            openGLErrorDetector: OpenGLErrorDetector,
            shaderProgram: Int
    ) : ShaderProgramInfo(openGLErrorDetector, shaderProgram) {
        val directionalLightColorUniform = GLES20.glGetUniformLocation(shaderProgram, "directionalLightUniform.color")
        val directionalLightDirectionUniform =
                GLES20.glGetUniformLocation(shaderProgram, "directionalLightUniform.direction")

        init {
            openGLErrorDetector.dispatchOpenGLErrors("DirectionalLightShaderProgram.init")
        }
    }

    class ShadowMapShaderProgram(
            openGLErrorDetector: OpenGLErrorDetector,
            shaderProgram: Int
    ) : ShaderProgramInfo(openGLErrorDetector, shaderProgram)
}