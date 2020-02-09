package ilapin.opengl_research.domain.physics_engine

import ilapin.opengl_research.toQuaternion
import ilapin.opengl_research.toVector
import org.joml.Quaternionf
import org.joml.Vector3f
import org.ode4j.math.DQuaternion
import org.ode4j.math.DVector3
import org.ode4j.ode.*
import org.ode4j.ode.OdeConstants.*
import kotlin.math.ceil


/**
 * @author raynor on 08.02.20.
 */
class PhysicsEngine : DGeom.DNearCallback {

    private val world: DWorld
    private val space: DSpace

    private val contactGroup: DJointGroup

    lateinit var capsuleRigidBody: DBody

    init {
        OdeHelper.initODE2(0)
        world = OdeHelper.createWorld()
        space = OdeHelper.createHashSpace()
        contactGroup = OdeHelper.createJointGroup()

        //world.setGravity(0.0, 0.0, 0.0)

        run {
            val mass = OdeHelper.createMass()
            val rigidBody = OdeHelper.createBody(world)

            rigidBody.mass = mass

            val collisionShape = OdeHelper.createCapsule(null, 1.0, 1.0)
            mass.setCapsuleTotal(1.0, 2, 1.0, 1.0)
            collisionShape.body = rigidBody

            rigidBody.position = Vector3f(0f, 4f, -5f).toVector()
            rigidBody.quaternion = Quaternionf().identity().toQuaternion()
            rigidBody.setLinearVel(0.0, 0.0, 0.0)
            rigidBody.setAngularVel(0.0, 0.0, 0.0)

            space.add(collisionShape)

            capsuleRigidBody = rigidBody
        }

        run {
            val rigidBody = OdeHelper.createBody(world)

            rigidBody.setKinematic()

//            val collisionShape = OdeHelper.createPlane(space, .0, 1.0, .0, .0)
            val collisionShape = OdeHelper.createBox(null, 1.0, 1.0, 1.0)
            collisionShape.body = rigidBody

            rigidBody.position = DVector3()
            val rotation = DQuaternion()
            rotation.setIdentity()
            rigidBody.quaternion = rotation
            rigidBody.setLinearVel(.0, .0, .0)
            rigidBody.setAngularVel(.0, .0, .0)

            space.add(collisionShape)
        }
    }

    private var count = 0
    fun update(dt: Float) {
        /*if (count >= 1) {
            return
        }
        count++*/

//        val startTime = SystemClock.elapsedRealtimeNanos()
        /*OdeHelper.spaceCollide(space, 0, this)
        world.quickStep(SIMULATION_STEP_TIME)
        contactGroup.empty()*/
//        L.d(LOG_TAG, "Elapsed time: ${(SystemClock.elapsedRealtimeNanos() - startTime) / 1000000f} ms")

        for (i in 0 until ceil(dt / SIMULATION_STEP_TIME).toInt()) {
            OdeHelper.spaceCollide(space, null, this)
            world.quickStep(SIMULATION_STEP_TIME)
            contactGroup.empty()
        }
    }

    override fun call(data: Any?, o1: DGeom, o2: DGeom) {
        val contactsBuffer = DContactBuffer(MAX_CONTACTS)
        val n = OdeHelper.collide(o1, o2, MAX_CONTACTS, contactsBuffer.geomBuffer)
        if (n > 0) {
            for (i in 0 until n) {
                val contact = contactsBuffer.get(i)
                // Paranoia  <-- not working for some people, temporarily removed for 0.6
                //dIASSERT(dVALIDVEC3(contact[i].geom.pos));
                //dIASSERT(dVALIDVEC3(contact[i].geom.normal));
                //dIASSERT(!dIsNan(contact[i].geom.depth));
                contact.surface.slip1 = 0.7
                contact.surface.slip2 = 0.7
                contact.surface.mode =
                    dContactSoftERP or dContactSoftCFM or dContactApprox1 or dContactSlip1 or dContactSlip2
                contact.surface.mu = 50.0 // was: dInfinity
                contact.surface.soft_erp = 0.96
                contact.surface.soft_cfm = 0.04
                val c = OdeHelper.createContactJoint(world, contactGroup, contact)
                c.attach(contact.geom.g1.body, contact.geom.g2.body)
            }
        }
        /*for (i in 0 until MAX_CONTACTS) {
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
        }*/
    }

    companion object {

        private const val SIMULATION_STEP_TIME = 0.016       // second
        private const val MAX_CONTACTS = 8
    }
}