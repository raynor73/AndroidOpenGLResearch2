package ilapin.opengl_research

import android.opengl.GLES20
import ilapin.engine3d.MaterialComponent
import org.joml.Matrix4f
import org.joml.Matrix4fc

/**
 * @author raynor on 03.02.20.
 */
class DirectionalLightRenderer(
    private val openGLObjectsRepository: OpenGLObjectsRepository,
    private val openGLErrorDetector: OpenGLErrorDetector
) : RendererComponent() {
    private val tmpFloatArray = FloatArray(16)
    private val tmpMatrix = Matrix4f()

    fun render(
        vboName: String,
        iboName: String,
        modelMatrix: Matrix4fc,
        viewMatrix: Matrix4fc,
        projectionMatrix: Matrix4fc,
        lightModelMatrix: Matrix4fc,
        lightViewMatrix: Matrix4fc,
        lightProjectionMatrix: Matrix4fc
    ) {
        if (!isEnabled) {
            return
        }

        val shaderProgram = openGLObjectsRepository.findShaderProgram("directional_light_shader_program") ?: return
        val vbo = openGLObjectsRepository.findVbo(vboName) ?: return
        val iboInfo = openGLObjectsRepository.findIbo(iboName) ?: return
        val material = gameObject?.getComponent(MaterialComponent::class.java) ?: return

        GLES20.glUseProgram(shaderProgram)

        val vertexCoordinateAttributeLocation = GLES20.glGetAttribLocation(shaderProgram, "vertexCoordinateAttribute")
        val uvAttributeLocation = GLES20.glGetAttribLocation(shaderProgram, "uvAttribute")

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo)
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, iboInfo.ibo)

        GLES20.glVertexAttribPointer(
            vertexCoordinateAttributeLocation,
            VERTEX_COORDINATE_COMPONENTS,
            GLES20.GL_FLOAT,
            false,
            (VERTEX_COORDINATE_COMPONENTS + TEXTURE_COORDINATE_COMPONENTS) * BYTES_IN_FLOAT,
            0
        )
        GLES20.glEnableVertexAttribArray(vertexCoordinateAttributeLocation)

        GLES20.glVertexAttribPointer(
            uvAttributeLocation,
            TEXTURE_COORDINATE_COMPONENTS,
            GLES20.GL_FLOAT,
            false,
            (VERTEX_COORDINATE_COMPONENTS + TEXTURE_COORDINATE_COMPONENTS) * BYTES_IN_FLOAT,
            VERTEX_COORDINATE_COMPONENTS * BYTES_IN_FLOAT
        )
        GLES20.glEnableVertexAttribArray(uvAttributeLocation)

        val lightMvpMatrixUniform = GLES20.glGetUniformLocation(shaderProgram, "lightMvpMatrixUniform")
        tmpMatrix.set(lightProjectionMatrix)
        tmpMatrix.mul(lightViewMatrix)
        tmpMatrix.mul(lightModelMatrix)
        tmpMatrix.get(tmpFloatArray)
        GLES20.glUniformMatrix4fv(lightMvpMatrixUniform, 1, false, tmpFloatArray, 0)

        val biasMatrixUniform = GLES20.glGetUniformLocation(shaderProgram, "biasMatrixUniform")
        BIAS_MATRIX.get(tmpFloatArray)
        GLES20.glUniformMatrix4fv(biasMatrixUniform, 1, false, tmpFloatArray, 0)

        val mvpMatrixUniformLocation = GLES20.glGetUniformLocation(shaderProgram, "mvpMatrixUniform")
        tmpMatrix.set(projectionMatrix)
        tmpMatrix.mul(viewMatrix)
        tmpMatrix.mul(modelMatrix)
        tmpMatrix.get(tmpFloatArray)
        GLES20.glUniformMatrix4fv(mvpMatrixUniformLocation, 1, false, tmpFloatArray, 0)

        val textureUniformLocation = GLES20.glGetUniformLocation(shaderProgram, "textureUniform")
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, openGLObjectsRepository.findTexture(material.textureName)!!.texture)
        GLES20.glUniform1i(textureUniformLocation, 0)

        val shadowMapUniformLocation = GLES20.glGetUniformLocation(shaderProgram, "shadowMapUniform")
        val depthFrameBufferInfo = (openGLObjectsRepository.findFrameBuffer("shadowMap") as FrameBufferInfo.DepthFrameBufferInfo)
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1)
        GLES20.glBindTexture(
            GLES20.GL_TEXTURE_2D,
            depthFrameBufferInfo.depthTextureInfo.texture
        )
        GLES20.glUniform1i(shadowMapUniformLocation, 0)

        GLES20.glDrawElements(
            GLES20.GL_TRIANGLES,
            iboInfo.numberOfIndices,
            GLES20.GL_UNSIGNED_SHORT,
            0
        )

        GLES20.glDisableVertexAttribArray(vertexCoordinateAttributeLocation)
        GLES20.glDisableVertexAttribArray(uvAttributeLocation)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0)

        openGLErrorDetector.dispatchOpenGLErrors("DirectionalLightRenderer.render")
    }

    companion object {

        private val BIAS_MATRIX: Matrix4fc = Matrix4f(
            0.5f, 0.0f, 0.0f, 0.0f,
            0.0f, 0.5f, 0.0f, 0.0f,
            0.0f, 0.0f, 0.5f, 0.0f,
            0.5f, 0.5f, 0.5f, 1.0f
        ).transpose()
    }
}