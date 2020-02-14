package ilapin.opengl_research.domain.skeletal_animation

import ilapin.common.time.TimeRepository
import ilapin.engine3d.GameObjectComponent
import ilapin.opengl_research.NANOS_IN_SECOND
import ilapin.opengl_research.ObjectsPool
import org.joml.Matrix4fc
import org.joml.Quaternionf
import org.joml.Vector3f

/**
 * @author raynor on 12.02.20.
 */
class SkeletalAnimatorComponent(
    private val vectorsPool: ObjectsPool<Vector3f>,
    private val quaternionsPool: ObjectsPool<Quaternionf>,
    private val timeRepository: TimeRepository
) : GameObjectComponent() {

    private var startTimestamp: Long? = null
    private var prevTimestamp: Long? = null
    private var currentAnimationComponent: SkeletalAnimationComponent? = null

    private val _jointTransforms = ArrayList<JointTransform>()

    private val pose = HashMap<String, Matrix4fc>()

    val jointTransforms: List<JointTransform> = _jointTransforms

    /*init {
        repeat(MAX_JOINTS) { _jointTransforms += JointTransform(Vector3f(), Quaternionf()) }
    }*/

    override fun update() {
        super.update()

        if (!isEnabled) {
            return
        }

        val animationComponent =
            gameObject?.getComponent(SkeletalAnimationComponent::class.java) ?: return
        if (currentAnimationComponent != null && currentAnimationComponent != animationComponent) {
            stop()
            return
        } else if (currentAnimationComponent == null) {
            currentAnimationComponent = animationComponent
        }

        val startTimestamp = startTimestamp ?: return
        val currentTimestamp = timeRepository.getTimestamp()

        val currentAnimationTime =
            ((currentTimestamp - startTimestamp) / NANOS_IN_SECOND) % animationComponent.animation.length

        val frames = findPreviousAndNextKeyFrames(
            animationComponent.animation,
            currentAnimationTime
        )
        val progression = calculateProgression(frames[0], frames[1])
        return interpolatePoses(frames[0], frames[1], progression)
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
    }

    private fun interpolatePoses(keyFrames: KeyFramesPair, progression: Float) {
        pose.clear()

        for (jointName in keyFrames.previous.jointTransforms.keys) {
            val previousTransform = keyFrames.previous.jointTransforms[jointName]
                    ?: error("Previous joint transform not found")
            val nextTransform = keyFrames.next.jointTransforms[jointName]
                    ?: error("Next joint transform not found")
            val currentTransform = interpolate(previousTransform, nextTransform, progression)
            pose[jointName] = currentTransform.transform
        }
    }

    private fun interpolate(previous: JointTransform, next: JointTransform, progress: Float): JointTransform {
        val interpolatedPosition = vectorsPool.obtain()
        interpolatedPosition.set(previous.position).lerp(next.position, progress)

        val interpolatedRotation = quaternionsPool.obtain()
        interpolatedRotation.set(previous.rotation).slerp(next.rotation, progress)

        val interpolatedJointTransform = JointTransform(interpolatedPosition, interpolatedRotation)

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
}