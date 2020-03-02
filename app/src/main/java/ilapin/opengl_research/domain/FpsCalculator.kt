package ilapin.opengl_research.domain

import java.util.*

/**
 * @author Игорь on 02.03.2020.
 */
class FpsCalculator {

    private var _fps = 0f

    private val buffer = LinkedList<Float>()

    val fps: Float
        get() = _fps

    fun update(dt: Float) {
        buffer.addLast(dt)
        if (buffer.size > MAX_BUFFER_SIZE) {
            buffer.removeFirst()
        }

        _fps = buffer.sum() / buffer.size
        _fps = 1 / _fps
    }

    companion object {

        private const val MAX_BUFFER_SIZE = 100
    }
}