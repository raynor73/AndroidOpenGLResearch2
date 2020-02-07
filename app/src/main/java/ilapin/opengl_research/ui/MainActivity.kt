package ilapin.opengl_research.ui

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.opengl.GLSurfaceView
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import ilapin.common.input.TouchEvent
import ilapin.opengl_research.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            val renderer = GLSurfaceViewRenderer(this)
            val glView = GLSurfaceView(this)
            glView.setOnTouchListener { _, event ->
                renderer.putMessage(
                    TouchEvent(
                        when (event.action) {
                            MotionEvent.ACTION_DOWN -> TouchEvent.Action.DOWN
                            MotionEvent.ACTION_MOVE -> TouchEvent.Action.MOVE
                            MotionEvent.ACTION_UP -> TouchEvent.Action.UP
                            else -> TouchEvent.Action.CANCEL
                        },
                        event.x.toInt(),
                        event.y.toInt()
                    )
                )
                true
            }
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
