package ilapin.opengl_research

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * @author ilapin on 25.01.2020.
 */
class OpenGLObjectsRepository(private val openGLErrorDetector: OpenGLErrorDetector) {

    private val textures = HashMap<String, Int>()
    private val fbos = HashMap<String, Int>()
    private val vbos = HashMap<String, Int>()
    private val ibos = HashMap<String, Int>()
    private val vertexShaders = HashMap<String, Int>()
    private val fragmentShaders = HashMap<String, Int>()
    private val shaderPrograms = HashMap<String, Int>()

    private val tmpIntArray = IntArray(1)

    fun findVbo(name: String) = vbos[name]
    fun findIbo(name: String) = ibos[name]
    fun findVertexShader(name: String) = vertexShaders[name]
    fun findFragmentShader(name: String) = fragmentShaders[name]
    fun findShaderProgram(name: String) = shaderPrograms[name]

    fun createStaticVbo(name: String, verticesData: FloatArray): Int {
        val verticesBuffer = ByteBuffer.allocateDirect(verticesData.size * BYTES_IN_FLOAT).apply {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(verticesData)
                position(0)
            }
        }

        GLES20.glGenBuffers(1, tmpIntArray, 0)
        val vbo = tmpIntArray[0]
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo)
        GLES20.glBufferData(
            GLES20.GL_ARRAY_BUFFER,
            verticesData.size * BYTES_IN_FLOAT,
            verticesBuffer,
            GLES20.GL_STATIC_DRAW
        )
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)

        vbos[name] = vbo

        openGLErrorDetector.dispatchOpenGLErrors("createStaticVbo")

        return vbo
    }

    fun createStaticIbo(name: String, indices: ShortArray): Int {
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

        ibos[name] = ibo

        openGLErrorDetector.dispatchOpenGLErrors("createStaticIbo")

        return ibo
    }

    fun createVertexShader(name: String, source: String): Int {
        val shader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER)
        GLES20.glShaderSource(shader, source)
        GLES20.glCompileShader(shader)

        vertexShaders[name] = shader

        openGLErrorDetector.dispatchShaderCompilationError(shader, "createVertexShader")

        return shader
    }

    fun createFragmentShader(name: String, source: String): Int {
        val shader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER)
        GLES20.glShaderSource(shader, source)
        GLES20.glCompileShader(shader)

        fragmentShaders[name] = shader

        openGLErrorDetector.dispatchShaderCompilationError(shader, "createFragmentShader")

        return shader
    }

    fun createShaderProgram(name: String, vertexShader: Int, fragmentShader: Int): Int {
        val shaderProgram = GLES20.glCreateProgram()
        GLES20.glAttachShader(shaderProgram, vertexShader)
        GLES20.glAttachShader(shaderProgram, fragmentShader)
        GLES20.glLinkProgram(shaderProgram)

        shaderPrograms[name] = shaderProgram

        openGLErrorDetector.dispatchShaderLinkingError(shaderProgram, "createShaderProgram")
        openGLErrorDetector.dispatchOpenGLErrors("createShaderProgram")

        return shaderProgram
    }

    //fun createTexture
}