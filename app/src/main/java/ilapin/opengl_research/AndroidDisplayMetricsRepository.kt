package ilapin.opengl_research

import android.content.Context
import ilapin.opengl_research.domain.DisplayMetricsRepository

/**
 * @author raynor on 07.02.20.
 */
class AndroidDisplayMetricsRepository(private val context: Context) : DisplayMetricsRepository {

    private var _displayWidth: Int? = null
    private var _displayHeight: Int? = null

    override val displayWidth: Int
        get() = _displayWidth ?: error("Display width is unknown yet")
    override val displayHeight: Int
        get() = _displayHeight ?: error("Display height is unknown yet")

    override fun getPixelDensityFactor(): Float {
        return context.resources.displayMetrics.density
    }

    fun onDisplaySizeChanged(width: Int, height: Int) {
        _displayWidth = width
        _displayHeight = height
    }
}