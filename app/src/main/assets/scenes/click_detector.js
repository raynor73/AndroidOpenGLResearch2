function ClickDetector(gestureConsumer) {

    this.gestureConsumer = gestureConsumer;

    this.prevTouchEvent = null;

    this.isClickDetected = false;

    this.update = function() {
        this.isClickDetected = false;

        var touchEvents = this.gestureConsumer.touchEvents;

        if (touchEvents.size() == 0) {
            return;
        }

        for (var iterator = touchEvents.iterator(); iterator.hasNext();) {
            var touchEvent = iterator.next();

            if (
                this.prevTouchEvent != null &&
                touchEvent.action == Packages.ilapin.common.input.TouchEvent.Action.UP ||
                touchEvent.action == Packages.ilapin.common.input.TouchEvent.Action.CANCEL
            ) {
                this.prevTouchEvent = null;
                if (
                    touchEvent.x > this.gestureConsumer.left &&
                    touchEvent.x < this.gestureConsumer.right &&
                    touchEvent.y > this.gestureConsumer.bottom &&
                    touchEvent.y < this.gestureConsumer.top
                ) {
                    this.isClickDetected = true;
                }
            } else {
                this.prevTouchEvent = touchEvent;
            }
        }
    }
}