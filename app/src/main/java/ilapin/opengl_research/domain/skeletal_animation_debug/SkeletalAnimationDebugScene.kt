package ilapin.opengl_research.domain.skeletal_animation_debug

import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import ilapin.common.android.log.L
import ilapin.common.time.TimeRepository
import ilapin.engine3d.GameObject
import ilapin.engine3d.GameObjectComponent
import ilapin.engine3d.TransformationComponent
import ilapin.meshloader.MeshLoadingRepository
import ilapin.opengl_research.*
import ilapin.opengl_research.app.App.Companion.LOG_TAG
import ilapin.opengl_research.data.assets_management.OpenGLGeometryManager
import ilapin.opengl_research.data.assets_management.OpenGLTexturesManager
import ilapin.opengl_research.data.engine.MeshRendererComponent
import ilapin.opengl_research.domain.DisplayMetricsRepository
import ilapin.opengl_research.domain.Scene2
import ilapin.opengl_research.domain.engine.CameraComponent
import ilapin.opengl_research.domain.engine.MaterialComponent
import ilapin.opengl_research.domain.engine.MeshComponent
import ilapin.opengl_research.domain.engine.PerspectiveCameraComponent
import ilapin.opengl_research.domain.skeletal_animation.AnimatedMeshRepository
import ilapin.opengl_research.domain.skeletal_animation.Joint
import ilapin.opengl_research.domain.skeletal_animation.KeyFrame
import org.joml.*

/**
 * @author Игорь on 12.03.2020.
 */
class SkeletalAnimationDebugScene(
    displayMetricsRepository: DisplayMetricsRepository,
    meshLoadingRepository: MeshLoadingRepository,
    texturesManager: OpenGLTexturesManager,
    geometryManager: OpenGLGeometryManager,
    openGLErrorDetector: OpenGLErrorDetector,
    private val vectorsPool: ObjectsPool<Vector3f>,
    private val matrixPool: ObjectsPool<Matrix4f>,
    private val quaternionPool: ObjectsPool<Quaternionf>,
    animatedMeshRepository: AnimatedMeshRepository,
    private val timeRepository: TimeRepository
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

    private val spherePrefab: GameObject

    private val debugGameObject: GameObject
    private val debugGameObjectTransform: TransformationComponent
    private var debugGameObjectYAngle = 0f

    private var prevTimestamp: Long? = null

    init {
        val cameraGameObject = GameObject("camera").apply {
            addComponent(TransformationComponent(
                Vector3f(0f, 0.7f, 3f),
                Quaternionf().identity(),
                Vector3f(1f, 1f, 1f)
            ))

            val cameraComponent = PerspectiveCameraComponent(vectorsPool, 45f, listOf("defaultLayer"))
            addComponent(cameraComponent)
            _activeCameras += cameraComponent
            _cameraAmbientLights[cameraComponent] = Vector3f(0.1f, 0.1f, 0.1f)
        }
        rootGameObject.addChild(cameraGameObject)

        spherePrefab = GameObject("sphere").apply {
            val meshName = "sphere"
            val mesh = meshLoadingRepository.loadMesh("meshes/sphere.obj").toMesh()
            geometryManager.createStaticVertexBuffer(meshName, mesh.verticesAsArray())
            geometryManager.createStaticIndexBuffer(meshName, mesh.indices.toShortArray())

            addComponent(MeshComponent(meshName))

            addComponent(TransformationComponent(Vector3f(), Quaternionf().identity(), Vector3f(0.05f, 0.05f, 0.05f)))

            val meshRenderer = MeshRendererComponent(
                displayMetricsRepository.getPixelDensityFactor(),
                listOf(DEFAULT_LAYER_NAME),
                texturesManager,
                geometryManager,
                openGLErrorDetector
            )
            addComponent(meshRenderer)

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

        debugGameObject = GameObject("debug").apply {
            debugGameObjectTransform = TransformationComponent(
                Vector3f(),
                Quaternionf().identity(),
                Vector3f(1f, 1f, 1f)
            )
            addComponent(debugGameObjectTransform)
        }
        rootGameObject.addChild(debugGameObject)

        val skeletalAnimation = animatedMeshRepository.loadAnimation("meshes/female_idle.dae")

        L.d(LOG_TAG, "Number of keyframes: ${skeletalAnimation.animation.keyFrames.size}")

        addJoint(
            Matrix4f().identity(),
            skeletalAnimation.rootJoint,
            skeletalAnimation.animation.keyFrames[0],
            spherePrefab
        )
    }

    private fun addJoint(
        /*parent: GameObject, */
        parentTransform: Matrix4fc,
        joint: Joint,
        keyFrame: KeyFrame,
        prefab: GameObject
    ) {
        val bindTransform = matrixPool.obtain()
        val currentTransform = matrixPool.obtain()
        val position = vectorsPool.obtain()
        val rotation = quaternionPool.obtain()

        val jointGameObject = prefab.copy()
        val gameObjectTransform = jointGameObject.getComponent(TransformationComponent::class.java)
            ?: error("Transform not found")

        currentTransform.set(parentTransform)
        currentTransform.mul(
            keyFrame.jointLocalTransforms[joint.name]?.transform ?: error("Joint ${joint.name} transform not found")
        )

        joint.children.forEach { childJoint -> addJoint(currentTransform, childJoint, keyFrame, prefab) }

        bindTransform.set(joint.invertedBindTransform)
        bindTransform.invert()
        currentTransform.mul(bindTransform)

        currentTransform.getTranslation(position)
        currentTransform.getNormalizedRotation(rotation)
        gameObjectTransform.position = position
        gameObjectTransform.rotation = rotation

        debugGameObject.addChild(jointGameObject)
        _layerRenderers.put(
            DEFAULT_LAYER_NAME,
            jointGameObject.getComponent(MeshRendererComponent::class.java)
                ?: error("Renderer component not found")
        )

        matrixPool.recycle(currentTransform)
        vectorsPool.recycle(position)
        quaternionPool.recycle(rotation)
        matrixPool.recycle(bindTransform)
    }

    override fun update() {
        val currentTimestamp = timeRepository.getTimestamp()
        val dt = prevTimestamp?.let { prevTimestamp -> (currentTimestamp - prevTimestamp) / NANOS_IN_SECOND } ?: 0f
        prevTimestamp = currentTimestamp

        val rotation = quaternionPool.obtain()

        debugGameObjectYAngle += ANGULAR_SPEED * dt
        rotation.identity().rotateY(debugGameObjectYAngle)
        debugGameObjectTransform.rotation = rotation

        quaternionPool.recycle(rotation)
    }

    override fun deinit() {
        // do nothing
    }

    companion object {

        private const val DEFAULT_LAYER_NAME = "defaultLayer"

        private const val ANGULAR_SPEED = 1f // rad/sec
    }
}