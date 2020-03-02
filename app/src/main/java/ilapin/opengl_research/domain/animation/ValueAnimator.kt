package ilapin.opengl_research.domain.animation

import ilapin.common.android.log.L
import ilapin.common.math.lerp
import ilapin.opengl_research.app.App.Companion.LOG_TAG

/**
 * @author raynor on 24.02.20.
 */
class ValueAnimator(
    val interpolator: Interpolator,
    val start: Float,
    val end: Float,
    val duration: Float,
    val repeatCount: Int
) {
    private var _state: State =
        State.NOT_RUNNING

    private var _isEnded = false
    private var _isCancelled = false

    private var currentCount = 0
    private var currentTime = 0f
    private var _value = 0f

    private var isCancelRequested = false

    val state: State
        get() = _state

    val isEnded: Boolean
        get() = _isEnded

    val isCancelled
        get() = _isCancelled

    val value: Float
        get() = _value

    fun start() {
        if (_state != State.NOT_RUNNING) {
            L.e(LOG_TAG, "Animation is already running")
            return
        }

        _state = State.RUNNING

        currentTime = 0f
        currentCount = 0
        isCancelRequested = false

        _value = start
    }

    fun reverse() {
        TODO("Not implemented")
    }

    fun pause() {
        if (_state != State.RUNNING) {
            L.e(LOG_TAG, "Animation is not running")
            return
        }

        _state = State.PAUSED
    }

    fun resume() {
        if (_state != State.PAUSED) {
            L.e(LOG_TAG, "Animation is not paused")
            return
        }

        _state = State.RUNNING
    }

    fun cancel() {
        if (_state == State.NOT_RUNNING) {
            L.e(LOG_TAG, "Animation is not running")
            return
        }

        isCancelRequested = true
    }

    fun end() {
        if (_state == State.NOT_RUNNING) {
            L.e(LOG_TAG, "Animation is not running")
            return
        }

        currentTime = duration
    }

    fun update(dt: Float) {
        _isEnded = false
        _isCancelled = false

        if (_state != State.RUNNING) {
            return
        }

        val newTime = currentTime + dt
        if (newTime > duration) {
            when {
                repeatCount <= 0 -> {
                    currentTime = newTime % duration
                }

                currentCount >= repeatCount - 1 -> {
                    currentTime = duration
                    _isEnded = true
                    _state = State.NOT_RUNNING
                }

                else -> {
                    currentCount++
                    currentTime = newTime % duration
                }
            }
        } else {
            currentTime = newTime
        }

        _value = lerp(start, end, interpolator.interpolate(currentTime / duration))

        if (isCancelRequested) {
            _isCancelled = true
            _state = State.NOT_RUNNING
        }
    }

    enum class State {
        NOT_RUNNING, RUNNING, PAUSED
    }
}