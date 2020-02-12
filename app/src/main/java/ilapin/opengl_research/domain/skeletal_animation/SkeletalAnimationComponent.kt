package ilapin.opengl_research.domain.skeletal_animation

import ilapin.common.time.TimeRepository
import ilapin.engine3d.GameObjectComponent
import ilapin.opengl_research.MAX_JOINTS
import org.joml.Matrix4f
import org.joml.Matrix4fc

/**
 * @author raynor on 12.02.20.
 */
class SkeletalAnimationComponent(
    private val timeRepository: TimeRepository
) : GameObjectComponent() {

    private var startTimestamp: Long? = null
    private var prevTimestamp: Long? = null

    private val _jointTransforms = ArrayList<Matrix4f>()

    val jointTransforms: List<Matrix4fc> = _jointTransforms

    init {
        repeat(MAX_JOINTS) { _jointTransforms += Matrix4f().identity() }
    }

    override fun update() {
        super.update()

        if (!isEnabled) {
            return
        }

        val startTimestamp = startTimestamp ?: return
        val currentTimestamp = timeRepository.getTimestamp()


    }

    fun start() {
        startTimestamp = timeRepository.getTimestamp()
        prevTimestamp = null
    }

    // TODO Imoplemnt pause and seek(?)
    /*fun pause() {

    }*/

    fun stop() {
        startTimestamp = null
    }
}