package ilapin.opengl_research.ui.skeletal_animation_debug

import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import ilapin.opengl_research.R
import ilapin.opengl_research.app.App
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

/**
 * @author Игорь on 12.03.2020.
 */
class SkeletalAnimationDebugActivity : AppCompatActivity() {

    @Inject
    @JvmField
    var renderer: SkeletalAnimationDebugRenderer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_skeletal_animation_debug)

        App
            .appComponent
            .skeletalAnimationDebugScreenComponent(SkeletalAnimationDebugScreenModule(this))
            .inject(this)

        renderer?.let { renderer ->
            val glView = GLSurfaceView(this)
            glView.setEGLContextClientVersion(2)
            glView.setRenderer(renderer)
            glView.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
            containerLayout.addView(glView, 0)
        }
    }

    override fun onResume() {
        super.onResume()

        hideControls()
    }

    private fun hideControls() {
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LOW_PROFILE or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
    }
}