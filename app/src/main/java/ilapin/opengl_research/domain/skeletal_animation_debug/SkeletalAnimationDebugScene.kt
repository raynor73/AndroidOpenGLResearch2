package ilapin.opengl_research.domain.skeletal_animation_debug

import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import ilapin.engine3d.GameObject
import ilapin.engine3d.GameObjectComponent
import ilapin.engine3d.TransformationComponent
import ilapin.meshloader.MeshLoadingRepository
import ilapin.opengl_research.*
import ilapin.opengl_research.data.assets_management.OpenGLGeometryManager
import ilapin.opengl_research.data.assets_management.OpenGLTexturesManager
import ilapin.opengl_research.data.engine.MeshRendererComponent
import ilapin.opengl_research.domain.DisplayMetricsRepository
import ilapin.opengl_research.domain.Scene2
import ilapin.opengl_research.domain.engine.CameraComponent
import ilapin.opengl_research.domain.engine.MaterialComponent
import ilapin.opengl_research.domain.engine.MeshComponent
import ilapin.opengl_research.domain.engine.PerspectiveCameraComponent
import org.joml.Quaternionf
import org.joml.Vector3f
import org.joml.Vector3fc
import org.joml.Vector4f

/**
 * @author Игорь on 12.03.2020.
 */
class SkeletalAnimationDebugScene(
    displayMetricsRepository: DisplayMetricsRepository,
    meshLoadingRepository: MeshLoadingRepository,
    texturesManager: OpenGLTexturesManager,
    geometryManager: OpenGLGeometryManager,
    openGLErrorDetector: OpenGLErrorDetector,
    vectorsPool: ObjectsPool<Vector3f>
) : Scene2 {

    private val _activeCameras = ArrayList<CameraComponent>()
    private val _layerRenderers = HashMultimap.create<String, MeshRendererComponent>()
    private val _cameraAmbientLights = HashMap<CameraComponent, Vector3fc>()

    override val rootGameObject: GameObject = GameObject("root").apply {
        addComponent(TransformationComponent(Vector3f(), Quaternionf().identity(), Vector3f(1f, 1f, 1f)))
    }

    override val activeCameras: List<CameraComponent> = _activeCameras

    override val layerRenderers: Multimap<String, MeshRendererComponent> = _layerRenderers

    override val layerLights: Multimap<String, GameObjectComponent> = HashMultimap.create()

    override val cameraAmbientLights: Map<CameraComponent, Vector3fc> = _cameraAmbientLights

    override val renderTargets: List<FrameBufferInfo.RenderTargetFrameBufferInfo> = emptyList()

    init {
        val cameraGameObject = GameObject("camera").apply {
            addComponent(TransformationComponent(
                Vector3f(0f, 0f, 2f),
                Quaternionf().identity(),
                Vector3f(1f, 1f, 1f)
            ))

            val cameraComponent = PerspectiveCameraComponent(vectorsPool, 90f, listOf("defaultLayer"))
            addComponent(cameraComponent)
            _activeCameras += cameraComponent
            _cameraAmbientLights[cameraComponent] = Vector3f(0.1f, 0.1f, 0.1f)
        }
        rootGameObject.addChild(cameraGameObject)

        val sphereGameObject = GameObject("sphere").apply {
            val meshName = "sphere"
            val mesh = meshLoadingRepository.loadMesh("meshes/sphere.obj").toMesh()
            geometryManager.createStaticVertexBuffer(meshName, mesh.verticesAsArray())
            geometryManager.createStaticIndexBuffer(meshName, mesh.indices.toShortArray())

            addComponent(MeshComponent(meshName))

            addComponent(TransformationComponent(Vector3f(), Quaternionf().identity(), Vector3f(0.05f, 0.05f, 0.05f)))

            val meshRenderer = MeshRendererComponent(
                displayMetricsRepository.getPixelDensityFactor(),
                listOf("defaultLayer"),
                texturesManager,
                geometryManager,
                openGLErrorDetector
            )
            addComponent(meshRenderer)
            _layerRenderers.put("defaultLayer", meshRenderer)

            addComponent(MaterialComponent(
                textureName = null,
                diffuseColor = Vector4f(0f, 0.5f, 0f, 1f),
                isDoubleSided = false,
                isWireframe = false,
                isUnlit = true,
                isTranslucent = false,
                castShadows = false,
                receiveShadows = false
            ))
        }
        rootGameObject.addChild(sphereGameObject)
    }

    override fun update() {
        // do nothing
    }

    override fun deinit() {
        // do nothing
    }
}