package ilapin.opengl_research

import android.opengl.GLES20
import org.joml.Matrix4f
import org.joml.Matrix4fc
import org.joml.Vector4fc

class UnlitRendererComponent(
    private val openGLObjectsRepository: OpenGLObjectsRepository,
    private val openGLErrorDetector: OpenGLErrorDetector
) : RendererComponent() {
    private val tmpFloatArray = FloatArray(16)
    private val tmpIntArray = IntArray(1)
    private val tmpMatrix = Matrix4f()

    fun render(
        vboName: String,
        iboName: String,
        modelMatrix: Matrix4fc,
        viewMatrix: Matrix4fc,
        projectionMatrix: Matrix4fc,
        color: Vector4fc
    ) {
        if (!isEnabled) {
            return
        }

        val shaderProgram = openGLObjectsRepository.findShaderProgram("unlit_shader_program") ?: return
        val vbo = openGLObjectsRepository.findVbo(vboName) ?: return
        val ibo = openGLObjectsRepository.findIbo(iboName) ?: return

        GLES20.glUseProgram(shaderProgram)

        val vertexCoordinateAttributeLocation = GLES20.glGetAttribLocation(shaderProgram, "vertexCoordinateAttribute")

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo)
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ibo)

        GLES20.glVertexAttribPointer(
            vertexCoordinateAttributeLocation,
            VERTEX_COORDINATE_COMPONENTS,
            GLES20.GL_FLOAT,
            false,
            0,
            0
        )
        GLES20.glEnableVertexAttribArray(vertexCoordinateAttributeLocation)

        val mvpMatrixUniformLocation = GLES20.glGetUniformLocation(shaderProgram, "mvpMatrixUniform")
        tmpMatrix.set(projectionMatrix)
        tmpMatrix.mul(viewMatrix)
        tmpMatrix.mul(modelMatrix)
        tmpMatrix.get(tmpFloatArray)
        GLES20.glUniformMatrix4fv(mvpMatrixUniformLocation, 1, false, tmpFloatArray, 0)

        GLES20.glUniform4f(
            GLES20.glGetUniformLocation(shaderProgram, "color"),
            color.x(),
            color.y(),
            color.z(),
            color.w()
        )

        GLES20.glGetBufferParameteriv(GLES20.GL_ELEMENT_ARRAY_BUFFER, GLES20.GL_BUFFER_SIZE, tmpIntArray, 0)
        GLES20.glDrawElements(
            GLES20.GL_TRIANGLES,
            tmpIntArray[0] / BYTES_IN_SHORT,
            GLES20.GL_UNSIGNED_SHORT,
            0
        )

        GLES20.glDisableVertexAttribArray(vertexCoordinateAttributeLocation)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0)

        openGLErrorDetector.dispatchOpenGLErrors("UnlitRenderer.render")
    }
}