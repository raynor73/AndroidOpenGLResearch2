package ilapin.opengl_research.domain.engine

import ilapin.engine3d.GameObjectComponent
import ilapin.engine3d.TransformationComponent
import ilapin.opengl_research.toVector
import org.joml.Matrix4x3f
import org.joml.Quaternionf
import org.joml.Vector3f

/**
 * @author raynor on 08.02.20.
 */
class RigidBodyGameObjectComponent(val rigidBody: DBody) : GameObjectComponent() {

    private val rotationMatrix = Matrix4x3f()
    private val rotationQuaternion = Quaternionf()
    private val column = DVector3()

    private val tmpVector = Vector3f()

    override fun update() {
        super.update()

        val transform = gameObject?.getComponent(TransformationComponent::class.java)
            ?: error("No transform found for game object ${gameObject?.name}")

        val rotationDMatrix3 = rigidBody.rotation

        rotationDMatrix3.getColumn0(column)
        column.toVector(tmpVector)
        rotationMatrix.setColumn(0, tmpVector)

        rotationDMatrix3.getColumn1(column)
        column.toVector(tmpVector)
        rotationMatrix.setColumn(1, tmpVector)

        rotationDMatrix3.getColumn2(column)
        column.toVector(tmpVector)
        rotationMatrix.setColumn(2, tmpVector)

        rotationQuaternion.setFromUnnormalized(rotationMatrix)

        rigidBody.position.toVector(tmpVector)
        transform.position = tmpVector
        transform.rotation = rotationQuaternion
    }
}