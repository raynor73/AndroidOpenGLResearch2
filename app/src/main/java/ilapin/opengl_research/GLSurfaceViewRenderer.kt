package ilapin.opengl_research

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.GLU
import ilapin.common.android.log.L
import ilapin.opengl_research.App.Companion.LOG_TAG
import org.joml.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.Charset
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class GLSurfaceViewRenderer(private val context: Context) : GLSurfaceView.Renderer {

    private val cameraPosition = Vector3f(0f, 0f, 2f)
    private val cameraRotation = Quaternionf().identity()
    private var surfaceAspect: Float? = null

    private val triangleVertices: List<Vector3fc> = listOf(
        Vector3f(0f, 0.5f, 0f),
        Vector3f(-0.5f, -0.5f, 0f),
        Vector3f(0.5f, -0.5f, 0f)
    )
    private val triangleIndices = listOf<Short>(0, 1, 2)

    private val tmpIntArray = IntArray(1)
    private val tmpFloatArray = FloatArray(16)
    private val tmpVector = Vector3f()
    private val tmpVector2 = Vector3f()
    private val tmpMatrix = Matrix4f()
    private val tmpMatrix2 = Matrix4f()

    private var triangleVerticesBufferName = 0
    private var triangleIndicesBufferName = 0
    private var shaderProgramName = 0

    private val openGLErrorMap = mapOf(
        GLES20.GL_INVALID_ENUM to "GL_INVALID_ENUM",
        GLES20.GL_INVALID_VALUE to "GL_INVALID_VALUE",
        GLES20.GL_INVALID_OPERATION to "GL_INVALID_OPERATION",
        GL10.GL_STACK_OVERFLOW to "GL_STACK_OVERFLOW",
        GL10.GL_STACK_UNDERFLOW to "GL_STACK_UNDERFLOW",
        GLES20.GL_OUT_OF_MEMORY to "GL_OUT_OF_MEMORY",
        GLES20.GL_INVALID_FRAMEBUFFER_OPERATION to "GL_INVALID_FRAMEBUFFER_OPERATION"
    )
    private var isOpenGLErrorDetected = false

    override fun onDrawFrame(gl: GL10) {
        if (isOpenGLErrorDetected) {
            return
        }

        val surfaceAspect = this.surfaceAspect ?: return

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        GLES20.glUseProgram(shaderProgramName)

        val vertexCoordinateAttributeLocation = GLES20.glGetAttribLocation(
            shaderProgramName, "vertexCoordinateAttribute"
        )

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, triangleVerticesBufferName)
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, triangleIndicesBufferName)

        GLES20.glVertexAttribPointer(
            vertexCoordinateAttributeLocation,
            VERTEX_COORDINATE_COMPONENTS,
            GLES20.GL_FLOAT,
            false,
            0,
            0
        )
        GLES20.glEnableVertexAttribArray(vertexCoordinateAttributeLocation)

        val mvpMatrixUniformLocation = GLES20.glGetUniformLocation(shaderProgramName, "mvpMatrixUniform")
        val mvpMatrix = tmpMatrix
        getViewProjectionMatrix(surfaceAspect, FIELD_OF_VIEW, Z_NEAR, Z_FAR).get(mvpMatrix)
        // Model transformation
        /*mvpMatrix.translate()
        mvpMatrix.scale()
        mvpMatrix.rotate()*/
        mvpMatrix.get(tmpFloatArray)
        GLES20.glUniformMatrix4fv(mvpMatrixUniformLocation, 1, false, tmpFloatArray, 0)

        GLES20.glUniform3f(GLES20.glGetUniformLocation(shaderProgramName, "color"), 1f, 1f, 1f)

        GLES20.glDrawElements(
            GLES20.GL_TRIANGLES,
            triangleIndices.size,
            GLES20.GL_UNSIGNED_SHORT,
            0
        )

        GLES20.glDisableVertexAttribArray(vertexCoordinateAttributeLocation)

        dispatchOpenGLErrors("onDrawFrame()")
    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        surfaceAspect = width.toFloat() / height.toFloat()

        GLES20.glViewport(0, 0, width, height)
        GLES20.glClearColor(0f, 0f, 0.5f, 1f)

        GLES20.glFrontFace(GLES20.GL_CCW)
        GLES20.glCullFace(GLES20.GL_BACK)

        //GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        //GLES20.glEnable(GLES20.GL_CULL_FACE)

        setupTriangle()
        setupShaders()

        dispatchOpenGLErrors("onSurfaceChanged()")
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig) {
        // do nothing
    }

    private fun dispatchOpenGLErrors(locationName: String) {
        var error = GLES20.glGetError()
        while(error != GLES20.GL_NO_ERROR) {
            isOpenGLErrorDetected = true
            val errorDescription = openGLErrorMap[error] ?: "Unknown error $error"
            L.d(LOG_TAG, "OpenGL error detected at $locationName: $errorDescription ${GLU.gluErrorString(error)}")
            error = GLES20.glGetError()
        }
    }

    private fun dispatchShaderError(shader: Int, locationName: String) {
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, tmpIntArray, 0)
        if (tmpIntArray[0] == GLES20.GL_FALSE) {
            isOpenGLErrorDetected = true
            L.d(LOG_TAG, "OpenGL shader compilation failure detected at $locationName: ${GLES20.glGetShaderInfoLog(shader)}")
        }
    }

    private fun setupShaders() {
        val vertexShaderName = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER)
        GLES20.glShaderSource(
            vertexShaderName,
            context.assets.open("vertexShader.glsl").readBytes().toString(Charset.defaultCharset())
        )
        GLES20.glCompileShader(vertexShaderName)
        dispatchShaderError(vertexShaderName, "GLES20.glCompileShader(vertexShaderName)")

        val fragmentShaderName = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER)
        GLES20.glShaderSource(
            fragmentShaderName,
            context.assets.open("fragmentShader.glsl").readBytes().toString(Charset.defaultCharset())
        )
        GLES20.glCompileShader(fragmentShaderName)
        dispatchShaderError(fragmentShaderName, "GLES20.glCompileShader(fragmentShaderName)")

        shaderProgramName = GLES20.glCreateProgram()
        GLES20.glAttachShader(shaderProgramName, vertexShaderName)
        GLES20.glAttachShader(shaderProgramName, fragmentShaderName)
        GLES20.glLinkProgram(shaderProgramName)

        GLES20.glGetProgramiv(shaderProgramName, GLES20.GL_LINK_STATUS, tmpIntArray, 0)
        if (tmpIntArray[0] == GLES20.GL_FALSE) {
            isOpenGLErrorDetected = true
            L.d(LOG_TAG, "OpenGL shader linking failure detected: ${GLES20.glGetShaderInfoLog(shaderProgramName)}")
        }

        dispatchOpenGLErrors("setupShaders()")
    }

    private fun setupTriangle() {
        val verticesComponentsArray = FloatArray(
            triangleVertices.size * VERTEX_COORDINATE_COMPONENTS
        )
        for (i in triangleVertices.indices) {
            verticesComponentsArray[0 + i * VERTEX_COORDINATE_COMPONENTS] = triangleVertices[i].x()
            verticesComponentsArray[1 + i * VERTEX_COORDINATE_COMPONENTS] = triangleVertices[i].y()
            verticesComponentsArray[2 + i * VERTEX_COORDINATE_COMPONENTS] = triangleVertices[i].z()
        }
        val verticesBuffer =
            ByteBuffer.allocateDirect(verticesComponentsArray.size * BYTES_PER_FLOAT).run {
                order(ByteOrder.nativeOrder())
                asFloatBuffer().apply {
                    put(verticesComponentsArray)
                    position(0)
                }
            }

        val indicesArray = ShortArray(triangleIndices.size)
        for (i in triangleIndices.indices) {
            indicesArray[i] = triangleIndices[i]
        }
        val indicesBuffer = ByteBuffer.allocateDirect(triangleIndices.size * BYTES_PER_SHORT).run {
            order(ByteOrder.nativeOrder())
            asShortBuffer().apply {
                put(indicesArray)
                position(0)
            }
        }

        GLES20.glGenBuffers(1, tmpIntArray, 0)
        triangleVerticesBufferName = tmpIntArray[0]
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, triangleVerticesBufferName)
        GLES20.glBufferData(
            GLES20.GL_ARRAY_BUFFER,
            verticesComponentsArray.size * BYTES_PER_FLOAT,
            verticesBuffer,
            GLES20.GL_STATIC_DRAW
        )
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)

        GLES20.glGenBuffers(1, tmpIntArray, 0)
        triangleIndicesBufferName = tmpIntArray[0]
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, triangleIndicesBufferName)
        GLES20.glBufferData(
            GLES20.GL_ELEMENT_ARRAY_BUFFER,
            indicesArray.size * BYTES_PER_SHORT,
            indicesBuffer,
            GLES20.GL_STATIC_DRAW
        )
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0)

        dispatchOpenGLErrors("setupTriangle()")
    }

    private fun getViewProjectionMatrix(
        aspect: Float,
        fov: Float,
        zNear: Float,
        zFar: Float
    ): Matrix4fc {
        val projectionMatrix = tmpMatrix
        val viewProjectionMatrix = tmpMatrix2
        val lookAtDirection = tmpVector
        val up = tmpVector2

        lookAtDirection.set(DEFAULT_LOOK_AT_DIRECTION)
        lookAtDirection.rotate(cameraRotation)
        up.set(DEFAULT_CAMERA_UP_DIRECTION)
        up.rotate(cameraRotation)

        projectionMatrix.identity().perspective(
            Math.toRadians(fov.toDouble()).toFloat(),
            aspect,
            zNear,
            zFar
        )

        return projectionMatrix.lookAt(
            cameraPosition.x,
            cameraPosition.y,
            cameraPosition.z,
            cameraPosition.x + lookAtDirection.x,
            cameraPosition.y + lookAtDirection.y,
            cameraPosition.z + lookAtDirection.z,
            up.x,
            up.y,
            up.z,
            viewProjectionMatrix
        )
    }

    companion object {

        private const val BYTES_PER_FLOAT = 4
        private const val BYTES_PER_SHORT = 2
        private const val VERTEX_COORDINATE_COMPONENTS = 3

        private val DEFAULT_LOOK_AT_DIRECTION: Vector3fc = Vector3f(0f, 0f, -1f)
        private val DEFAULT_CAMERA_UP_DIRECTION: Vector3fc = Vector3f(0f, 1f, 0f)

        private const val FIELD_OF_VIEW = 45f
        private const val Z_NEAR = 0.1f
        private const val Z_FAR = 5000f
    }
}