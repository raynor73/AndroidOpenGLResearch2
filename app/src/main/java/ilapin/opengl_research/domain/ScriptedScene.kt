package ilapin.opengl_research.domain

import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import ilapin.common.time.TimeRepository
import ilapin.engine3d.GameObject
import ilapin.engine3d.GameObjectComponent
import ilapin.engine3d.TransformationComponent
import ilapin.opengl_research.*
import ilapin.opengl_research.data.assets_management.FrameBuffersManager
import ilapin.opengl_research.data.assets_management.OpenGLGeometryManager
import ilapin.opengl_research.data.assets_management.OpenGLTexturesManager
import ilapin.opengl_research.data.scripting_engine.RhinoScriptingEngine
import ilapin.opengl_research.domain.scene_loader.SceneData
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
    private val gesturesDispatcher: GesturesDispatcher
) : Scene2 {

    private val _activeCameras = ArrayList<CameraComponent>().apply { addAll(sceneData.activeCameras) }

    private val _layerRenderers = HashMultimap.create<String, MeshRendererComponent>().apply {
        putAll(sceneData.layerRenderers)
    }

    private val _lights = ArrayList<GameObjectComponent>().apply { addAll(sceneData.lights) }

    private val _cameraAmbientLights = HashMap<CameraComponent, Vector3fc>().apply {
        putAll(sceneData.cameraAmbientLights)
    }

    private var prevTimestamp: Long? = null

    override val rootGameObject = sceneData.rootGameObject

    override val activeCameras: List<CameraComponent> = _activeCameras

    override val layerRenderers: Multimap<String, MeshRendererComponent> = _layerRenderers

    override val lights: List<GameObjectComponent> = _lights

    override val cameraAmbientLights: Map<CameraComponent, Vector3fc> = _cameraAmbientLights

    override val renderTargets: List<FrameBufferInfo.RenderTargetFrameBufferInfo> = emptyList()

    init {
        scriptingEngine.touchEventsRepository = touchEventsRepository
        scriptingEngine.scene = this
        scriptingEngine.displayMetricsRepository = displayMetricsRepository
        scriptingEngine.vectorsPool = vectorsPool
        scriptingEngine.quaternionsPool = quaternionsPool
        scriptingEngine.evaluateScript(sceneData.scriptSource)
    }

    override fun update() {
        val currentTimestamp = timeRepository.getTimestamp()
        val dt = prevTimestamp?.let { prevTimestamp -> (currentTimestamp - prevTimestamp) / NANOS_IN_SECOND } ?: 0f
        prevTimestamp = currentTimestamp

        gesturesDispatcher.begin()
        touchEventsRepository.touchEvents.forEach { gesturesDispatcher.onTouchEvent(it) }

        scriptingEngine.update(dt)
    }

    // TODO Make another facility for onGoingToForeground/onGoingToBackground
    override fun onGoingToForeground() {
        scriptingEngine.onGoingToForeground()
    }

    override fun onGoingToBackground() {
        scriptingEngine.onGoingToBackground()
    }

    override fun deinit() {
        meshStorage.removeAllMeshes()
        frameBuffersManager.removeAllFrameBuffers()
        geometryManager.removeAllBuffers()
        texturesManager.removeAllTextures()
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
}