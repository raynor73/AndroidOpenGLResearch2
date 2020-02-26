package ilapin.opengl_research.domain

import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import ilapin.common.time.TimeRepository
import ilapin.engine3d.GameObject
import ilapin.engine3d.GameObjectComponent
import ilapin.engine3d.TransformationComponent
import ilapin.opengl_research.FrameBufferInfo
import ilapin.opengl_research.NANOS_IN_SECOND
import ilapin.opengl_research.ObjectsPool
import ilapin.opengl_research.data.assets_management.FrameBuffersManager
import ilapin.opengl_research.data.assets_management.OpenGLGeometryManager
import ilapin.opengl_research.data.assets_management.OpenGLTexturesManager
import ilapin.opengl_research.data.scripting_engine.RhinoScriptingEngine
import ilapin.opengl_research.domain.engine.*
import ilapin.opengl_research.domain.physics_engine.PhysicsEngine
import ilapin.opengl_research.domain.scene_loader.SceneData
import ilapin.opengl_research.domain.sound.SoundClipsRepository
import ilapin.opengl_research.domain.sound.SoundScene
import ilapin.opengl_research.domain.sound_2d.SoundScene2D
import org.joml.Quaternionf
import org.joml.Vector3f
import org.joml.Vector3fc

/**
 * @author raynor on 18.02.20.
 */
class ScriptedScene(
    sceneData: SceneData,
    private val scriptingEngine: RhinoScriptingEngine,
    private val texturesManager: OpenGLTexturesManager,
    private val geometryManager: OpenGLGeometryManager,
    private val frameBuffersManager: FrameBuffersManager,
    private val meshStorage: MeshStorage,
    private val timeRepository: TimeRepository,
    private val touchEventsRepository: TouchEventsRepository,
    displayMetricsRepository: DisplayMetricsRepository,
    vectorsPool: ObjectsPool<Vector3f>,
    quaternionsPool: ObjectsPool<Quaternionf>,
    private val gesturesDispatcher: GesturesDispatcher,
    appPriorityReporter: AppPriorityReporter,
    private val soundScene: SoundScene,
    private val soundScene2d: SoundScene2D,
    private val soundClipsRepository: SoundClipsRepository,
    private val physicsEngine: PhysicsEngine
) : Scene2 {

    private val _activeCameras = ArrayList<CameraComponent>().apply { addAll(sceneData.activeCameras) }

    private val _layerRenderers = HashMultimap.create<String, MeshRendererComponent>().apply {
        putAll(sceneData.layerRenderers)
    }

    private val _layerLights = HashMultimap.create<String, GameObjectComponent>().apply {
        putAll(sceneData.layerLights)
    }

    private val _cameraAmbientLights = HashMap<CameraComponent, Vector3fc>().apply {
        putAll(sceneData.cameraAmbientLights)
    }

    private var prevTimestamp: Long? = null

    override val rootGameObject = sceneData.rootGameObject

    override val activeCameras: List<CameraComponent> = _activeCameras

    override val layerRenderers: Multimap<String, MeshRendererComponent> = _layerRenderers

    override val layerLights: Multimap<String, GameObjectComponent> = _layerLights

    override val cameraAmbientLights: Map<CameraComponent, Vector3fc> = _cameraAmbientLights

    override val renderTargets: List<FrameBufferInfo.RenderTargetFrameBufferInfo> = emptyList()

    init {
        scriptingEngine.appPriorityReporter = appPriorityReporter
        scriptingEngine.touchEventsRepository = touchEventsRepository
        scriptingEngine.scene = this
        scriptingEngine.displayMetricsRepository = displayMetricsRepository
        scriptingEngine.vectorsPool = vectorsPool
        scriptingEngine.quaternionsPool = quaternionsPool
        scriptingEngine.soundClipsRepository = soundClipsRepository
        scriptingEngine.soundScene = soundScene
        scriptingEngine.soundScene2D = soundScene2d
        scriptingEngine.loadScripts(sceneData.scriptSources)
    }

    override fun update() {
        val currentTimestamp = timeRepository.getTimestamp()
        val dt = prevTimestamp?.let { prevTimestamp -> (currentTimestamp - prevTimestamp) / NANOS_IN_SECOND } ?: 0f
        prevTimestamp = currentTimestamp

        gesturesDispatcher.begin()
        touchEventsRepository.touchEvents.forEach { gesturesDispatcher.onTouchEvent(it) }

        physicsEngine.update(dt)
        rootGameObject.update()
        soundScene.update()
        soundScene2d.update()
        scriptingEngine.update(dt)
    }

    override fun deinit() {
        meshStorage.removeAllMeshes()
        frameBuffersManager.removeAllFrameBuffers()
        geometryManager.removeAllBuffers()
        texturesManager.removeAllTextures()
        soundScene.clear()
        soundScene2d.clear()
        soundClipsRepository.clear()
        physicsEngine.clear()
    }

    @Suppress("unused")
    fun getTransformationComponent(gameObject: GameObject): TransformationComponent? {
        return gameObject.getComponent(TransformationComponent::class.java)
    }

    @Suppress("unused")
    fun getOrthoCameraComponent(gameObject: GameObject): OrthoCameraComponent? {
        return gameObject.getComponent(OrthoCameraComponent::class.java)
    }

    @Suppress("unused")
    fun getGestureConsumerComponent(gameObject: GameObject): GestureConsumerComponent? {
        return gameObject.getComponent(GestureConsumerComponent::class.java)
    }

    @Suppress("unused")
    fun getSoundPlayer3DComponent(gameObject: GameObject): SoundPlayer3DComponent? {
        return gameObject.getComponent(SoundPlayer3DComponent::class.java)
    }

    @Suppress("unused")
    fun getSoundPlayer2DComponent(gameObject: GameObject): SoundPlayer2DComponent? {
        return gameObject.getComponent(SoundPlayer2DComponent::class.java)
    }

    @Suppress("unused")
    fun getSoundListenerComponent(gameObject: GameObject): SoundListenerComponent? {
        return gameObject.getComponent(SoundListenerComponent::class.java)
    }
}