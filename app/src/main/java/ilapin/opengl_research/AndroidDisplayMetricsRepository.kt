package ilapin.opengl_research

import android.content.Context
import ilapin.opengl_research.domain.DisplayMetricsRepository

/**
 * @author raynor on 07.02.20.
 */
class AndroidDisplayMetricsRepository(private val context: Context) : DisplayMetricsRepository {

    override fun getPixelDensityFactor(): Float {
        return context.resources.displayMetrics.density
    }
}