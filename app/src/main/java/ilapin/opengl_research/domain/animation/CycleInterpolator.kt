package ilapin.opengl_research.domain.animation

import kotlin.math.PI
import kotlin.math.sin

/**
 * @author ilapin on 03.03.20.
 */
class CycleInterpolator(val cycles: Int = 1) : Interpolator {

    override fun interpolate(input: Float): Float {
        return sin(2 * cycles * PI * input).toFloat()
    }
}