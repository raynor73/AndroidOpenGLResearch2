package ilapin.opengl_research

import android.opengl.GLES20
import org.joml.Matrix4f
import org.joml.Matrix4fc

/**
 * @author ilapin on 25.01.2020.
 */
class DepthRenderer(
    private val openGLObjectsRepository: OpenGLObjectsRepository,
    private val openGLErrorDetector: OpenGLErrorDetector
) {
    private val tmpFloatArray = FloatArray(16)
    private val tmpIntArray = IntArray(1)
    private val tmpMatrix = Matrix4f()
    private val tmpMatrix2 = Matrix4f()

    /*private val verticesBufferObject: Int
    private val indicesBufferObject: Int*/

    /*var surfaceWidth: Int? = null
    var surfaceHeight: Int? = null*/

    /*init {
        val vertexComponentsArray = FloatArray(
            mesh.vertexCoordinates.size * VERTEX_COORDINATE_COMPONENTS
        )
        for (i in mesh.indices) {
            vertexComponentsArray[0 + i * VERTEX_COORDINATE_COMPONENTS] = mesh.vertexCoordinates[i.toInt()].x()
            vertexComponentsArray[1 + i * VERTEX_COORDINATE_COMPONENTS] = mesh.vertexCoordinates[i.toInt()].y()
            vertexComponentsArray[2 + i * VERTEX_COORDINATE_COMPONENTS] = mesh.vertexCoordinates[i.toInt()].z()
        }
        val verticesBuffer =
            ByteBuffer.allocateDirect(vertexComponentsArray.size * BYTES_IN_FLOAT).run {
                order(ByteOrder.nativeOrder())
                asFloatBuffer().apply {
                    put(vertexComponentsArray)
                    position(0)
                }
            }

        val indicesArray = ShortArray(mesh.indices.size)
        for (i in mesh.indices.indices) {
            indicesArray[i] = mesh.indices[i]
        }
        val indicesBuffer = ByteBuffer.allocateDirect(mesh.indices.size * BYTES_IN_SHORT).run {
            order(ByteOrder.nativeOrder())
            asShortBuffer().apply {
                put(indicesArray)
                position(0)
            }
        }

        GLES20.glGenBuffers(1, tmpIntArray, 0)
        verticesBufferObject = tmpIntArray[0]
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, verticesBufferObject)
        GLES20.glBufferData(
            GLES20.GL_ARRAY_BUFFER,
            vertexComponentsArray.size * BYTES_IN_FLOAT,
            verticesBuffer,
            GLES20.GL_STATIC_DRAW
        )
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)

        GLES20.glGenBuffers(1, tmpIntArray, 0)
        indicesBufferObject = tmpIntArray[0]
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, indicesBufferObject)
        GLES20.glBufferData(
            GLES20.GL_ELEMENT_ARRAY_BUFFER,
            indicesArray.size * BYTES_IN_SHORT,
            indicesBuffer,
            GLES20.GL_STATIC_DRAW
        )
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0)

        openGLErrorDetector.dispatchOpenGLErrors("MeshRenderer.init()")
    }*/

    fun render(
        vboName: String,
        iboName: String,
        modelMatrix: Matrix4fc,
        viewMatrix: Matrix4fc,
        projectionMatrix: Matrix4fc
    ) {
        //val surfaceAspect = safeLet(surfaceWidth, surfaceHeight) { width, height -> width.toFloat() / height } ?: return

        //GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        //GLES20.glUseProgram(shaderProgram)
        val shaderProgram = openGLObjectsRepository.findShaderProgram("depth_visualization") ?: return
        val vbo = openGLObjectsRepository.findVbo(vboName) ?: return
        val ibo = openGLObjectsRepository.findIbo(iboName) ?: return

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
        tmpMatrix.set(modelMatrix)
        projectionMatrix.mul(viewMatrix, tmpMatrix)
        mvpMatrix.get(tmpFloatArray)
        GLES20.glUniformMatrix4fv(mvpMatrixUniformLocation, 1, false, tmpFloatArray, 0)

        val mvMatrixUniformLocation = GLES20.glGetUniformLocation(shaderProgramName, "mvMatrixUniform")
        val mvMatrix = tmpMatrix
        getViewProjectionMatrix(surfaceAspect, FIELD_OF_VIEW, Z_NEAR, Z_FAR).get(mvMatrix)
        // Model transformation
        mvMatrix.translate(0.5f, 0f, triangleZ)
        mvMatrix.get(tmpFloatArray)
        GLES20.glUniformMatrix4fv(mvMatrixUniformLocation, 1, false, tmpFloatArray, 0)

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

        openGLErrorDetector.dispatchOpenGLErrors("DepthRenderer.render")
    }
}