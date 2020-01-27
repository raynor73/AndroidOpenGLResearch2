package ilapin.opengl_research

import ilapin.engine3d.GameObjectComponent
import ilapin.engine3d.TransformationComponent
import org.joml.Matrix4f
import org.joml.Matrix4fc
import org.joml.Vector3f
import org.joml.Vector3fc

/**
 * @author raynor on 27.01.20.
 */
class PerspectiveCameraComponent(
    private val vectorsPool: ObjectsPool<Vector3f>
) : GameObjectComponent() {

    var fov = DEFAULT_FIELD_OF_VIEW
    var zNear = DEFAULT_Z_NEAR
    var zFar = DEFAULT_Z_FAR

    private fun calculateViewMatrix(dest: Matrix4f): Matrix4f {
        val transform = gameObject?.getComponent(TransformationComponent::class.java) ?: return dest

        val lookAtDirection = vectorsPool.obtain()
        val up = vectorsPool.obtain()

        lookAtDirection.set(DEFAULT_LOOK_AT_DIRECTION)
        lookAtDirection.rotate(transform.rotation)
        up.set(DEFAULT_CAMERA_UP_DIRECTION)
        up.rotate(transform.rotation)

        val position = transform.position
        dest.setLookAt(
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

    private fun calculateProjectionMatrix(aspect: Float, dest: Matrix4f): Matrix4f {
        dest.setPerspective(fov, aspect, zNear, zFar)

        return dest
    }


    companion object {

        private val DEFAULT_LOOK_AT_DIRECTION: Vector3fc = Vector3f(0f, 0f, -1f)
        private val DEFAULT_CAMERA_UP_DIRECTION: Vector3fc = Vector3f(0f, 1f, 0f)

        private const val DEFAULT_FIELD_OF_VIEW = 45f
        private const val DEFAULT_Z_NEAR = 1f
        private const val DEFAULT_Z_FAR = 10f
    }
}