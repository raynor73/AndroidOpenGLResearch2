package ilapin.opengl_research.domain

/**
 * @author raynor on 07.02.20.
 */
interface DisplayMetricsRepository {

    val displayWidth: Int
    val displayHeight: Int

    fun getPixelDensityFactor(): Float
}