package ilapin.opengl_research.domain

import android.content.Context
import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import ilapin.engine3d.GameObject
import ilapin.engine3d.GameObjectComponent
import ilapin.engine3d.MaterialComponent
import ilapin.engine3d.TransformationComponent
import ilapin.opengl_research.*
import org.joml.Quaternionf
import org.joml.Vector3f
import java.nio.charset.Charset
import kotlin.math.PI

/**
 * @author raynor on 03.02.20.
 */
class DirectionalLightScene(
    private val context: Context,
    displayWidth: Int,
    displayHeight: Int,
    private val openGLObjectsRepository: OpenGLObjectsRepository,
    private val openGLErrorDetector: OpenGLErrorDetector
) : Scene2 {

    private val vectorsPool = ObjectsPool { Vector3f() }

    private val _cameras = ArrayList<CameraComponent>()

    private val _layerRenderers = HashMultimap.create<String, GameObjectComponent>()

    override val rootGameObject = GameObject("root").apply {
        addComponent(TransformationComponent(Vector3f(), Quaternionf().identity(), Vector3f(1f, 1f, 1f)))
    }

    override val cameras: List<CameraComponent> = _cameras

    override val layerRenderers: Multimap<String, GameObjectComponent> = _layerRenderers

    var directionalLightShadowMapCamera: GameObject? = null

    init {
        setupTextures()
        setupGeometry(displayWidth, displayHeight)
        setupShaders()
        setupCameras(displayWidth, displayHeight)
        setupFrameBuffers(displayWidth, displayHeight)
    }

    override fun update() {
        // do nothing
    }

    private fun setupTextures() {
        openGLObjectsRepository.createTexture("green", 1, 1, intArrayOf(0xff008000.toInt()))
        openGLObjectsRepository.createTexture("blue", 1, 1, intArrayOf(0xff000080.toInt()))
    }

    private fun setupGeometry(displayWidth: Int, displayHeight: Int) {
        val mesh = MeshFactory.createQuad()
        openGLObjectsRepository.createStaticVbo("quad", mesh.verticesAsArray())
        openGLObjectsRepository.createStaticIbo("quad", mesh.indices.toShortArray())

        run {
            val gameObject = GameObject("ground_plane")
            gameObject.addComponent(TransformationComponent(
                Vector3f(0f, -0.5f, 0f),
                Quaternionf().identity().rotateX(-(PI / 2).toFloat()),
                Vector3f(10f, 10f, 1f)
            ))
            val renderer = DirectionalLightRenderer(openGLObjectsRepository, openGLErrorDetector)
            layerRenderers[DEFAULT_LAYER_NAME] += renderer
            gameObject.addComponent(renderer)
            gameObject.addComponent(MaterialComponent("green"))
            gameObject.addComponent(MeshComponent("quad"))
            rootGameObject.addChild(gameObject)
        }

        run {
            val gameObject = GameObject("floating_plane")
            gameObject.addComponent(TransformationComponent(
                Vector3f(0f, -0.25f, 0f),
                Quaternionf().identity().rotateX(-(PI / 2).toFloat()),
                Vector3f(1f, 1f, 1f)
            ))
            val renderer = UnlitRendererComponent(openGLObjectsRepository, openGLErrorDetector)
            layerRenderers[DEFAULT_LAYER_NAME] += renderer
            gameObject.addComponent(renderer)

            val shadowMapRenderer = ShadowMapRendererComponent(openGLObjectsRepository, openGLErrorDetector)
            layerRenderers[SHADOW_CAST_LAYER_NAME] += shadowMapRenderer
            gameObject.addComponent(shadowMapRenderer)

            gameObject.addComponent(MaterialComponent("blue"))
            gameObject.addComponent(MeshComponent("quad"))
            rootGameObject.addChild(gameObject)
        }

        run {
            val gameObject = GameObject("shadow_map_texture_visualization")
            val size = displayWidth / 3f
            gameObject.addComponent(TransformationComponent(
                Vector3f(size / 2f, size / 2f, -2f),
                Quaternionf().identity(),
                Vector3f(size, size, 1f)
            ))
            val renderer = ShadowMapVisualizationRenderer(openGLObjectsRepository, openGLErrorDetector)
            layerRenderers[UI_LAYER_NAME] += renderer
            gameObject.addComponent(renderer)

            gameObject.addComponent(MeshComponent("quad"))
            rootGameObject.addChild(gameObject)
        }
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

        openGLObjectsRepository.createVertexShader(
            "shadow_map_vertex_shader",
            context.assets.open("shadowMap/shadowMapVertexShader.glsl").readBytes().toString(Charset.defaultCharset())
        )
        openGLObjectsRepository.createFragmentShader(
            "shadow_map_fragment_shader",
            context.assets.open("shadowMap/shadowMapFragmentShader.glsl").readBytes().toString(Charset.defaultCharset())
        )
        openGLObjectsRepository.createShaderProgram(
            "shadow_map_shader_program",
            openGLObjectsRepository.findVertexShader("shadow_map_vertex_shader")!!,
            openGLObjectsRepository.findFragmentShader("shadow_map_fragment_shader")!!
        )

        openGLObjectsRepository.createVertexShader(
            "shadow_map_visualization_vertex_shader",
            context.assets.open("unlit/unlitVertexShader.glsl").readBytes().toString(Charset.defaultCharset())
        )
        openGLObjectsRepository.createFragmentShader(
            "shadow_map_visualization_fragment_shader",
            context
                .assets
                .open("depthVisualization/depthTextureFragmentShader.glsl")
                .readBytes()
                .toString(Charset.defaultCharset())
        )
        openGLObjectsRepository.createShaderProgram(
            "shadow_map_visualization_shader_program",
            openGLObjectsRepository.findVertexShader("shadow_map_visualization_vertex_shader")!!,
            openGLObjectsRepository.findFragmentShader("shadow_map_visualization_fragment_shader")!!
        )

        openGLObjectsRepository.createVertexShader(
            "directional_light_vertex_shader",
            context
                .assets
                .open("directionalLight/directionalLightVertexShader.glsl")
                .readBytes()
                .toString(Charset.defaultCharset())
        )
        openGLObjectsRepository.createFragmentShader(
            "directional_light_fragment_shader",
            context
                .assets
                .open("directionalLight/directionalLightFragmentShader.glsl")
                .readBytes()
                .toString(Charset.defaultCharset())
        )
        openGLObjectsRepository.createShaderProgram(
            "directional_light_shader_program",
            openGLObjectsRepository.findVertexShader("directional_light_vertex_shader")!!,
            openGLObjectsRepository.findFragmentShader("directional_light_fragment_shader")!!
        )
    }

    // TODO Find out why issues appear if framebuffer is not the same size as display
    private fun setupFrameBuffers(displayWidth: Int, displayHeight: Int) {
        openGLObjectsRepository.createDepthOnlyFramebuffer("shadowMap", displayWidth, displayHeight)
    }

    private fun setupCameras(displayWidth: Int, displayHeight: Int) {
        run {
            val gameObject = GameObject("camera")
            gameObject.addComponent(TransformationComponent(
                Vector3f(0f, 0f, 2f),
                Quaternionf().identity(),
                Vector3f(1f, 1f, 1f)
            ))
            val cameraComponent = PerspectiveCameraComponent(vectorsPool, listOf(DEFAULT_LAYER_NAME))
            gameObject.addComponent(cameraComponent)
            rootGameObject.addChild(gameObject)
            _cameras += cameraComponent
        }

        run {
            val gameObject = GameObject("directionalLightShadowMapCamera")
            gameObject.addComponent(TransformationComponent(
                Vector3f(0f, 2f, 0f),
                Quaternionf().identity().rotateX(-(PI / 2).toFloat()),
                Vector3f(1f, 1f, 1f)
            ))
            val cameraComponent = OrthoCameraComponent(
                vectorsPool,
                -6f, 6f, -6f, 6f,
                listOf(SHADOW_CAST_LAYER_NAME)
            )
            gameObject.addComponent(cameraComponent)
            rootGameObject.addChild(gameObject)
            _cameras += cameraComponent

            directionalLightShadowMapCamera = gameObject
        }

        run {
            val gameObject = GameObject("uiCamera")
            gameObject.addComponent(TransformationComponent(
                Vector3f(0f, 0f, 0f),
                Quaternionf().identity(),
                Vector3f(1f, 1f, 1f)
            ))
            val cameraComponent = OrthoCameraComponent(
                vectorsPool,
                0f, displayWidth.toFloat(),
                0f, displayHeight.toFloat(),
                listOf(UI_LAYER_NAME)
            )
            gameObject.addComponent(cameraComponent)
            rootGameObject.addChild(gameObject)
            _cameras += cameraComponent
        }
    }

    companion object {

        private const val DEFAULT_LAYER_NAME = "defaultLayer"
        private const val SHADOW_CAST_LAYER_NAME = "shadowCastLayer"
        private const val UI_LAYER_NAME = "uiLayer"
    }
}