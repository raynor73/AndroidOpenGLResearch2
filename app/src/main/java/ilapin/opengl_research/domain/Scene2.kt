package ilapin.opengl_research.domain

import com.google.common.collect.Multimap
import ilapin.engine3d.GameObject
import ilapin.opengl_research.CameraComponent
import ilapin.opengl_research.RendererComponent

/**
 * @author raynor on 03.02.20.
 */
interface Scene2 {

    val rootGameObject: GameObject

    val cameras: List<CameraComponent>

    val shadowMapCameras: List<CameraComponent>

    val layerRenderers: Multimap<String, RendererComponent>

    fun update()
}