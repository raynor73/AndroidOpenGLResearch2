package ilapin.opengl_research.ui

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import dagger.Module
import dagger.Provides
import ilapin.common.messagequeue.MessageQueue
import javax.inject.Named

/**
 * @author raynor on 17.02.20.
 */
@Module
class RendererModule(private val activity: AppCompatActivity) {

    @Provides
    @RendererScope
    fun provideMessageQueue(): MessageQueue {
        return MessageQueue()
    }

    @Provides
    @RendererScope
    @Named("Activity")
    fun provideActivityContext(): Context {
        return activity
    }

    @Provides
    @RendererScope
    fun provideRenderer(
        @Named("Activity") context: Context
    ): GLSurfaceViewRenderer? {
        return if (context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            GLSurfaceViewRenderer(this)
        } else {
            null
        }
    }
}