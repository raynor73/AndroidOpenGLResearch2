package ilapin.opengl_research

import android.app.Application
import android.os.StrictMode
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
    }
}