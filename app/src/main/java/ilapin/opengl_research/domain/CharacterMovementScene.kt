package ilapin.opengl_research.domain

import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import ilapin.engine3d.GameObject
import ilapin.engine3d.GameObjectComponent
import ilapin.engine3d.TransformationComponent
import ilapin.meshloader.MeshLoadingRepository
import ilapin.opengl_research.*
import org.joml.Quaternionf
import org.joml.Vector3f
import org.joml.Vector3fc
import org.joml.Vector4f
import kotlin.math.PI

/**
 * @author raynor on 07.02.20.
 */
class CharacterMovementScene(
    private val openGLObjectsRepository: OpenGLObjectsRepository,
    private val openGLErrorDetector: OpenGLErrorDetector,
    private val meshLoadingRepository: MeshLoadingRepository,
    displayMetricsRepository: DisplayMetricsRepository,
    private val scrollController: ScrollController
) : Scene2 {

    private val vectorsPool = ObjectsPool { Vector3f() }
    private val tmpQuaternion = Quaternionf()

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

    private val pixelDensityFactor = displayMetricsRepository.getPixelDensityFactor()
    private var xAngle = -(PI / 2).toFloat()
    private var zAngle = -(PI / 4).toFloat()

    private lateinit var directionalLightTransform: TransformationComponent
    private lateinit var playerTransform: TransformationComponent

    init {
        setupTextures()
        setupGeometry()
        setupCameras()
        setupLights()
    }

    override fun update() {
        scrollController.scrollEvent?.let { scrollEvent ->
            zAngle -= Math.toRadians((scrollEvent.dx / pixelDensityFactor).toDouble()).toFloat()
            xAngle -=  Math.toRadians((scrollEvent.dy / pixelDensityFactor).toDouble()).toFloat()

            tmpQuaternion.identity()
            tmpQuaternion.rotateZ(zAngle).rotateX(xAngle)
            directionalLightTransform.rotation = tmpQuaternion
        }
    }

    private fun setupTextures() {
        openGLObjectsRepository.createTexture("green", 1, 1, intArrayOf(0xff008000.toInt()))
        openGLObjectsRepository.createTexture("blue", 1, 1, intArrayOf(0xff000080.toInt()))
    }

    private fun setupGeometry() {
        val mesh = MeshFactory.createQuad()
        val quadVbo = openGLObjectsRepository.createStaticVbo("quad", mesh.verticesAsArray())
        val quadIboInfo = IboInfo(
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
                pixelDensityFactor,
                openGLObjectsRepository,
                openGLErrorDetector
            )
            layerRenderers[DEFAULT_LAYER_NAME] += renderer
            gameObject.addComponent(renderer)
            gameObject.addComponent(MaterialComponent("green", Vector4f(1f, 1f, 1f, 1f)))
            gameObject.addComponent(MeshComponent(quadVbo, quadIboInfo))
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
                pixelDensityFactor,
                openGLObjectsRepository,
                openGLErrorDetector
            )
            layerRenderers[DEFAULT_LAYER_NAME] += renderer
            gameObject.addComponent(renderer)
            gameObject.addComponent(MaterialComponent("blue", Vector4f(1f, 1f, 1f, 1f)))
            gameObject.addComponent(MeshComponent(quadVbo, quadIboInfo))
            rootGameObject.addChild(gameObject)
        }

        run {
            val playerMesh = meshLoadingRepository.loadMesh("meshes/cube.obj").toMesh()
            val playerMeshVbo = openGLObjectsRepository.createStaticVbo("player", playerMesh.verticesAsArray())
            val playerMeshIboInfo = IboInfo(
                openGLObjectsRepository.createStaticIbo("player", playerMesh.indices.toShortArray()),
                playerMesh.indices.size
            )

            val gameObject = GameObject("player")
            playerTransform = TransformationComponent(
                Vector3f(0f, 0f, -2f),
                Quaternionf().identity(),
                Vector3f(1f, 1f, 1f)
            )
            gameObject.addComponent(playerTransform)
            val renderer = MeshRendererComponent(
                pixelDensityFactor,
                openGLObjectsRepository,
                openGLErrorDetector
            )
            layerRenderers[DEFAULT_LAYER_NAME] += renderer
            gameObject.addComponent(renderer)
            gameObject.addComponent(MaterialComponent(null, Vector4f(0f, 1f, 1f, 1f)))
            gameObject.addComponent(MeshComponent(playerMeshVbo, playerMeshIboInfo))
            rootGameObject.addChild(gameObject)
        }
    }

    private fun setupLights() {
        run {
            val gameObject = GameObject("directionalLight")
            val lightComponent = DirectionalLightComponent(Vector3f(1f, 1f, 1f))
            gameObject.addComponent(lightComponent)
            directionalLightTransform = TransformationComponent(
                Vector3f(0f, 0f, 0f),
                Quaternionf().identity().rotateZ(zAngle).rotateX(xAngle),
                Vector3f(1f, 1f, 1f)
            )
            gameObject.addComponent(directionalLightTransform)
            val halfShadowSize = GLOBAL_DIRECTIONAL_LIGHT_SHADOW_SIZE / 2
            val cameraComponent = DirectionalLightShadowMapCameraComponent(
                vectorsPool,
                GLOBAL_DIRECTIONAL_LIGHT_DISTANCE_FROM_VIEWER,
                -halfShadowSize,
                halfShadowSize,
                -halfShadowSize,
                halfShadowSize
            )
            cameraComponent.zNear = 1f
            cameraComponent.zFar = 2 * GLOBAL_DIRECTIONAL_LIGHT_DISTANCE_FROM_VIEWER
            gameObject.addComponent(cameraComponent)
            rootGameObject.addChild(gameObject)

            _lights += lightComponent
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

            _cameraAmbientLights[cameraComponent] = Vector3f(0.5f, 0.5f, 0.5f)
        }
    }

    companion object {

        private const val DEFAULT_LAYER_NAME = "defaultLayer"
    }
}