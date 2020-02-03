package ilapin.opengl_research.domain

import com.google.common.collect.Multimap
import ilapin.engine3d.GameObject
import ilapin.engine3d.GameObjectComponent
import ilapin.opengl_research.CameraComponent

/**
 * @author raynor on 03.02.20.
 */
interface Scene2 {

    val rootGameObject: GameObject

    val cameras: List<CameraComponent>

    val layerRenderers: Multimap<String, GameObjectComponent>

    fun update()
}