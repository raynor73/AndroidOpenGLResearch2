package ilapin.opengl_research.domain.physics_engine

import ilapin.opengl_research.domain.Mesh
import ilapin.opengl_research.toQuaternion
import ilapin.opengl_research.toVector
import ilapin.opengl_research.vertexCoordinatesOnlyAsArray
import org.joml.*
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
    private val tmpVector = Vector3f()
    private val tmpColumn = DVector3()
    private val tmpDVector = DVector3()

    private val contactGroup = OdeHelper.createJointGroup()

    private val characterCapsules = HashMap<String, DBody>()
    private val rigidBodies = HashMap<String, DBody>()
    private val collisionShapes = HashMap<String, DGeom>()
    private val linearMotors = HashMap<String, DLMotorJoint>()
    private val angularMotors = HashMap<String, DAMotorJoint>()

    init {
        initODE()
    }

    fun setGravity(gravity: Vector3fc) {
        world?.setGravity(gravity.toVector())
    }

    fun setPosition(rigidBodyName: String, position: Vector3fc) {
        position.toVector(tmpDVector)
        getRigidBody(rigidBodyName).position = tmpDVector
    }

    fun setRotation(rigidBodyName: String, rotation: Quaternionfc) {
        rotation.toQuaternion(tmpDQuaternion)
        getRigidBody(rigidBodyName).quaternion = tmpDQuaternion
    }

    fun addForce(rigidBodyName: String, force: Vector3fc) {
        (rigidBodies[rigidBodyName] ?: error("Rigid body $rigidBodyName not found")).addForce(
                force.x().toDouble(),
                force.y().toDouble(),
                force.z().toDouble()
        )
    }

    fun addTorque(rigidBodyName: String, torque: Vector3fc) {
        torque.toVector(tmpDVector)
        getRigidBody(rigidBodyName).addTorque(tmpDVector)
    }

    fun setVelocityDirectly(rigidBodyName: String, velocity: Vector3fc) {
        velocity.toVector(tmpDVector)
        getRigidBody(rigidBodyName).linearVel = tmpDVector
    }

    fun setVelocityViaMotor(rigidBodyName: String, velocity: Vector3fc) {
        val motor = linearMotors[rigidBodyName] ?: error("Linear motor for $rigidBodyName not found")
        motor.paramVel = velocity.x().toDouble()
        motor.paramVel2 = velocity.y().toDouble()
        motor.paramVel3 = velocity.z().toDouble()
    }

    fun setAngularVelocityViaMotor(rigidBodyName: String, velocity: Vector3fc) {
        val motor = angularMotors[rigidBodyName] ?: error("Angular motor for $rigidBodyName not found")
        motor.paramVel = velocity.x().toDouble()
        motor.paramVel2 = velocity.y().toDouble()
        motor.paramVel3 = velocity.z().toDouble()
    }

    fun setAngularVelocityDirectly(rigidBodyName: String, angularVelocity: Vector3fc) {
        angularVelocity.toVector(tmpDVector)
        getRigidBody(rigidBodyName).angularVel = tmpDVector
    }

    fun createCharacterCapsuleRigidBody(
        name: String,
        massValue: Float,
        radius: Float,
        length: Float,
        position: Vector3fc,
        maxMotorForceX: Float,
        maxMotorForceY: Float,
        maxMotorForceZ: Float/*,
        maxMotorTorqueX: Float,
        maxMotorTorqueY: Float,
        maxMotorTorqueZ: Float*/
    ) {
        if (characterCapsules.containsKey(name)) {
            error("Already has $name Character Capsule")
        }

        val mass = OdeHelper.createMass()

        val rigidBody = OdeHelper.createBody(world)
        rigidBodies[name] = rigidBody

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

        val motor = OdeHelper.createLMotorJoint(world, null)
        motor.numAxes = 3
        motor.setAxis(0, 0, 1.0, 0.0, 0.0)
        motor.setAxis(1, 0, 0.0, 1.0, 0.0)
        motor.setAxis(2, 0, 0.0, 0.0, 1.0)
        motor.paramFMax = maxMotorForceX.toDouble()
        motor.paramFMax2 = maxMotorForceY.toDouble()
        motor.paramFMax3 = maxMotorForceZ.toDouble()
        motor.paramVel = 0.0
        motor.paramVel2 = 0.0
        motor.paramVel3 = 0.0

        motor.attach(rigidBody, null)
        linearMotors[name] = motor

        /*val angularMotor = OdeHelper.createAMotorJoint(world, null)
        angularMotor.numAxes = 3
        angularMotor.setAxis(0, 0, 1.0, 0.0, 0.0)
        angularMotor.setAxis(1, 0, 0.0, 1.0, 0.0)
        angularMotor.setAxis(2, 0, 0.0, 0.0, 1.0)
        angularMotor.paramFMax = maxMotorTorqueX.toDouble()
        angularMotor.paramFMax2 = maxMotorTorqueY.toDouble()
        angularMotor.paramFMax3 = maxMotorTorqueZ.toDouble()
        angularMotor.paramVel = 0.0
        angularMotor.paramVel2 = 0.0
        angularMotor.paramVel3 = 0.0

        angularMotor.attach(rigidBody, null)
        angularMotors[name] = angularMotor*/

        characterCapsules[name] = rigidBody
    }

    fun createTriMeshRigidBody(
        name: String,
        mesh: Mesh,
        massValue: Float?,
        position: Vector3fc,
        rotation: Quaternionfc
    ) {
        val triMeshData = OdeHelper.createTriMeshData()

        triMeshData.build(mesh.vertexCoordinatesOnlyAsArray(), mesh.indices.map { it.toInt() }.toIntArray())
        triMeshData.preprocess()

        val triMesh = OdeHelper.createTriMesh(space, triMeshData, null, null, null)

        val mass = OdeHelper.createMass()

        val rigidBody = OdeHelper.createBody(world)
        rigidBodies[name] = rigidBody

        if (massValue != null) {
            mass.setTrimeshTotal(massValue.toDouble(), triMesh)
            rigidBody.mass = mass
        } else {
            rigidBody.setKinematic()
        }

        triMesh.body = rigidBody

        rigidBody.position = position.toVector()
        rigidBody.quaternion = rotation.toQuaternion()
    }

    fun removeRigidBody(rigidBodyName: String) {
        linearMotors.getOrElse(rigidBodyName) { error("Linear motor for $rigidBodyName not found") }.destroy()
        angularMotors.getOrElse(rigidBodyName) { error("Angular motor for $rigidBodyName not found") }.destroy()
        space?.remove(collisionShapes[rigidBodyName] ?: error("Collision shape for $rigidBodyName not found"))
        collisionShapes.remove(rigidBodyName)
        characterCapsules.remove(rigidBodyName)
        rigidBodies.remove(rigidBodyName) ?: error("Rigid body $rigidBodyName not found")
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

        world = null
        space = null

        characterCapsules.clear()
        rigidBodies.clear()
        contactGroup.clear()

        initODE()
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

    private fun initODE() {
        OdeHelper.initODE2(0)
        world = OdeHelper.createWorld()
        space = OdeHelper.createHashSpace()
    }

    private fun getRigidBody(name: String): DBody {
        return rigidBodies.getOrElse(name) { error("Rigid body $name not found") }
    }

    companion object {

        private const val SIMULATION_STEP_TIME = 0.01 // second
        private const val MAX_CONTACTS = 64
    }
}