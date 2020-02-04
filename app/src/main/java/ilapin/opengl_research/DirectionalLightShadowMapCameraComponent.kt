package ilapin.opengl_research

import ilapin.engine3d.TransformationComponent
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
    top: Float,
    layerNames: List<String>
) : OrthoCameraComponent(vectorsPool, left, right, bottom, top, layerNames) {

    fun calculateViewMatrix(viewerPosition: Vector3fc, dest: Matrix4f): Matrix4f {
        val correctedPosition = vectorsPool.obtain()
        val transform = gameObject?.getComponent(TransformationComponent::class.java)!!

        correctedPosition.set(0f, 0f, -1f)
        correctedPosition.rotate(transform.rotation)
        correctedPosition.mul(-distanceFromViewer)
        correctedPosition.add(viewerPosition)

        transform.position = correctedPosition

        vectorsPool.recycle(correctedPosition)
        return calculateViewMatrix(dest)
    }
}