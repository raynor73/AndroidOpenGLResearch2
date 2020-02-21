function JoystickController(gestureConsumer, handleTransform) {

    //this.StateEnum = { IDLE: {}, DRAGGING: {} };

    this.gestureConsumer = gestureConsumer;
    this.handleTransform = handleTransform;
    this.width = (gestureConsumer.right - gestureConsumer.left) * pixelDensityFactor;
    this.height = (gestureConsumer.top - gestureConsumer.bottom) * pixelDensityFactor;

    //this.state = StateEnum.IDLE;

    this.joystickPositionX = 0;
    this.joystickPositionY = 0;

    this.update = function() {
        var touchEvents = this.gestureConsumer.touchEvents;

        if (touchEvents.size() == 0) {
            return;
        }

        var position = vectorsPool.obtain();

        for (var iterator = touchEvents.iterator(); iterator.hasNext();) {
            var touchEvent = iterator.next();

            if (
                touchEvent.action == Packages.ilapin.common.input.TouchEvent.Action.UP ||
                touchEvent.action == Packages.ilapin.common.input.TouchEvent.Action.CANCEL
            ) {
                position.set(this.handleTransform.localPosition);
                position.x = 0;
                position.z = 0;
                this.handleTransform.position = position;

                this.joystickPositionX = 0;
                this.joystickPositionY = 0;
            } else {
                var eventX = this.gestureConsumer.toLocalX(touchEvent.x);
                var eventY = this.gestureConsumer.toLocalY(touchEvent.y);

                position.set(this.handleTransform.localPosition);

                var halfWidth = this.width / 2;
                var halfHeight = this.height / 2;

                var clampedX = eventX - halfWidth;
                if (clampedX > halfWidth) {
                    clampedX = halfWidth;
                } else if (clampedX < -halfWidth) {
                    clampedX = -halfWidth;
                }

                var clampedY = -(eventY - this.height / 2);
                if (clampedY > halfHeight) {
                    clampedY = halfHeight;
                } else if (clampedY < -halfHeight) {
                    clampedY = -halfHeight;
                }

                position.x = clampedX;
                position.z = clampedY;

                this.handleTransform.position = position;

                this.joystickPositionX = clampedX / this.width * 2;
                this.joystickPositionY = -clampedY / this.height * 2;
            }
        }

        vectorsPool.recycle(position);
    };
}
