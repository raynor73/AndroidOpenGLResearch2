package ilapin.opengl_research.domain.skeletal_animation

import ilapin.common.time.TimeRepository
import ilapin.engine3d.GameObjectComponent
import ilapin.opengl_research.NANOS_IN_SECOND
import ilapin.opengl_research.ObjectsPool
import org.joml.Matrix4f
import org.joml.Matrix4fc
import org.joml.Quaternionf
import org.joml.Vector3f

/**
 * @author raynor on 12.02.20.
 */
class SkeletalAnimatorComponent(
    private val vectorsPool: ObjectsPool<Vector3f>,
    private val quaternionsPool: ObjectsPool<Quaternionf>,
    private val matrixPool: ObjectsPool<Matrix4f>,
    private val timeRepository: TimeRepository
) : GameObjectComponent() {

    private var startTimestamp: Long? = null
    private var prevTimestamp: Long? = null
    private var currentAnimationComponent: SkeletalAnimationComponent? = null

    private var _jointTransforms: ArrayList<Matrix4fc?>? = null

    private val pose = HashMap<String, Matrix4fc>()

    val jointTransforms: List<Matrix4fc?>? = _jointTransforms

    override fun update() {
        super.update()

        if (!isEnabled) {
            return
        }

        val startTimestamp = startTimestamp ?: return
        val currentTimestamp = timeRepository.getTimestamp()

        val animationComponent = gameObject?.getComponent(SkeletalAnimationComponent::class.java)
        if (animationComponent == null) {
            stop()
            return
        } else if (currentAnimationComponent != null && currentAnimationComponent != animationComponent) {
            stop()

            calculatePose(animationComponent.animation, 0f)
            applyPoseToJoints(animationComponent.rootJoint, IDENTITY)
            return
        } else if (currentAnimationComponent == null) {
            currentAnimationComponent = animationComponent
        }

        val currentAnimationTime =
            ((currentTimestamp - startTimestamp) / NANOS_IN_SECOND) % animationComponent.animation.length

        calculatePose(animationComponent.animation, currentAnimationTime)
        applyPoseToJoints(animationComponent.rootJoint, IDENTITY)
    }

    fun start() {
        startTimestamp = timeRepository.getTimestamp()
        prevTimestamp = null
    }

    // TODO Implement pause and seek(?)
    /*fun pause() {

    }*/

    fun stop() {
        startTimestamp = null

        _jointTransforms?.forEach { matrixPool.recycle(it as Matrix4f) }
        _jointTransforms = null
    }

    private fun calculatePose(animation: SkeletalAnimation, currentAnimationTime: Float) {
        val frames = findPreviousAndNextKeyFrames(
            animation,
            currentAnimationTime
        )
        val progression = calculateProgression(frames, currentAnimationTime)
        interpolatePose(frames, progression)
    }

    private fun applyPoseToJoints(
        joint: Joint,
        parentTransform: Matrix4fc
    ) {
        _jointTransforms?.let { prevJointTransforms ->
            prevJointTransforms.forEach { matrixPool.recycle(it as Matrix4f) }
            if (pose.keys.size > prevJointTransforms.size) {
                repeat(pose.keys.size - prevJointTransforms.size) { prevJointTransforms.add(null) }
            }
            prevJointTransforms.indices.forEach { i -> prevJointTransforms[i] = null }
        }
        val jointTransforms = _jointTransforms ?: ArrayList<Matrix4fc?>().apply {
            indices.forEach { i -> set(i, null) }
            _jointTransforms = this
        }

        val currentLocalTransform = pose[joint.name]
        val currentTransform = matrixPool.obtain()
        currentTransform.set(parentTransform)
        currentTransform.mul(currentLocalTransform)

        joint.children.forEach { child -> applyPoseToJoints(child, currentTransform) }

        currentTransform.mul(joint.invertedBindTransform)

        jointTransforms[joint.index] = currentTransform
    }

    private fun interpolatePose(keyFrames: KeyFramesPair, progression: Float) {
        pose.clear()

        for (jointName in keyFrames.previous.jointLocalTransforms.keys) {
            val previousTransform = keyFrames.previous.jointLocalTransforms[jointName]
                    ?: error("Previous joint transform not found")
            val nextTransform = keyFrames.next.jointLocalTransforms[jointName]
                    ?: error("Next joint transform not found")
            val currentTransform = interpolate(previousTransform, nextTransform, progression)
            pose[jointName] = currentTransform.transform
        }
    }

    private fun interpolate(previous: JointLocalTransform, next: JointLocalTransform, progress: Float): JointLocalTransform {
        val interpolatedPosition = vectorsPool.obtain()
        interpolatedPosition.set(previous.position).lerp(next.position, progress)

        val interpolatedRotation = quaternionsPool.obtain()
        interpolatedRotation.set(previous.rotation).slerp(next.rotation, progress)

        val interpolatedJointTransform = JointLocalTransform(interpolatedPosition, interpolatedRotation)

        vectorsPool.recycle(interpolatedPosition)
        quaternionsPool.recycle(interpolatedRotation)

        return interpolatedJointTransform
    }

    private fun findPreviousAndNextKeyFrames(
        animation: SkeletalAnimation,
        currentAnimationTime: Float
    ): KeyFramesPair {
        val allFrames = animation.keyFrames
        var previousFrame = allFrames[0]
        var nextFrame = allFrames[0]
        for (i in 1 until allFrames.size) {
            nextFrame = allFrames[i]
            if (nextFrame.time > currentAnimationTime) {
                break
            }
            previousFrame = allFrames[i]
        }
        return KeyFramesPair(previousFrame, nextFrame)
    }

    private fun calculateProgression(
        keyFramesPair: KeyFramesPair,
        currentAnimationTime: Float
    ): Float {
        val totalTime = keyFramesPair.next.time - keyFramesPair.previous.time
        val currentTime = currentAnimationTime - keyFramesPair.previous.time
        return currentTime / totalTime
    }

    private class KeyFramesPair(
        val previous: KeyFrame,
        val next: KeyFrame
    )

    companion object {

        private val IDENTITY: Matrix4fc = Matrix4f()
    }
}