package ilapin.opengl_research.domain

import com.google.common.collect.Multimap
import ilapin.engine3d.GameObject
import ilapin.engine3d.GameObjectComponent
import ilapin.opengl_research.CameraComponent
import ilapin.opengl_research.RendererComponent
import ilapin.opengl_research.ShadowMapRendererComponent

/**
 * @author raynor on 03.02.20.
 */
interface Scene2 {

    val rootGameObject: GameObject

    val cameras: List<CameraComponent>

    val layerRenderers: Multimap<String, RendererComponent>

    val shadowMapCameras: List<CameraComponent>

    val shadowLayerRenderers: Multimap<String, ShadowMapRendererComponent>

    val lights: List<GameObjectComponent>

    fun update()
}