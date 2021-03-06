package ilapin.opengl_research.data.assets_management

import android.opengl.GLES20
import ilapin.opengl_research.BYTES_IN_FLOAT
import ilapin.opengl_research.BYTES_IN_SHORT
import ilapin.opengl_research.IboInfo
import ilapin.opengl_research.OpenGLErrorDetector
import ilapin.opengl_research.domain.assets_management.GeometryManager
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * @author raynor on 18.02.20.
 */
class OpenGLGeometryManager(
    private val openGLErrorDetector: OpenGLErrorDetector
) : GeometryManager {
    private val tmpIntArray = IntArray(1)

    private val buffersCreationParams = ArrayList<BufferCreationParams>()

    private val vbos = HashMap<String, Int>()
    private val ibos = HashMap<String, IboInfo>()

    fun findVbo(name: String) = vbos[name]
    fun findIbo(name: String) = ibos[name]

    fun restoreBuffers() {
        vbos.clear()
        ibos.clear()

        buffersCreationParams.forEach { params ->
            when (params) {
                is BufferCreationParams.Vertices -> createStaticVertexBuffer(params.name, params.verticesData)
                is BufferCreationParams.Indices -> createStaticIndexBuffer(params.name, params.indices)
            }
        }
    }

    override fun createStaticVertexBuffer(name: String, vertexData: FloatArray) {
        if (vbos.containsKey(name)) {
            throw IllegalArgumentException("VBO $name already exists")
        }

        buffersCreationParams += BufferCreationParams.Vertices(
            name,
            FloatArray(vertexData.size) { i -> vertexData[i] }
        )

        val verticesBuffer = ByteBuffer.allocateDirect(vertexData.size * BYTES_IN_FLOAT).apply {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(vertexData)
                position(0)
            }
        }

        GLES20.glGenBuffers(1, tmpIntArray, 0)
        val vbo = tmpIntArray[0]
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo)
        GLES20.glBufferData(
            GLES20.GL_ARRAY_BUFFER,
            vertexData.size * BYTES_IN_FLOAT,
            verticesBuffer,
            GLES20.GL_STATIC_DRAW
        )
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)

        vbos[name] = vbo

        openGLErrorDetector.dispatchOpenGLErrors("createStaticVerticesBuffer")
    }

    override fun createStaticIndexBuffer(name: String, indices: ShortArray) {
        if (ibos.containsKey(name)) {
            throw IllegalArgumentException("IBO $name already exists")
        }

        buffersCreationParams += BufferCreationParams.Indices(
            name,
            ShortArray(indices.size) { i -> indices[i] }
        )

        val indicesBuffer = ByteBuffer.allocateDirect(indices.size * BYTES_IN_SHORT).apply {
            order(ByteOrder.nativeOrder())
            asShortBuffer().apply {
                put(indices)
                position(0)
            }
        }

        GLES20.glGenBuffers(1, tmpIntArray, 0)
        val ibo = tmpIntArray[0]
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ibo)
        GLES20.glBufferData(
            GLES20.GL_ELEMENT_ARRAY_BUFFER,
            indices.size * BYTES_IN_SHORT,
            indicesBuffer,
            GLES20.GL_STATIC_DRAW
        )
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0)

        ibos[name] = IboInfo(ibo, indices.size)

        openGLErrorDetector.dispatchOpenGLErrors("createStaticIndicesBuffer")
    }

    override fun removeStaticVertexBuffer(name: String) {
        tmpIntArray[0] = vbos.remove(name) ?: error("Static vertex buffer $name not found")
        GLES20.glDeleteBuffers(1, tmpIntArray, 0)
        openGLErrorDetector.dispatchOpenGLErrors("removeStaticVertexBuffer")
    }

    override fun removeStaticIndexBuffer(name: String) {
        tmpIntArray[0] = (ibos.remove(name) ?: error("Static vertex buffer $name not found")).ibo
        GLES20.glDeleteBuffers(1, tmpIntArray, 0)
        openGLErrorDetector.dispatchOpenGLErrors("removeStaticIndexBuffer")
    }

    override fun removeAllBuffers() {
        ArrayList<String>().apply { addAll(vbos.keys) }.forEach { name -> removeStaticVertexBuffer(name) }
        ArrayList<String>().apply { addAll(ibos.keys) }.forEach { name -> removeStaticIndexBuffer(name) }
    }

    private sealed class BufferCreationParams(val name: String) {
        class Vertices(name: String, val verticesData: FloatArray) : BufferCreationParams(name)
        class Indices(name: String, val indices: ShortArray) : BufferCreationParams(name)
    }
}