package ilapin.opengl_research

import ilapin.engine3d.TransformationComponent
import org.joml.Matrix4f
import org.joml.Vector3f
import org.joml.Vector3fc

class OrthoCameraComponent(
    private val vectorsPool: ObjectsPool<Vector3f>,
    var left: Float,
    var right: Float,
    var bottom: Float,
    var top: Float,
    layerNames: List<String>
) : CameraComponent(layerNames) {

    var zNear = DEFAULT_Z_NEAR
    var zFar = DEFAULT_Z_FAR

    fun calculateViewMatrix(dest: Matrix4f): Matrix4f {
        val transform = gameObject?.getComponent(TransformationComponent::class.java) ?: return dest

        val lookAtDirection = vectorsPool.obtain()
        val up = vectorsPool.obtain()

        lookAtDirection.set(DEFAULT_LOOK_AT_DIRECTION)
        lookAtDirection.rotate(transform.rotation)
        up.set(DEFAULT_CAMERA_UP_DIRECTION)
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

    fun calculateProjectionMatrix(dest: Matrix4f): Matrix4f {
        dest.identity().setOrtho(left, right, bottom, top, zNear, zFar)

        return dest
    }

    companion object {

        private val DEFAULT_LOOK_AT_DIRECTION: Vector3fc = Vector3f(0f, 0f, -1f)
        private val DEFAULT_CAMERA_UP_DIRECTION: Vector3fc = Vector3f(0f, 1f, 0f)

        private const val DEFAULT_Z_NEAR = 1f
        private const val DEFAULT_Z_FAR = 10f
    }
}