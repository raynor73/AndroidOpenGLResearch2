package ilapin.opengl_research

import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * @author ilapin on 25.01.2020.
 */
class OpenGLObjectsRepository(private val openGLErrorDetector: OpenGLErrorDetector) {

    private val textures = HashMap<String, TextureInfo>()
    private val fbos = HashMap<String, Int>()
    private val vbos = HashMap<String, Int>()
    private val ibos = HashMap<String, IboInfo>()
    private val vertexShaders = HashMap<String, Int>()
    private val fragmentShaders = HashMap<String, Int>()
    private val shaderPrograms = HashMap<String, Int>()

    private val tmpIntArray = IntArray(1)

    fun findVbo(name: String) = vbos[name]
    fun findIbo(name: String) = ibos[name]
    fun findVertexShader(name: String) = vertexShaders[name]
    fun findFragmentShader(name: String) = fragmentShaders[name]
    fun findShaderProgram(name: String) = shaderPrograms[name]
    fun findTexture(name: String) = textures[name]

    fun createStaticVbo(name: String, verticesData: FloatArray): Int {
        if (vbos.containsKey(name)) {
            throw IllegalArgumentException("VBO $name already exists")
        }

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
        if (ibos.containsKey(name)) {
            throw IllegalArgumentException("IBO $name already exists")
        }

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

        openGLErrorDetector.dispatchOpenGLErrors("createStaticIbo")

        return ibo
    }

    fun createVertexShader(name: String, source: String): Int {
        if (vertexShaders.containsKey(name)) {
            throw IllegalArgumentException("Vertex shader $name already exists")
        }

        val shader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER)
        GLES20.glShaderSource(shader, source)
        GLES20.glCompileShader(shader)

        vertexShaders[name] = shader

        openGLErrorDetector.dispatchShaderCompilationError(shader, "createVertexShader")

        return shader
    }

    fun createFragmentShader(name: String, source: String): Int {
        if (fragmentShaders.containsKey(name)) {
            throw IllegalArgumentException("Fragment shader $name already exists")
        }

        val shader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER)
        GLES20.glShaderSource(shader, source)
        GLES20.glCompileShader(shader)

        fragmentShaders[name] = shader

        openGLErrorDetector.dispatchShaderCompilationError(shader, "createFragmentShader")

        return shader
    }

    fun createShaderProgram(name: String, vertexShader: Int, fragmentShader: Int): Int {
        if (shaderPrograms.containsKey(name)) {
            throw IllegalArgumentException("Shader program $name already exists")
        }

        val shaderProgram = GLES20.glCreateProgram()
        GLES20.glAttachShader(shaderProgram, vertexShader)
        GLES20.glAttachShader(shaderProgram, fragmentShader)
        GLES20.glLinkProgram(shaderProgram)

        shaderPrograms[name] = shaderProgram

        openGLErrorDetector.dispatchShaderLinkingError(shaderProgram, "createShaderProgram")
        openGLErrorDetector.dispatchOpenGLErrors("createShaderProgram")

        return shaderProgram
    }

    fun createTexture(name: String, width: Int, height: Int, data: IntArray) {
        if (textures.containsKey(name)) {
            throw IllegalArgumentException("Texture $name already exists")
        }

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
    }

    private fun createFramebuffer(name: String, width: Int, height: Int) {
        if (fbos.containsKey(name)) {
            throw IllegalArgumentException("FBO $name already exists")
        }

        // Create a frame buffer
        GLES20.glGenFramebuffers(1, tmpIntArray, 0)
        val framebuffer = tmpIntArray[0]

        // Generate a texture to hold the colour buffer
        GLES20.glGenTextures(1, tmpIntArray, 0)
        val colorTexture = tmpIntArray[0]
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

        // Associate the textures with the FBO.
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

        fbos[name] = framebuffer

        /*GLES20.glGenFramebuffers(1, tmpIntArray, 0)
        framebufferName = tmpIntArray[0]
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, framebufferName)

        GLES20.glGenTextures(1, tmpIntArray, 0)
        val colorTexture = tmpIntArray[0]
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, colorTexture)

        GLES20.glTexImage2D(
            GLES20.GL_TEXTURE_2D,
            0,
            GLES20.GL_RGBA,
            width,
            height,
            0,
            GLES20.GL_RGBA,
            GLES20.GL_UNSIGNED_BYTE,
            null
        )
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)

        GLES20.glGenRenderbuffers(1, tmpIntArray, 0)
        val depthRenderBuffer = tmpIntArray[0]
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, depthRenderBuffer)
        GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16, width, height)
        GLES20.glFramebufferRenderbuffer(
            GLES20.GL_FRAMEBUFFER,
            GLES20.GL_DEPTH_ATTACHMENT,
            GLES20.GL_RENDERBUFFER,
            depthRenderBuffer
        )

        // Set "renderedTexture" as our colour attachement #0
        GLES20.glFramebufferTexture2D(
            GLES20.GL_FRAMEBUFFER,
            GLES20.GL_COLOR_ATTACHMENT0,
            GLES20.GL_TEXTURE_2D,
            colorTexture,
            0
        )

        // Set the list of draw buffers.
        //GLenum DrawBuffers[1] = {GL_COLOR_ATTACHMENT0};
        //GLES20.glDra glDrawBuffers(1, DrawBuffers); // "1" is the size of DrawBuffers

        val framebufferStatus = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER)
        if(framebufferStatus != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            isOpenGLErrorDetected = true
            val statusDescription = framebufferStatusMap[framebufferStatus] ?: "Unknown status $framebufferStatus"
            L.d(LOG_TAG, "Incomplete framebuffer status: $statusDescription")
        }

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, 0)
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)*/

        /*GLES20.glGenFramebuffers(1, tmpIntArray, 0)
        framebufferName = tmpIntArray[0]
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, framebufferName)

        GLES20.glGenTextures(1, tmpIntArray, 0)
        val textureId = tmpIntArray[0]
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)

        GLES20.glTexImage2D(
            GLES20.GL_TEXTURE_2D,
            0,
            GLES20.GL_DEPTH_COMPONENT,
            width,
            height,
            0,
            GLES20.GL_DEPTH_COMPONENT,
            GLES20.GL_UNSIGNED_SHORT,
            null
        )
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)

        GLES20.glFramebufferTexture2D(
            GLES20.GL_FRAMEBUFFER,
            GLES20.GL_DEPTH_ATTACHMENT,
            GLES20.GL_TEXTURE_2D,
            textureId,
            0
        )

        val framebufferStatus = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER)
        if(framebufferStatus != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            isOpenGLErrorDetected = true
            val statusDescription = framebufferStatusMap[framebufferStatus] ?: "Unknown status $framebufferStatus"
            L.d(LOG_TAG, "Incomplete framebuffer status: $statusDescription")
        }

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)*/

        openGLErrorDetector.dispatchOpenGLErrors("createFramebuffer")
    }
}