package ilapin.opengl_research.domain

import android.content.Context
import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import ilapin.engine3d.GameObject
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

    private val _layerRenderers = HashMultimap.create<String, RendererComponent>()

    private val _shadowMapCameras = ArrayList<CameraComponent>()

    private val _shadowLayerRenderers = HashMultimap.create<String, ShadowMapRendererComponent>()

    override val rootGameObject = GameObject("root").apply {
        addComponent(TransformationComponent(Vector3f(), Quaternionf().identity(), Vector3f(1f, 1f, 1f)))
    }

    override val cameras: List<CameraComponent> = _cameras

    override val layerRenderers: Multimap<String, RendererComponent> = _layerRenderers

    override val shadowMapCameras: List<CameraComponent> = _shadowMapCameras

    override val shadowLayerRenderers: Multimap<String, ShadowMapRendererComponent> = _shadowLayerRenderers

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
            shadowLayerRenderers[SHADOW_CAST_LAYER_NAME] += shadowMapRenderer
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
            val cameraComponent = DirectionalLightShadowMapCameraComponent(
                vectorsPool,
                10f,
                -6f, 6f, -6f, 6f,
                listOf(SHADOW_CAST_LAYER_NAME)
            )
            gameObject.addComponent(cameraComponent)
            rootGameObject.addChild(gameObject)
            _shadowMapCameras += cameraComponent

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