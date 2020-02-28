package ilapin.opengl_research.domain.engine

import ilapin.engine3d.GameObjectComponent

/**
 * @author raynor on 28.01.20.
 */
class MeshComponent(val name: String) : GameObjectComponent() {

    override fun copy(): GameObjectComponent {
        return MeshComponent(name)
    }
}