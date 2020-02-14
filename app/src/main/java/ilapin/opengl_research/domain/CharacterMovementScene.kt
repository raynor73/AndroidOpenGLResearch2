package ilapin.opengl_research.domain

import android.content.Context
import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import ilapin.collada_parser.collada_loader.ColladaLoader
import ilapin.common.time.TimeRepository
import ilapin.engine3d.GameObject
import ilapin.engine3d.GameObjectComponent
import ilapin.engine3d.TransformationComponent
import ilapin.meshloader.MeshLoadingRepository
import ilapin.opengl_research.*
import ilapin.opengl_research.domain.physics_engine.PhysicsEngine
import ilapin.opengl_research.domain.skeletal_animation.SkeletalAnimatorComponent
import ilapin.opengl_research.domain.sound.SoundScene
import org.joml.Quaternionf
import org.joml.Vector3f
import org.joml.Vector3fc
import org.joml.Vector4f
import kotlin.math.PI
import kotlin.math.abs

/**
 * @author raynor on 07.02.20.
 */
class CharacterMovementScene(
    private val context: Context,
    private val openGLObjectsRepository: OpenGLObjectsRepository,
    private val openGLErrorDetector: OpenGLErrorDetector,
    private val soundScene: SoundScene,
    private val vectorsPool: ObjectsPool<Vector3f>,
    private val quaternionsPool: ObjectsPool<Quaternionf>,
    private val timeRepository: TimeRepository,
    private val meshLoadingRepository: MeshLoadingRepository,
    displayMetricsRepository: DisplayMetricsRepository,
    private val scrollController: ScrollController,
    private val playerController: PlayerController
) : Scene2 {

    private val _cameras = ArrayList<CameraComponent>()

    private val _layerRenderers = HashMultimap.create<String, MeshRendererComponent>()

    private val _lights = ArrayList<GameObjectComponent>()

    private val _cameraAmbientLights = HashMap<CameraComponent, Vector3fc>()

    private val physicsEngine = PhysicsEngine()

    override val rootGameObject = GameObject("root").apply {
        addComponent(TransformationComponent(Vector3f(), Quaternionf().identity(), Vector3f(1f, 1f, 1f)))
    }

    override val cameras: List<CameraComponent> = _cameras

    override val layerRenderers: Multimap<String, MeshRendererComponent> = _layerRenderers

    override val lights: List<GameObjectComponent> = _lights

    override val cameraAmbientLights: Map<CameraComponent, Vector3fc> = _cameraAmbientLights

    override val renderTargets: List<FrameBufferInfo.RenderTargetFrameBufferInfo> = emptyList()

    private var prevTimestamp: Long? = null

    private val pixelDensityFactor = displayMetricsRepository.getPixelDensityFactor()
    private var xAngle = -(PI / 2).toFloat()
    private var zAngle = -(PI / 4).toFloat()

    private lateinit var directionalLightTransform: TransformationComponent

    private lateinit var player: GameObject
    private lateinit var playerTransform: TransformationComponent
    private var playerYAngle = 0f

    init {
        setupTextures()
        setupPhysics()
        setupGeometry()
        setupCameras()
        setupLights()
        setupSounds()
    }

    override fun update() {
        val currentTimestamp = timeRepository.getTimestamp()
        val dt = prevTimestamp?.let { prevTimestamp -> (currentTimestamp - prevTimestamp) / NANOS_IN_SECOND } ?: 0f
        prevTimestamp = currentTimestamp

        physicsEngine.update(dt)
        rootGameObject.update()

        scrollController.scrollEvent?.let { scrollEvent ->
            zAngle -= Math.toRadians((scrollEvent.dx / pixelDensityFactor).toDouble()).toFloat()
            xAngle -=  Math.toRadians((scrollEvent.dy / pixelDensityFactor).toDouble()).toFloat()

            val lightRotation = quaternionsPool.obtain()

            lightRotation.identity()
            lightRotation.rotateZ(zAngle).rotateX(xAngle)
            directionalLightTransform.rotation = lightRotation

            quaternionsPool.recycle(lightRotation)
        }

        val playerPosition = vectorsPool.obtain()
        val movingDirection = vectorsPool.obtain()
        val strafingDirection = vectorsPool.obtain()

        movingDirection.set(INITIAL_FORWARD_VECTOR)
        movingDirection.rotate(playerTransform.rotation)
        strafingDirection.set(INITIAL_RIGHT_VECTOR)
        strafingDirection.rotate(playerTransform.rotation)

        playerPosition.set(playerTransform.position)
        playerPosition.add(movingDirection.mul(PLAYER_MOVEMENT_SPEED).mul(playerController.movingFraction).mul(dt))
        playerPosition.add(strafingDirection.mul(PLAYER_MOVEMENT_SPEED).mul(playerController.strafingFraction).mul(dt))
        playerTransform.position = playerPosition

        val playerRotation = quaternionsPool.obtain()
        playerYAngle += playerController.horizontalSteeringFraction * PLAYER_STEERING_SPEED * dt
        playerRotation.identity().rotateY(playerYAngle)
        playerTransform.rotation = playerRotation

        if (
            abs(playerController.movingFraction) > 0.01 ||
            abs(playerController.strafingFraction) > 0.01 ||
            abs(playerController.horizontalSteeringFraction) > 0.01
        ) {
            soundScene.updateSoundListenerPosition(playerTransform.position)
            soundScene.updateSoundListenerRotation(playerTransform.rotation)
        }

        vectorsPool.recycle(playerPosition)
        vectorsPool.recycle(movingDirection)
        vectorsPool.recycle(strafingDirection)

        quaternionsPool.recycle(playerRotation)
    }

    private fun setupPhysics() {
        // do nothing
    }

    private fun setupTextures() {
        openGLObjectsRepository.createTexture("green", 1, 1, intArrayOf(0xff008000.toInt()))
        openGLObjectsRepository.createTexture("blue", 1, 1, intArrayOf(0xff000080.toInt()))
        openGLObjectsRepository.loadTexture("female", "textures/female.png")
        openGLObjectsRepository.loadTexture("fountain", "textures/fountain.png")
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
                Vector3f(0f, 0f, 0f),
                Quaternionf().identity().rotateX(-(PI / 2).toFloat()),
                Vector3f(50f, 50f, 1f)
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
            gameObject.addComponent(CollisionShapeGameObjectComponent(
                physicsEngine.createTriMeshCollisionShape(mesh.applyTransform(
                    Vector3f(0f, 0f, 0f),
                    Quaternionf().identity().rotateX(-(PI / 2).toFloat()),
                    Vector3f(50f, 50f, 1f)
                ))
            ))
            rootGameObject.addChild(gameObject)
        }

        run {
            val gameObject = GameObject("floating_plane")
            gameObject.addComponent(TransformationComponent(
                Vector3f(0f, 2f, 0f),
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
            val playerMesh = meshLoadingRepository.loadMesh("meshes/female.obj").toMesh()
            val playerMeshVbo = openGLObjectsRepository.createStaticVbo("player", playerMesh.verticesAsArray())
            val playerMeshIboInfo = IboInfo(
                openGLObjectsRepository.createStaticIbo("player", playerMesh.indices.toShortArray()),
                playerMesh.indices.size
            )

            player = GameObject("player")
            playerTransform = TransformationComponent(
                Vector3f(0f, 0f, 0f),
                Quaternionf().identity(),
                Vector3f(1f, 1f, 1f)
            )
            player.addComponent(playerTransform)
            val renderer = MeshRendererComponent(
                pixelDensityFactor,
                openGLObjectsRepository,
                openGLErrorDetector
            )
            layerRenderers[DEFAULT_LAYER_NAME] += renderer
            player.addComponent(renderer)
            player.addComponent(MaterialComponent("female", Vector4f(1f, 1f, 1f, 1f)))
            player.addComponent(MeshComponent(playerMeshVbo, playerMeshIboInfo))
            rootGameObject.addChild(player)
        }

        run {
            val gameObject = GameObject("fountain")

            val fountainMesh = meshLoadingRepository.loadMesh("meshes/fountain.obj").toMesh()
            val fountainMeshVbo = openGLObjectsRepository.createStaticVbo("fountain", fountainMesh.verticesAsArray())
            val fountainMeshIboInfo = IboInfo(
                openGLObjectsRepository.createStaticIbo("fountain", fountainMesh.indices.toShortArray()),
                fountainMesh.indices.size
            )

            gameObject.addComponent(TransformationComponent(
                Vector3f(0f, 0.01f, -10f),
                Quaternionf().identity(),
                Vector3f(1f, 1f, 1f)
            ))
            val renderer = MeshRendererComponent(
                pixelDensityFactor,
                openGLObjectsRepository,
                openGLErrorDetector
            )
            layerRenderers[DEFAULT_LAYER_NAME] += renderer
            gameObject.addComponent(renderer)
            gameObject.addComponent(MaterialComponent("fountain", Vector4f(1f, 1f, 1f, 1f)))
            gameObject.addComponent(MeshComponent(fountainMeshVbo, fountainMeshIboInfo))
            rootGameObject.addChild(gameObject)
        }

        run {
            val gameObject = GameObject("capsule")

            val capsuleMesh = meshLoadingRepository.loadMesh("meshes/capsule.obj").toMesh()
            val capsuleMeshVbo = openGLObjectsRepository.createStaticVbo("capsule", capsuleMesh.verticesAsArray())
            val capsuleMeshIboInfo = IboInfo(
                openGLObjectsRepository.createStaticIbo("capsule", capsuleMesh.indices.toShortArray()),
                capsuleMesh.indices.size
            )

            gameObject.addComponent(TransformationComponent(
                Vector3f(0f, 0f, 0f),
                Quaternionf().identity(),
                Vector3f(1f, 1f, 1f)
            ))
            val renderer = MeshRendererComponent(
                pixelDensityFactor,
                openGLObjectsRepository,
                openGLErrorDetector
            )
            layerRenderers[DEFAULT_LAYER_NAME] += renderer
            gameObject.addComponent(renderer)
            gameObject.addComponent(MaterialComponent(null, Vector4f(.5f, .5f, 0f, 1f)))
            gameObject.addComponent(MeshComponent(capsuleMeshVbo, capsuleMeshIboInfo))
            gameObject.addComponent(RigidBodyGameObjectComponent(physicsEngine.createCharacterCapsuleRigidBody(
                1f,
                1f,
                2f,
                Vector3f(0f, 4f, -5f)
            )))
            rootGameObject.addChild(gameObject)
        }

        run {
            val animatedModel = ColladaLoader.loadColladaModel(
                context.assets.open("meshes/cowboy.dae"),
                NUMBER_OF_JOINT_WEIGHTS
            )

            val gameObject = GameObject("cowboy")

            val cowboyMesh = animatedModel.meshData.toMesh()
            val cowboyMeshVbo = openGLObjectsRepository.createStaticVbo("cowboy", cowboyMesh.verticesAsArray())
            val cowboyMeshIboInfo = IboInfo(
                openGLObjectsRepository.createStaticIbo("cowboy", cowboyMesh.indices.toShortArray()),
                cowboyMesh.indices.size
            )

            gameObject.addComponent(TransformationComponent(
                Vector3f(5f, 0f, -5f),
                Quaternionf().identity(),
                Vector3f(1f, 1f, 1f)
            ))
            val renderer = MeshRendererComponent(
                pixelDensityFactor,
                openGLObjectsRepository,
                openGLErrorDetector
            )
            layerRenderers[DEFAULT_LAYER_NAME] += renderer
            gameObject.addComponent(renderer)
            gameObject.addComponent(MaterialComponent(null, Vector4f(.5f, 0f, .5f, 1f)))
            gameObject.addComponent(MeshComponent(cowboyMeshVbo, cowboyMeshIboInfo))
            gameObject.addComponent(SkeletalAnimatorComponent(timeRepository))
            rootGameObject.addChild(gameObject)
        }
    }

    private fun setupLights() {
        run {
            val gameObject = GameObject("directionalLight")
            val lightComponent = DirectionalLightComponent(Vector3f(0.7f, 0.7f, 0.7f))
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
            val gameObject = GameObject("player_camera")
            gameObject.addComponent(TransformationComponent(
                Vector3f(0f, 1f, 3f),
                Quaternionf().identity(),
                Vector3f(1f, 1f, 1f)
            ))
            val cameraComponent = PerspectiveCameraComponent(
                vectorsPool,
                45f,
                listOf(DEFAULT_LAYER_NAME)
            )
            gameObject.addComponent(cameraComponent)
            player.addChild(gameObject)
            _cameras += cameraComponent

            _cameraAmbientLights[cameraComponent] = Vector3f(0.5f, 0.5f, 0.5f)
        }
    }

    private fun setupSounds() {
        soundScene.loadSoundClip("water_flow", "sounds/water_flow.wav")
        soundScene.addSoundPlayer("fountain", "water_flow", 7791, Vector3f(0f, 0.5f, -10f), 1f, 15f)
        soundScene.updateSoundListenerPosition(playerTransform.position)
        soundScene.updateSoundListenerRotation(playerTransform.rotation)

        soundScene.startSoundPlayer("fountain", true)
    }


    companion object {

        private const val DEFAULT_LAYER_NAME = "defaultLayer"

        private val INITIAL_FORWARD_VECTOR: Vector3fc = Vector3f(0f, 0f, -1f)
        private val INITIAL_RIGHT_VECTOR: Vector3fc = Vector3f(1f, 0f, 0f)
        private const val PLAYER_MOVEMENT_SPEED = 5f // unit/sec
        private const val PLAYER_STEERING_SPEED = PI.toFloat() // rad/sec
    }
}