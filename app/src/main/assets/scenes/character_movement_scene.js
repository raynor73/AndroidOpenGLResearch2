// var touchEventsRepository = ...
// var scene = ...
// var displayMetricsRepository = ...
// var quaternionsPool = ...
// var vectorsPool = ...

var rootGestureConsumer;

var uiCamera;
var directionalLight;

var leftJoystick;
var leftJoystickGestureConsumer;
var leftJoystickController;

var rightJoystick;
var rightJoystickGestureConsumer;
var rightJoystickController;

var scrollController;

var pixelDensityFactor;
var displayWidth;
var displayHeight;

var xAngle = -Math.PI / 2;
var zAngle = -Math.PI / 4;

function start() {
    rootGestureConsumer = scene.getGestureConsumerComponent(scene.rootGameObject);

    pixelDensityFactor = displayMetricsRepository.getPixelDensityFactor();
    displayWidth = displayMetricsRepository.displayWidth;
    displayHeight = displayMetricsRepository.displayHeight;

    directionalLight = findGameObject(scene.rootGameObject, "directional_light");
    uiCamera = findGameObject(scene.rootGameObject, "ui_camera");

    leftJoystick = findGameObject(scene.rootGameObject, "left_joystick")
    leftJoystickGestureConsumer = scene.getGestureConsumerComponent(leftJoystick)
    rightJoystick = findGameObject(scene.rootGameObject, "right_joystick")
    rightJoystickGestureConsumer = scene.getGestureConsumerComponent(rightJoystick)

    scrollController = new ScrollController(rootGestureConsumer);
    leftJoystickController = new JoystickController(
        leftJoystickGestureConsumer,
        scene.getTransformationComponent(findGameObject(leftJoystick, "left_joystick_handle"))
    );
    rightJoystickController = new JoystickController(
        rightJoystickGestureConsumer,
        scene.getTransformationComponent(findGameObject(rightJoystick, "right_joystick_handle"))
    );

    var orthoCamera = scene.getOrthoCameraComponent(uiCamera);
    orthoCamera.left = 0;
    orthoCamera.right = displayWidth;
    orthoCamera.bottom = 0;
    orthoCamera.top = displayHeight;

    layoutUi()
}

function update(dt) {
    scrollController.update();
    leftJoystickController.update();
    rightJoystickController.update();

    var scrollEvent = scrollController.scrollEvent;
    if (scrollEvent != null) {
        var transform = scene.getTransformationComponent(directionalLight);

        zAngle -= toRadians(scrollEvent.dx / pixelDensityFactor);
        xAngle -= toRadians(scrollEvent.dy / pixelDensityFactor);

        var lightRotation = quaternionsPool.obtain();

        lightRotation.identity();
        lightRotation.rotateZ(zAngle).rotateX(xAngle);
        transform.rotation = lightRotation;

        quaternionsPool.recycle(lightRotation);
    }
}

function onGoingToForeground() {
    // do nothing
}

function onGoingToBackground() {
    // do nothing
}

function layoutUi() {
    layoutLeftJoystick();
    layoutRightJoystick();
    layoutRootGestureConsumer();
}

function layoutLeftJoystick() {
    var position = vectorsPool.obtain();
    var scale = vectorsPool.obtain();

    var transform = scene.getTransformationComponent(leftJoystick);
    var backgroundTransform =
        scene.getTransformationComponent(findGameObject(leftJoystick, "left_joystick_background"));

    scale.set(backgroundTransform.scale);
    scale.mul(pixelDensityFactor);

    position.set(transform.position);
    position.x += scale.x / 2;
    position.y += scale.z / 2;
    transform.position = position;

    leftJoystickGestureConsumer.left = 0;
    leftJoystickGestureConsumer.top = scale.z;
    leftJoystickGestureConsumer.right = scale.x;
    leftJoystickGestureConsumer.bottom = 0;

    vectorsPool.recycle(position);
    vectorsPool.recycle(scale);
}

function layoutRightJoystick() {
    var position = vectorsPool.obtain();
    var scale = vectorsPool.obtain();

    var transform = scene.getTransformationComponent(rightJoystick);
    var backgroundTransform =
        scene.getTransformationComponent(findGameObject(rightJoystick, "right_joystick_background"));

    scale.set(backgroundTransform.scale);
    scale.mul(pixelDensityFactor);

    position.set(transform.position);
    position.x += displayWidth - scale.x / 2;
    position.y += scale.z / 2;
    transform.position = position;

    rightJoystickGestureConsumer.left = displayWidth - scale.x;
    rightJoystickGestureConsumer.top = scale.z;
    rightJoystickGestureConsumer.right = displayWidth;
    rightJoystickGestureConsumer.bottom = 0;

    vectorsPool.recycle(position);
    vectorsPool.recycle(scale);
}

function layoutRootGestureConsumer() {
    rootGestureConsumer.left = 0;
    rootGestureConsumer.top = displayHeight;
    rootGestureConsumer.right = displayWidth;
    rootGestureConsumer.bottom = 0;
}

function findGameObject(currentGameObject, name) {
    if (currentGameObject.name == name) {
        return currentGameObject;
    } else if (currentGameObject.children.size() > 0) {
        for (var iterator = currentGameObject.children.iterator(); iterator.hasNext();) {
            var child = findGameObject(iterator.next(), name);
            if (child != null) {
                return child;
            }
        }
    }

    return null;
}

function println(message) {
    java.lang.System.out.println("" + message);
}

function toRadians(degrees) {
    return degrees * (Math.PI / 180);
}

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

function JoystickController(gestureConsumer, handleTransform) {

    //this.StateEnum = { IDLE: {}, DRAGGING: {} };

    this.gestureConsumer = gestureConsumer;
    this.handleTransform = handleTransform;
    this.width = gestureConsumer.right - gestureConsumer.left;
    this.height = gestureConsumer.top - gestureConsumer.bottom;

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