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
                prevTouchEvent != null &&
                touchEvent.action == Packages.ilapin.common.input.TouchEvent.Action.UP ||
                touchEvent.action == Packages.ilapin.common.input.TouchEvent.Action.CANCEL
            ) {
                this.prevTouchEvent = null;
                this.isClickDetected = true;
            } else {
                this.prevTouchEvent = touchEvent;
            }
        }
    }
}