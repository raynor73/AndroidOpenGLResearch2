package ilapin.opengl_research

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.os.SystemClock
import ilapin.engine3d.GameObject
import ilapin.engine3d.MaterialComponent
import ilapin.engine3d.TransformationComponent
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f
import org.joml.Vector4f
import java.nio.charset.Charset
import java.util.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.PI

class GLSurfaceViewRenderer(private val context: Context) : GLSurfaceView.Renderer {

    private val openGLErrorDetector = OpenGLErrorDetector()
    private val openGLObjectsRepository = OpenGLObjectsRepository(openGLErrorDetector)
    private val depthVisualizationRenderer = DepthVisualizationRendererComponent(openGLObjectsRepository, openGLErrorDetector)
    private val unlitRenderer = UnlitRendererComponent(openGLObjectsRepository, openGLErrorDetector)

    private var surfaceWidth: Int? = null
    private var surfaceHeight: Int? = null

    private val vectorsPool = ObjectsPool { Vector3f() }
    private val matrixPool = ObjectsPool { Matrix4f() }

    private var prevTimestamp: Long? = null

    private
    private val rootGameObject = GameObject("root")
    private val green = Vector4f(0f, 0.5f, 0f, 1f)

    private val renderers = LinkedList<RendererComponent>()

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

        setupTextures()
        setupGeometry()
        setupShaders()
        setupCamera()

        openGLErrorDetector.dispatchOpenGLErrors("onSurfaceChanged")
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig) {
        // do nothing
    }

    private fun setupCamera() {
        val gameObject = GameObject("camera")
        gameObject.addComponent(TransformationComponent(
            Vector3f(0f, 0f, 2f),
            Quaternionf().identity(),
            Vector3f(1f, 1f, 1f)
        ))
        gameObject.addComponent(PerspectiveCameraComponent(vectorsPool))
        rootGameObject.addChild(gameObject)
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

        renderers.clear()
        rootGameObject.findAllRendererComponents(renderers)

        val camera =

        renderers.forEach {
            when (it) {
                is DepthVisualizationRendererComponent -> {

                }

                is UnlitRendererComponent -> {
                    val meshName = it.gameObject?.getComponent(MeshComponent::class.java)!!.name
                    val transform = it.gameObject?.getComponent(TransformationComponent::class.java)!!
                    unlitRenderer.render(
                        meshName,
                        meshName,
                        modelMatrix.identity()
                            .scale(transform.scale)
                            .rotate(transform.rotation)
                            .translate(transform.position),
                        calculateViewMatrix(vectorsPool, cameraPosition, cameraRotation, viewMatrix),
                        calculateProjectionMatrix(surfaceAspect, projectionMatrix),
                        green
                    )
                }
            }
        }

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

        matrixPool.recycle(modelMatrix)
        matrixPool.recycle(viewMatrix)
        matrixPool.recycle(projectionMatrix)

        openGLErrorDetector.dispatchOpenGLErrors("render")
    }

    private fun setupTextures() {
        openGLObjectsRepository.createTexture("green", 1, 1, intArrayOf(0xff008000.toInt()))
        openGLObjectsRepository.createTexture("blue", 1, 1, intArrayOf(0xff000080.toInt()))
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

    private fun setupGeometry() {
        val mesh = MeshFactory.createQuad()
        openGLObjectsRepository.createStaticVbo("quad", mesh.verticesAsArray())
        openGLObjectsRepository.createStaticIbo("quad", mesh.indices.toShortArray())

        run {
            val gameObject = GameObject("ground_plane")
            gameObject.addComponent(TransformationComponent(
                Vector3f(0f, -2f, 0f),
                Quaternionf().identity().rotationX(-(PI / 2).toFloat()),
                Vector3f(10f, 10f, 1f)
            ))
            gameObject.addComponent(unlitRenderer)
            gameObject.addComponent(MaterialComponent("green"))
            gameObject.addComponent(MeshComponent("quad"))
            rootGameObject.addChild(gameObject)
        }

        run {
            val gameObject = GameObject("floating_plane")
            gameObject.addComponent(TransformationComponent(
                Vector3f(0f, -1f, 0f),
                Quaternionf().identity().rotationX(-(PI / 2).toFloat()),
                Vector3f(1f, 1f, 1f)
            ))
            gameObject.addComponent(unlitRenderer)
            gameObject.addComponent(MaterialComponent("blue"))
            gameObject.addComponent(MeshComponent("quad"))
            rootGameObject.addChild(gameObject)
        }
    }

    companion object {

        private const val NANOS_IN_SECOND = 1e9f
    }
}