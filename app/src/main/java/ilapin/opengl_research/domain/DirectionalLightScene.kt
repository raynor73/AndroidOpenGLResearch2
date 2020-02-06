package ilapin.opengl_research.domain

import android.content.Context
import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import ilapin.engine3d.GameObject
import ilapin.engine3d.GameObjectComponent
import ilapin.engine3d.TransformationComponent
import ilapin.opengl_research.*
import org.joml.Quaternionf
import org.joml.Vector3f
import org.joml.Vector3fc
import org.joml.Vector4f
import kotlin.math.PI

/**
 * @author raynor on 03.02.20.
 */
class DirectionalLightScene(
    private val context: Context,
    private val openGLObjectsRepository: OpenGLObjectsRepository,
    private val openGLErrorDetector: OpenGLErrorDetector
) : Scene2 {

    private val vectorsPool = ObjectsPool { Vector3f() }

    private val _cameras = ArrayList<CameraComponent>()

    private val _layerRenderers = HashMultimap.create<String, MeshRendererComponent>()

    private val _lights = ArrayList<GameObjectComponent>()

    private val _cameraAmbientLights = HashMap<CameraComponent, Vector3fc>()

    override val rootGameObject = GameObject("root").apply {
        addComponent(TransformationComponent(Vector3f(), Quaternionf().identity(), Vector3f(1f, 1f, 1f)))
    }

    override val cameras: List<CameraComponent> = _cameras

    override val layerRenderers: Multimap<String, MeshRendererComponent> = _layerRenderers

    override val lights: List<GameObjectComponent> = _lights

    override val cameraAmbientLights: Map<CameraComponent, Vector3fc> = _cameraAmbientLights

    override val renderTargets: List<FrameBufferInfo.RenderTargetFrameBufferInfo> = emptyList()

    init {
        setupTextures()
        setupGeometry()
        setupCameras()
        setupLights()
    }

    override fun update() {
        // do nothing
    }

    private fun setupTextures() {
        openGLObjectsRepository.createTexture("green", 1, 1, intArrayOf(0xff008000.toInt()))
        openGLObjectsRepository.createTexture("blue", 1, 1, intArrayOf(0xff000080.toInt()))
    }

    private fun setupGeometry() {
        val mesh = MeshFactory.createQuad()
        val vbo = openGLObjectsRepository.createStaticVbo("quad", mesh.verticesAsArray())
        val iboInfo = IboInfo(
            openGLObjectsRepository.createStaticIbo("quad", mesh.indices.toShortArray()),
            mesh.indices.size
        )

        run {
            val gameObject = GameObject("ground_plane")
            gameObject.addComponent(TransformationComponent(
                Vector3f(0f, -0.5f, 0f),
                Quaternionf().identity().rotateX(-(PI / 2).toFloat()),
                Vector3f(10f, 10f, 1f)
            ))
            val renderer = MeshRendererComponent(
                context.resources.displayMetrics,
                openGLObjectsRepository,
                openGLErrorDetector
            )
            layerRenderers[DEFAULT_LAYER_NAME] += renderer
            gameObject.addComponent(renderer)
            gameObject.addComponent(MaterialComponent("green", Vector4f(1f, 1f, 1f, 1f)))
            gameObject.addComponent(MeshComponent(vbo, iboInfo))
            rootGameObject.addChild(gameObject)
        }

        run {
            val gameObject = GameObject("floating_plane")
            gameObject.addComponent(TransformationComponent(
                Vector3f(0f, -0.25f, 0f),
                Quaternionf().identity().rotateX(-(PI / 2).toFloat()),
                Vector3f(1f, 1f, 1f)
            ))
            val renderer = MeshRendererComponent(
                context.resources.displayMetrics,
                openGLObjectsRepository,
                openGLErrorDetector
            )
            layerRenderers[DEFAULT_LAYER_NAME] += renderer
            gameObject.addComponent(renderer)
            gameObject.addComponent(MaterialComponent("blue", Vector4f(1f, 1f, 1f, 1f)))
            gameObject.addComponent(MeshComponent(vbo, iboInfo))
            rootGameObject.addChild(gameObject)
        }
    }

    private fun setupLights() {
        run {
            val gameObject = GameObject("directionalLight")
            gameObject.addComponent(DirectionalLightComponent(Vector3f(1f, 1f, 1f)))
            gameObject.addComponent(TransformationComponent(
                Vector3f(0f, 2f, 0f),
                Quaternionf().identity().rotateX(-(PI / 2).toFloat()),
                Vector3f(1f, 1f, 1f)
            ))
            val cameraComponent = DirectionalLightShadowMapCameraComponent(
                vectorsPool,
                10f,
                -6f, 6f, -6f, 6f
            )
            gameObject.addComponent(cameraComponent)
            rootGameObject.addChild(gameObject)
        }
    }

    private fun setupCameras() {
        run {
            val gameObject = GameObject("camera")
            gameObject.addComponent(TransformationComponent(
                Vector3f(0f, 0f, 2f),
                Quaternionf().identity(),
                Vector3f(1f, 1f, 1f)
            ))
            val cameraComponent = PerspectiveCameraComponent(
                vectorsPool,
                45f,
                listOf(DEFAULT_LAYER_NAME)
            )
            gameObject.addComponent(cameraComponent)
            rootGameObject.addChild(gameObject)
            _cameras += cameraComponent

            //_cameraAmbientLights[cameraComponent] = Vector3f(0.1f, 0.1f, 0.1f)
            _cameraAmbientLights[cameraComponent] = Vector3f(1f, 1f, 1f)
        }

        /*run {
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
        }*/
    }

    companion object {

        private const val DEFAULT_LAYER_NAME = "defaultLayer"
        //private const val UI_LAYER_NAME = "uiLayer"
    }
}