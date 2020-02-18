package ilapin.opengl_research.domain

import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import ilapin.engine3d.GameObjectComponent
import ilapin.opengl_research.CameraComponent
import ilapin.opengl_research.FrameBufferInfo
import ilapin.opengl_research.MeshRendererComponent
import ilapin.opengl_research.data.assets_management.FrameBuffersManager
import ilapin.opengl_research.data.assets_management.OpenGLGeometryManager
import ilapin.opengl_research.data.assets_management.OpenGLTexturesManager
import ilapin.opengl_research.domain.scene_loader.SceneData
import org.joml.Vector3fc

/**
 * @author raynor on 18.02.20.
 */
class ScriptedScene(
    sceneData: SceneData,
    private val texturesManager: OpenGLTexturesManager,
    private val geometryManager: OpenGLGeometryManager,
    private val frameBuffersManager: FrameBuffersManager,
    private val meshStorage: MeshStorage
) : Scene2 {

    private val _activeCameras = ArrayList<CameraComponent>().apply { addAll(sceneData.activeCameras) }

    private val _layerRenderers = HashMultimap.create<String, MeshRendererComponent>().apply {
        putAll(sceneData.layerRenderers)
    }

    private val _lights = ArrayList<GameObjectComponent>().apply { addAll(sceneData.lights) }

    private val _cameraAmbientLights = HashMap<CameraComponent, Vector3fc>().apply {
        putAll(sceneData.cameraAmbientLights)
    }

    override val rootGameObject = sceneData.rootGameObject

    override val activeCameras: List<CameraComponent> = _activeCameras

    override val layerRenderers: Multimap<String, MeshRendererComponent> = _layerRenderers

    override val lights: List<GameObjectComponent> = _lights

    override val cameraAmbientLights: Map<CameraComponent, Vector3fc> = _cameraAmbientLights

    override val renderTargets: List<FrameBufferInfo.RenderTargetFrameBufferInfo> = emptyList()

    override fun update() {
        // do nothing
    }

    override fun onGoingToForeground() {
        // do nothing
    }

    override fun onGoingToBackground() {
        // do nothing
    }

    override fun deinit() {
        meshStorage.removeAllMeshes()
        frameBuffersManager.removeAllFrameBuffers()
        geometryManager.removeAllBuffers()
        texturesManager.removeAllTextures()
    }
}