package ilapin.opengl_research.app

import android.app.Application
import android.os.StrictMode
import ilapin.opengl_research.BuildConfig
import timber.log.Timber

/**
 * @author raynor on 24.01.20.
 */
class App : Application() {

    override fun onCreate() {
        super.onCreate()

        @Suppress("ConstantConditionIf")
        if (BuildConfig.DEVELOPER_MODE) {
            Timber.plant(Timber.DebugTree())

            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build()
            )
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build()
            )
        }
    }

    companion object {

        const val LOG_TAG = "OpenGLResearch"

        private lateinit var appComponent_: AppComponent

        val appComponent
            get() = appComponent_
    }
}