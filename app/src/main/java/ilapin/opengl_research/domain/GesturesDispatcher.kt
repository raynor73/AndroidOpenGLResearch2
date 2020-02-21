package ilapin.opengl_research.domain

import android.annotation.SuppressLint
import ilapin.common.input.TouchEvent

/**
 * @author raynor on 20.02.20.
 */
class GesturesDispatcher {

    private val gestureConsumers = ArrayList<GestureConsumerComponent>()

    @SuppressLint("UseSparseArrays")
    private val gestureOwners = HashMap<Int, GestureConsumerComponent>()

    fun addGestureConsumer(gestureConsumerComponent: GestureConsumerComponent) {
        if (gestureConsumers.contains(gestureConsumerComponent)) {
            error("Trying to add gesture consumer ${gestureConsumerComponent.gameObject?.name} twice")
        }

        gestureConsumers += gestureConsumerComponent

        updateOrder()
    }

    fun removeGestureConsumer(gestureConsumerComponent: GestureConsumerComponent) {
        if (!gestureConsumers.contains(gestureConsumerComponent)) {
            error("Gesture consumer ${gestureConsumerComponent.gameObject?.name} not found")
        }

        gestureConsumers -= gestureConsumerComponent

        if (gestureOwners.values.contains(gestureConsumerComponent)) {
            gestureOwners.values.remove(gestureConsumerComponent)
        }

        updateOrder()
    }

    fun removeAllGestureConsumers() {
        gestureConsumers.clear()
        gestureOwners.clear()
    }

    fun begin() {
        gestureConsumers.forEach { it.clearPrevTouchEvent() }
    }

    fun onTouchEvent(touchEvent: TouchEvent) {
        if (gestureOwners.containsKey(touchEvent.id)) {
            gestureOwners[touchEvent.id]?.onTouchEvent(touchEvent)
        } else if (touchEvent.action == TouchEvent.Action.DOWN) {
            findMatchingGestureConsumer(touchEvent)?.let {
                gestureOwners[touchEvent.id] = it
                it.onTouchEvent(touchEvent)
            }
        }

        if (touchEvent.action == TouchEvent.Action.CANCEL || touchEvent.action == TouchEvent.Action.UP) {
            gestureOwners.remove(touchEvent.id)
        }
    }

    private fun findMatchingGestureConsumer(touchEvent: TouchEvent): GestureConsumerComponent? {
        gestureConsumers.forEach {
            if (
                touchEvent.x >= it.left && touchEvent.x <= it.right &&
                touchEvent.y >= it.bottom && touchEvent.y <= it.top
            ) {
                return it
            }
        }

        return null
    }

    private fun updateOrder() {
        gestureConsumers.sortBy { it.priority }
        gestureConsumers.reverse()
    }
}