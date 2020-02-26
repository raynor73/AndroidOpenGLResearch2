package ilapin.opengl_research.domain.physics_engine

import ilapin.opengl_research.domain.Mesh
import ilapin.opengl_research.toQuaternion
import ilapin.opengl_research.toVector
import ilapin.opengl_research.vertexCoordinatesOnlyAsArray
import org.joml.Matrix4x3f
import org.joml.Quaternionf
import org.joml.Vector3f
import org.joml.Vector3fc
import org.ode4j.math.DQuaternion
import org.ode4j.math.DVector3
import org.ode4j.ode.*
import org.ode4j.ode.OdeConstants.dContactBounce
import org.ode4j.ode.OdeConstants.dContactSoftCFM
import kotlin.math.PI
import kotlin.math.ceil

/**
 * @author raynor on 08.02.20.
 */
class PhysicsEngine : DGeom.DNearCallback {

    private var world: DWorld? = null
    private var space: DSpace? = null

    private val tmpQuaternion = Quaternionf()
    private val tmpDQuaternion = DQuaternion()

    private val contactGroup: DJointGroup

    private val characterCapsules = HashMap<String, DBody>()
    private val triMeshes = HashMap<String, DTriMesh>()

    private val tmpVector = Vector3f()
    private val tmpColumn = DVector3()

    init {
        OdeHelper.initODE2(0)
        contactGroup = OdeHelper.createJointGroup()
    }

    fun setGravity(gravity: Vector3fc) {
        world?.setGravity(gravity.toVector())
    }

    fun createCharacterCapsuleRigidBody(
        name: String,
        massValue: Float,
        radius: Float,
        length: Float,
        position: Vector3fc
    ) {
        if (characterCapsules.containsKey(name)) {
            error("Already has $name Character Capsule")
        }

        val mass = OdeHelper.createMass()

        val rigidBody = OdeHelper.createBody(world)

        mass.setCapsuleTotal(massValue.toDouble(), 2, radius.toDouble(), length.toDouble())
        rigidBody.mass = mass

        val collisionShape = OdeHelper.createCapsule(null, radius.toDouble(), length.toDouble())
        collisionShape.body = rigidBody

        rigidBody.position = position.toVector()
        rigidBody.quaternion = Quaternionf().identity().rotateX(-(PI / 2).toFloat()).toQuaternion()
        rigidBody.maxAngularSpeed = .0
        rigidBody.setLinearVel(0.0, 0.0, 0.0)
        rigidBody.setAngularVel(0.0, 0.0, 0.0)

        space?.add(collisionShape)

        characterCapsules[name] = rigidBody
    }

    fun createTriMeshCollisionShape(name: String, mesh: Mesh) {
        val triMeshData = OdeHelper.createTriMeshData()

        triMeshData.build(mesh.vertexCoordinatesOnlyAsArray(), mesh.indices.map { it.toInt() }.toIntArray())
        triMeshData.preprocess()

        triMeshes[name] = OdeHelper.createTriMesh(space, triMeshData, null, null, null)
    }

    fun update(dt: Float) {
        repeat(ceil(dt / SIMULATION_STEP_TIME).toInt()) {
            OdeHelper.spaceCollide(space, null, this)
            world?.step(SIMULATION_STEP_TIME)
            characterCapsules.values.forEach {
                tmpQuaternion.identity().rotateX(-(PI / 2).toFloat()).toQuaternion(tmpDQuaternion)
                it.quaternion = tmpDQuaternion
            }
            contactGroup.empty()
        }
    }

    fun getRigidBodyRotationAndPosition(rigidBodyName: String, rotationMatrix: Matrix4x3f, position: Vector3f) {
        val rigidBody = characterCapsules[rigidBodyName] ?: error("Rigid body $rigidBodyName not found")

        val rotationDMatrix3 = rigidBody.rotation

        rotationDMatrix3.getColumn0(tmpColumn)
        tmpColumn.toVector(tmpVector)
        rotationMatrix.setColumn(0, tmpVector)

        rotationDMatrix3.getColumn1(tmpColumn)
        tmpColumn.toVector(tmpVector)
        rotationMatrix.setColumn(1, tmpVector)

        rotationDMatrix3.getColumn2(tmpColumn)
        tmpColumn.toVector(tmpVector)
        rotationMatrix.setColumn(2, tmpVector)

        rigidBody.position.toVector(position)
    }

    fun clear() {
        OdeHelper.closeODE()
        world = OdeHelper.createWorld()
        space = OdeHelper.createHashSpace()

        characterCapsules.clear()
        triMeshes.clear()
    }

    override fun call(data: Any?, o1: DGeom, o2: DGeom) {
        val contactsBuffer = DContactBuffer(MAX_CONTACTS)
        for (i in 0 until MAX_CONTACTS) {
            val contact = contactsBuffer[i]
            contact.surface.mode = dContactBounce or dContactSoftCFM
            contact.surface.mu = 50.0
            contact.surface.mu2 = 50.0
            contact.surface.bounce = 0.1
            contact.surface.bounce_vel = 0.1
            contact.surface.soft_cfm = 0.01
        }
        val n = OdeHelper.collide(o1, o2, MAX_CONTACTS, contactsBuffer.geomBuffer)
        for (i in 0 until n) {
            val contactJoint = OdeHelper.createContactJoint(world, contactGroup, contactsBuffer[i])
            contactJoint.attach(o1.body, o2.body)
        }
    }

    companion object {

        private const val SIMULATION_STEP_TIME = 0.01 // second
        private const val MAX_CONTACTS = 64
    }
}