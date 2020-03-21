package ilapin.opengl_research.domain.physics_engine

import ilapin.engine3d.GameObject
import ilapin.engine3d.GameObjectComponent
import org.joml.Vector3f
import org.joml.Vector3fc
import java.util.ArrayList

/**
 * @author Игорь on 16.03.2020.
 */
class CollisionsInfoComponent : GameObjectComponent() {

    val collisions: MutableList<Collision> = ArrayList()

    override fun copy(): GameObjectComponent {
        return CollisionsInfoComponent()
    }

    class Collision(
        val collisionWith: GameObject,
        position: Vector3fc,
        normal: Vector3fc,
        val depth: Float
    ) {
        val position: Vector3fc = Vector3f(position)
        val normal: Vector3fc = Vector3f(normal)
    }
}