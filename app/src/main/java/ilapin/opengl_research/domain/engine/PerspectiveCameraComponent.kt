package ilapin.opengl_research.domain.engine

import ilapin.engine3d.GameObjectComponent
import ilapin.engine3d.TransformationComponent
import ilapin.opengl_research.*
import org.joml.Matrix4f
import org.joml.Vector3f

/**
 * @author raynor on 27.01.20.
 */
class PerspectiveCameraComponent(
    private val vectorsPool: ObjectsPool<Vector3f>,
    var fov: Float,
    layerNames: List<String>
) : CameraComponent(layerNames) {

    fun calculateViewMatrix(dest: Matrix4f): Matrix4f {
        val transform = gameObject?.getComponent(TransformationComponent::class.java) ?: return dest

        val lookAtDirection = vectorsPool.obtain()
        val up = vectorsPool.obtain()

        lookAtDirection.set(CAMERA_LOOK_AT_DIRECTION)
        lookAtDirection.rotate(transform.rotation)
        up.set(CAMERA_UP_DIRECTION)
        up.rotate(transform.rotation)

        val position = transform.position
        dest.identity().setLookAt(
            position.x(),
            position.y(),
            position.z(),
            position.x() + lookAtDirection.x,
            position.y() + lookAtDirection.y,
            position.z() + lookAtDirection.z,
            up.x,
            up.y,
            up.z
        )

        vectorsPool.recycle(lookAtDirection)
        vectorsPool.recycle(up)

        return dest
    }

    fun calculateProjectionMatrix(aspect: Float, dest: Matrix4f): Matrix4f {
        dest.identity().setPerspective(fov, aspect, zNear, zFar)

        return dest
    }

    override fun copy(): GameObjectComponent {
        return PerspectiveCameraComponent(vectorsPool, fov, layerNames)
    }
}