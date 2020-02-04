package ilapin.opengl_research

import android.opengl.GLES20
import org.joml.Matrix4f
import org.joml.Matrix4fc

/**
 * @author ilapin on 2020-01-30.
 */
class ShadowMapRendererComponent(
    private val openGLObjectsRepository: OpenGLObjectsRepository,
    private val openGLErrorDetector: OpenGLErrorDetector
) : RendererComponent() {
    private val tmpFloatArray = FloatArray(16)
    private val tmpMatrix = Matrix4f()

    fun render(
        vboName: String,
        iboName: String,
        frameBufferName: String,
        modelMatrix: Matrix4fc,
        viewMatrix: Matrix4fc,
        projectionMatrix: Matrix4fc
    ) {
        if (!isEnabled) {
            return
        }

        val shaderProgram = openGLObjectsRepository.findShaderProgram("shadow_map_shader_program") ?: return
        val frameBufferInfo =
            openGLObjectsRepository.findFrameBuffer(frameBufferName) as FrameBufferInfo.DepthFrameBufferInfo
        val vbo = openGLObjectsRepository.findVbo(vboName) ?: return
        val iboInfo = openGLObjectsRepository.findIbo(iboName) ?: return

        GLES20.glUseProgram(shaderProgram)
        val vertexCoordinateAttributeLocation = GLES20.glGetAttribLocation(shaderProgram, "vertexCoordinateAttribute")

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

        val mvpMatrixUniformLocation = GLES20.glGetUniformLocation(shaderProgram, "mvpMatrixUniform")
        tmpMatrix.set(projectionMatrix)
        tmpMatrix.mul(viewMatrix)
        tmpMatrix.mul(modelMatrix)
        tmpMatrix.get(tmpFloatArray)
        GLES20.glUniformMatrix4fv(mvpMatrixUniformLocation, 1, false, tmpFloatArray, 0)

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBufferInfo.frameBuffer)

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        GLES20.glDrawElements(
            GLES20.GL_TRIANGLES,
            iboInfo.numberOfIndices,
            GLES20.GL_UNSIGNED_SHORT,
            0
        )

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
        GLES20.glDisableVertexAttribArray(vertexCoordinateAttributeLocation)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0)

        openGLErrorDetector.dispatchOpenGLErrors("ShadowMapRendererComponent.render")
    }
}