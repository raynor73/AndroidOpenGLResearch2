package ilapin.opengl_research.domain.scene_loader

import com.google.common.collect.Multimap
import ilapin.engine3d.GameObject
import ilapin.engine3d.GameObjectComponent
import ilapin.opengl_research.domain.engine.CameraComponent
import ilapin.opengl_research.FrameBufferInfo
import ilapin.opengl_research.domain.engine.MeshRendererComponent
import org.joml.Vector3fc

/**
 * @author raynor on 21.01.20.
 */
class SceneData(
    val scriptSources: List<String>,
    val rootGameObject: GameObject,
    val activeCameras: List<CameraComponent>,
    val layerRenderers: Multimap<String, MeshRendererComponent>,
    val lights: List<GameObjectComponent>,
    val cameraAmbientLights: Map<CameraComponent, Vector3fc>,
    val renderTargets: List<FrameBufferInfo.RenderTargetFrameBufferInfo>
)