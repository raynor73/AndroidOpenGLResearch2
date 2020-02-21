package ilapin.opengl_research.ui

import android.annotation.SuppressLint
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import ilapin.common.android.kotlin.gone
import ilapin.common.android.kotlin.setVisible
import ilapin.common.input.TouchEvent
import ilapin.common.kotlin.plusAssign
import ilapin.opengl_research.R
import ilapin.opengl_research.app.App
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    @Inject
    @JvmField
    var renderer: GLSurfaceViewRenderer? = null

    private val subscriptions = CompositeDisposable()

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        App.appComponent.mainScreenComponent(MainScreenModule(this)).inject(this)

        progressBar.gone()

        renderer?.let { renderer ->
            val glView = GLSurfaceView(this)
            glView.setOnTouchListener { _, event ->
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                        renderer.putMessage(TouchEvent(
                            event.getPointerId(event.actionIndex),
                            TouchEvent.Action.DOWN,
                            event.getX(event.actionIndex).toInt(),
                            glView.height - event.getY(event.actionIndex).toInt()
                        ))
                    }

                    MotionEvent.ACTION_MOVE -> {
                        repeat(event.pointerCount) { i ->
                            renderer.putMessage(TouchEvent(
                                event.getPointerId(i),
                                TouchEvent.Action.MOVE,
                                event.getX(i).toInt(),
                                glView.height - event.getY(i).toInt()
                            ))
                        }
                    }

                    MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                        renderer.putMessage(TouchEvent(
                            event.getPointerId(event.actionIndex),
                            TouchEvent.Action.UP,
                            event.getX(event.actionIndex).toInt(),
                            glView.height - event.getY(event.actionIndex).toInt()
                        ))
                    }

                    else -> {
                        renderer.putMessage(TouchEvent(
                            event.getPointerId(event.actionIndex),
                            TouchEvent.Action.CANCEL,
                            event.getX(event.actionIndex).toInt(),
                            glView.height - event.getY(event.actionIndex).toInt()
                        ))
                    }
                }
                true
            }
            glView.setEGLContextClientVersion(2)
            glView.setRenderer(renderer)
            glView.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
            containerLayout.addView(glView, 0)

            subscriptions += renderer.isLoading.subscribe { isLoading -> progressBar.setVisible(isLoading) }

            renderer.putMessage(GLSurfaceViewRenderer.LoadAndStartSceneMessage("scenes/character_movement_scene.json"))
        }
    }

    override fun onPause() {
        super.onPause()

        renderer?.putMessage(GLSurfaceViewRenderer.LifecycleMessage.GoingToBackgroundMessage)
    }

    override fun onResume() {
        super.onResume()

        hideControls()
        renderer?.putMessage(GLSurfaceViewRenderer.LifecycleMessage.GoingToForegroundMessage)
    }

    override fun onDestroy() {
        super.onDestroy()

        subscriptions.clear()
        renderer?.putMessageAndWaitForExecution(GLSurfaceViewRenderer.LifecycleMessage.DeinitMessage)
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
