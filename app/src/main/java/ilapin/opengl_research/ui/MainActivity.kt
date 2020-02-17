package ilapin.opengl_research.ui

import android.annotation.SuppressLint
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import ilapin.common.input.TouchEvent
import ilapin.common.kotlin.plusAssign
import ilapin.opengl_research.JoystickPositionEvent
import ilapin.opengl_research.R
import ilapin.opengl_research.app.App
import ilapin.opengl_research.domain.Joystick
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    var renderer: GLSurfaceViewRenderer? = null

    private val subscriptions = CompositeDisposable()

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        App.appComponent.inject(this)

        renderer?.let { renderer ->
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

            subscriptions += leftJoystickView.positionObservable.subscribe { position ->
                renderer.putMessage(JoystickPositionEvent(
                    GLSurfaceViewRenderer.LEFT_JOYSTICK_ID,
                    Joystick.Position(position.x, position.y)
                ))
            }

            subscriptions += rightJoystickView.positionObservable.subscribe { position ->
                renderer.putMessage(JoystickPositionEvent(
                    GLSurfaceViewRenderer.RIGHT_JOYSTICK_ID,
                    Joystick.Position(position.x, position.y)
                ))
            }
        }
    }

    override fun onResume() {
        super.onResume()

        hideControls()
    }

    override fun onDestroy() {
        super.onDestroy()

        subscriptions.clear()
        renderer?.putMessageAndWaitForExecution(GLSurfaceViewRenderer.DeinitMessage)
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
