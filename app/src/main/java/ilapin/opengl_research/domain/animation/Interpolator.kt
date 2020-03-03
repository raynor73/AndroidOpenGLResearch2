package ilapin.opengl_research.domain.animation

/**
 * @author raynor on 24.02.20.
 */
interface Interpolator {

    fun interpolate(input: Float): Float
}