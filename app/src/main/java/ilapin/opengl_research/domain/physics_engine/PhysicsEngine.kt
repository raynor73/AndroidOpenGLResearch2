package ilapin.opengl_research.domain.physics_engine

import ilapin.opengl_research.cwConvertedIndices
import ilapin.opengl_research.domain.Mesh
import ilapin.opengl_research.toQuaternion
import ilapin.opengl_research.toVector
import ilapin.opengl_research.vertexCoordinatesOnlyAsArray
import org.joml.Quaternionf
import org.joml.Vector3fc
import org.ode4j.math.DQuaternion
import org.ode4j.ode.*
import org.ode4j.ode.OdeConstants.dContactBounce
import org.ode4j.ode.OdeConstants.dContactSoftCFM
import kotlin.math.PI
import kotlin.math.ceil

/**
 * @author raynor on 08.02.20.
 */
class PhysicsEngine : DGeom.DNearCallback {

    private val world: DWorld
    private val space: DSpace

    private val tmpQuaternion = Quaternionf()
    private val tmpDQuaternion = DQuaternion()

    private val contactGroup: DJointGroup

    private val characterCapsules = ArrayList<DBody>()

    init {
        OdeHelper.initODE2(0)
        world = OdeHelper.createWorld()
        space = OdeHelper.createHashSpace()
        contactGroup = OdeHelper.createJointGroup()

        world.setGravity(0.0, -9.81, 0.0)

        /*run {
            val rigidBody = OdeHelper.createBody(world)

            rigidBody.setKinematic()

            //val collisionShape = OdeHelper.createPlane(null, .0, 1.0, .0, .0)
            val triMeshData = OdeHelper.createTriMeshData()
            val vertices = floatArrayOf(
                -10f, 0f, -10f,
                10f, 0f, -10f,
                10f, 0f, 10f,
                -10f, 0f, 10f
            )
            val indices = intArrayOf(
                0, 1, 2,
                2, 3, 0
            )
            triMeshData.build(vertices, indices)
            triMeshData.preprocess()
            val collisionShape = OdeHelper.createTriMesh(null, triMeshData, null, null, null)
            collisionShape.body = rigidBody

            rigidBody.position = DVector3()
            val rotation = DQuaternion()
            rotation.setIdentity()
            rigidBody.quaternion = rotation
            rigidBody.setLinearVel(.0, .0, .0)
            rigidBody.setAngularVel(.0, .0, .0)

            space.add(collisionShape)
        }*/
    }

    fun setGravity(gravity: Vector3fc) {
        world.setGravity(gravity.toVector())
    }

    fun createCharacterCapsuleRigidBody(
        massValue: Float,
        radius: Float,
        length: Float,
        position: Vector3fc
    ): DBody {
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

        space.add(collisionShape)

        characterCapsules += rigidBody

        return rigidBody
    }

    fun createTriMeshCollisionShape(mesh: Mesh): DTriMesh {
        val triMeshData = OdeHelper.createTriMeshData()

        triMeshData.build(mesh.vertexCoordinatesOnlyAsArray(), mesh.cwConvertedIndices())
        triMeshData.preprocess()

        return OdeHelper.createTriMesh(space, triMeshData, null, null, null)
    }

    fun update(dt: Float) {
        repeat(ceil(dt / SIMULATION_STEP_TIME).toInt()) {
            OdeHelper.spaceCollide(space, null, this)
            world.step(SIMULATION_STEP_TIME)
            characterCapsules.forEach {
                tmpQuaternion.identity().rotateX(-(PI / 2).toFloat()).toQuaternion(tmpDQuaternion)
                it.quaternion = tmpDQuaternion
            }
            contactGroup.empty()
        }
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