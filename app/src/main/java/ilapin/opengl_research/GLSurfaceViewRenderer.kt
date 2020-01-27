package ilapin.opengl_research

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.os.SystemClock
import org.joml.*
import java.nio.charset.Charset
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class GLSurfaceViewRenderer(private val context: Context) : GLSurfaceView.Renderer {

    private val openGLErrorDetector = OpenGLErrorDetector()
    private val openGLObjectsRepository = OpenGLObjectsRepository(openGLErrorDetector)
    private val depthVisualizationRenderer = DepthVisualizationRenderer(openGLObjectsRepository, openGLErrorDetector)
    private val unlitRenderer = UnlitRenderer(openGLObjectsRepository, openGLErrorDetector)

    private val cameraPosition = Vector3f(0f, 0f, 2f)
    private val cameraRotation = Quaternionf().identity()
    private var surfaceWidth: Int? = null
    private var surfaceHeight: Int? = null

    private val vectorsPool = ObjectsPool { Vector3f() }
    private val matrixPool = ObjectsPool { Matrix4f() }

    private var prevTimestamp: Long? = null
    private var triangleZ = 0f
    private var triangleSpeed = -1f

    private val color = Vector4f(0f, 0.5f, 0f, 1f)

    override fun onDrawFrame(gl: GL10) {
        if (openGLErrorDetector.isOpenGLErrorDetected) {
            return
        }

        val currentTimestamp = SystemClock.elapsedRealtimeNanos()
        prevTimestamp?.let {
            val dt = (currentTimestamp - it) / NANOS_IN_SECOND
            render(dt)
        }
        prevTimestamp = currentTimestamp
    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        surfaceWidth = width
        surfaceHeight = height

        GLES20.glFrontFace(GLES20.GL_CCW)
        GLES20.glCullFace(GLES20.GL_BACK)

        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glEnable(GLES20.GL_CULL_FACE)

        setupTriangle()
        setupShaders()

        openGLErrorDetector.dispatchOpenGLErrors("onSurfaceChanged")
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig) {
        // do nothing
    }

    private fun render(dt: Float) {
        val width = surfaceWidth ?: return
        val height = surfaceHeight ?: return
        val surfaceAspect = width.toFloat() / height

        GLES20.glViewport(0, 0, width, height)
        GLES20.glClearColor(0f, 0f, 0f, 1f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        val modelMatrix = matrixPool.obtain()
        val viewMatrix = matrixPool.obtain()
        val projectionMatrix = matrixPool.obtain()

        unlitRenderer.render(
            "triangle_vertices",
            "triangle_indices",
            modelMatrix.setTranslation(0.5f, 0f, triangleZ),
            calculateViewMatrix(vectorsPool, cameraPosition, cameraRotation, viewMatrix),
            calculateProjectionMatrix(surfaceAspect, projectionMatrix),
            color
        )

        val debugViewportWidth = (width / 3f).toInt()
        val debugViewportHeight = (height / 3f).toInt()
        GLES20.glScissor(0, 0, debugViewportWidth, debugViewportHeight)
        GLES20.glViewport(0, 0, debugViewportWidth, debugViewportHeight)
        GLES20.glEnable(GLES20.GL_SCISSOR_TEST)
        GLES20.glClearColor(1f, 1f, 1f, 1f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        depthVisualizationRenderer.render(
            "triangle_vertices",
            "triangle_indices",
            modelMatrix.setTranslation(0.5f, 0f, triangleZ),
            calculateViewMatrix(vectorsPool, cameraPosition, cameraRotation, viewMatrix),
            calculateProjectionMatrix(surfaceAspect, projectionMatrix)
        )

        GLES20.glDisable(GLES20.GL_SCISSOR_TEST)

        triangleZ += triangleSpeed * dt
        if (triangleZ <= -8) {
            triangleSpeed = 1f
        } else if (triangleZ >= 1) {
            triangleSpeed = -1f
        }

        matrixPool.recycle(modelMatrix)
        matrixPool.recycle(viewMatrix)
        matrixPool.recycle(projectionMatrix)

        openGLErrorDetector.dispatchOpenGLErrors("render")
    }

    private fun setupShaders() {
        openGLObjectsRepository.createVertexShader(
            "depth_visualizer_vertex_shader",
            context.assets.open("depthVisualization/vertexShader.glsl").readBytes().toString(Charset.defaultCharset())
        )
        openGLObjectsRepository.createFragmentShader(
            "depth_visualizer_fragment_shader",
            context.assets.open("depthVisualization/fragmentShader.glsl").readBytes().toString(Charset.defaultCharset())
        )
        openGLObjectsRepository.createShaderProgram(
            "depth_visualizer_shader_program",
            openGLObjectsRepository.findVertexShader("depth_visualizer_vertex_shader")!!,
            openGLObjectsRepository.findFragmentShader("depth_visualizer_fragment_shader")!!
        )

        openGLObjectsRepository.createVertexShader(
            "unlit_vertex_shader",
            context.assets.open("unlit/unlitVertexShader.glsl").readBytes().toString(Charset.defaultCharset())
        )
        openGLObjectsRepository.createFragmentShader(
            "unlit_fragment_shader",
            context.assets.open("unlit/unlitFragmentShader.glsl").readBytes().toString(Charset.defaultCharset())
        )
        openGLObjectsRepository.createShaderProgram(
            "unlit_shader_program",
            openGLObjectsRepository.findVertexShader("unlit_vertex_shader")!!,
            openGLObjectsRepository.findFragmentShader("unlit_fragment_shader")!!
        )
    }

    private fun setupTriangle() {
        val mesh = MeshFactory.createTriangle()

        val verticesComponentsArray = FloatArray(
            mesh.vertexCoordinates.size * VERTEX_COORDINATE_COMPONENTS
        )
        for (i in mesh.indices.indices) {
            verticesComponentsArray[0 + i * VERTEX_COORDINATE_COMPONENTS] = mesh.vertexCoordinates[i].x()
            verticesComponentsArray[1 + i * VERTEX_COORDINATE_COMPONENTS] = mesh.vertexCoordinates[i].y()
            verticesComponentsArray[2 + i * VERTEX_COORDINATE_COMPONENTS] = mesh.vertexCoordinates[i].z()
        }
        openGLObjectsRepository.createStaticVbo("triangle_vertices", verticesComponentsArray)

        val indicesArray = ShortArray(mesh.indices.size)
        for (i in mesh.indices.indices) {
            indicesArray[i] = mesh.indices[i]
        }
        openGLObjectsRepository.createStaticIbo("triangle_indices", indicesArray)
    }

    private fun calculateViewMatrix(
        vectorsPool: ObjectsPool<Vector3f>,
        cameraPosition: Vector3fc,
        cameraRotation: Quaternionfc,
        dest: Matrix4f
    ): Matrix4f {
        val lookAtDirection = vectorsPool.obtain()
        val up = vectorsPool.obtain()

        lookAtDirection.set(DEFAULT_LOOK_AT_DIRECTION)
        lookAtDirection.rotate(cameraRotation)
        up.set(DEFAULT_CAMERA_UP_DIRECTION)
        up.rotate(cameraRotation)

        dest.setLookAt(
            cameraPosition.x(),
            cameraPosition.y(),
            cameraPosition.z(),
            cameraPosition.x() + lookAtDirection.x,
            cameraPosition.y() + lookAtDirection.y,
            cameraPosition.z() + lookAtDirection.z,
            up.x,
            up.y,
            up.z
        )

        vectorsPool.recycle(lookAtDirection)
        vectorsPool.recycle(up)

        return dest
    }

    private fun calculateProjectionMatrix(aspect: Float, dest: Matrix4f): Matrix4f {
        dest.setPerspective(FIELD_OF_VIEW, aspect, Z_NEAR, Z_FAR)

        return dest
    }

    companion object {

        private const val NANOS_IN_SECOND = 1e9f

        private val DEFAULT_LOOK_AT_DIRECTION: Vector3fc = Vector3f(0f, 0f, -1f)
        private val DEFAULT_CAMERA_UP_DIRECTION: Vector3fc = Vector3f(0f, 1f, 0f)

        private const val FIELD_OF_VIEW = 45f
        private const val Z_NEAR = 1f
        private const val Z_FAR = 10f
    }
}