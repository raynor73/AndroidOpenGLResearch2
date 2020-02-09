package ilapin.opengl_research.domain.physics_engine

import org.joml.Vector3f
import org.joml.Vector3fc

/**
 * @author raynor on 09.02.20.
 */
sealed class CollisionShape {
    class Sphere(position: Vector3fc, val radius: Float) : CollisionShape() {
        val position: Vector3fc = Vector3f(position)
    }
    class Aabb(val minExtent: Vector3fc, val maxExtent: Vector3fc) : CollisionShape()
    class Plane() : CollisionShape()
}