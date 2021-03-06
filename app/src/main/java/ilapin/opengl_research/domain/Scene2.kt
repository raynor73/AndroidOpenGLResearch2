package ilapin.opengl_research.domain

import com.google.common.collect.Multimap
import ilapin.engine3d.GameObject
import ilapin.engine3d.GameObjectComponent
import ilapin.opengl_research.domain.engine.CameraComponent
import ilapin.opengl_research.FrameBufferInfo
import ilapin.opengl_research.data.engine.MeshRendererComponent
import org.joml.Vector3fc

/**
 * @author raynor on 03.02.20.
 */
interface Scene2 {

    val rootGameObject: GameObject

    val activeCameras: List<CameraComponent>

    val layerRenderers: Multimap<String, MeshRendererComponent>

    val layerLights: Multimap<String, GameObjectComponent>

    val cameraAmbientLights: Map<CameraComponent, Vector3fc>

    val renderTargets: List<FrameBufferInfo.RenderTargetFrameBufferInfo>

    fun update()

    fun deinit()
}