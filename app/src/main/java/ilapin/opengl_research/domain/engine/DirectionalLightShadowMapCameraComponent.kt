package ilapin.opengl_research.domain.engine

import ilapin.engine3d.TransformationComponent
import ilapin.opengl_research.CAMERA_LOOK_AT_DIRECTION
import ilapin.opengl_research.ObjectsPool
import org.joml.Matrix4f
import org.joml.Vector3f
import org.joml.Vector3fc

/**
 * @author raynor on 04.02.20.
 */
class DirectionalLightShadowMapCameraComponent(
    vectorsPool: ObjectsPool<Vector3f>,
    var distanceFromViewer: Float,
    left: Float,
    right: Float,
    bottom: Float,
    top: Float
) : OrthoCameraComponent(vectorsPool, left, right, bottom, top, emptyList()) {

    fun calculateViewMatrix(viewerPosition: Vector3fc, dest: Matrix4f): Matrix4f {
        val correctedPosition = vectorsPool.obtain()
        val transform = gameObject?.getComponent(TransformationComponent::class.java)!!

        correctedPosition.set(CAMERA_LOOK_AT_DIRECTION)
        correctedPosition.rotate(transform.rotation)
        correctedPosition.mul(-distanceFromViewer)
        correctedPosition.add(viewerPosition)

        transform.position = correctedPosition

        vectorsPool.recycle(correctedPosition)
        return calculateViewMatrix(dest)
    }
}