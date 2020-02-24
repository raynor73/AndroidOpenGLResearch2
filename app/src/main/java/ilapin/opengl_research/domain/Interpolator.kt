package ilapin.opengl_research.domain

/**
 * @author raynor on 24.02.20.
 */
interface Interpolator {

    fun interpolate(progress: Float): Float
}