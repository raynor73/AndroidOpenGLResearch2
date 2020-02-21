function ScrollController(gestureConsumer) {

    this.gestureConsumer = gestureConsumer;

    this.prevTouchEvent = null;

    this.scrollEvent = null;

    this.update = function() {
        this.scrollEvent = null;

        var touchEvents = this.gestureConsumer.touchEvents;

        if (touchEvents.size() == 0) {
            return;
        }

        for (var iterator = touchEvents.iterator(); iterator.hasNext();) {
            var touchEvent = iterator.next();

            if (this.prevTouchEvent != null) {
                this.scrollEvent = new ScrollEvent(
                    touchEvent.x - this.prevTouchEvent.x,
                    touchEvent.y - this.prevTouchEvent.y
                );
            }

            if (
                touchEvent.action == Packages.ilapin.common.input.TouchEvent.Action.UP ||
                touchEvent.action == Packages.ilapin.common.input.TouchEvent.Action.CANCEL
            ) {
                this.prevTouchEvent = null;
            } else {
                this.prevTouchEvent = touchEvent;
            }
        }
    };
}

function ScrollEvent(dx, dy) {
    this.dx = dx;
    this.dy = dy;
}