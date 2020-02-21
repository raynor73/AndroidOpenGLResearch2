// var touchEventsRepository = ...
// var scene = ...
// var displayMetricsRepository = ...
// var quaternionsPool = ...
// var vectorsPool = ...

var uiCamera;
var directionalLight;
var leftJoystick;
var leftJoystickGestureConsumer;

var scrollController;
var leftJoystickController;

var pixelDensityFactor;
var displayWidth;
var displayHeight;

var xAngle = -Math.PI / 2;
var zAngle = -Math.PI / 4;

function start() {
    pixelDensityFactor = displayMetricsRepository.getPixelDensityFactor();
    displayWidth = displayMetricsRepository.displayWidth;
    displayHeight = displayMetricsRepository.displayHeight;

    directionalLight = findGameObject(scene.rootGameObject, "directional_light");
    uiCamera = findGameObject(scene.rootGameObject, "ui_camera");
    leftJoystick = findGameObject(scene.rootGameObject, "left_joystick")
    leftJoystickGestureConsumer = scene.getGestureConsumerComponent(leftJoystick)

    scrollController = new ScrollController();
    leftJoystickController = new JoystickController(
        leftJoystickGestureConsumer,
        scene.getTransformationComponent(findGameObject(leftJoystick, "left_joystick_handle"))
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
    layoutLeftJoystick()
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

function ScrollController() {

    this.prevTouchEvent = null;

    this.scrollEvent = null;

    this.update = function() {
        this.scrollEvent = null;

        var touchEvent = null;
        if (touchEventsRepository.touchEvents.size() > 0) {
            touchEvent = touchEventsRepository.touchEvents.get(0);
        }

        if (touchEvent != null && this.prevTouchEvent != null) {
            this.scrollEvent = new ScrollEvent(
                touchEvent.x - this.prevTouchEvent.x,
                touchEvent.y - this.prevTouchEvent.y
            );
        }
        this.prevTouchEvent = touchEvent;
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

    this.update = function() {
        var touchEvents = this.gestureConsumer.touchEvents;

        if (touchEvents.size() == 0) {
            return;
        }

        var position = vectorsPool.obtain();

        for (var iterator = touchEvents.iterator(); iterator.hasNext();) {
            var touchEvent = iterator.next();

            println("!@# js action: " + touchEvent.action);

            if (
                touchEvent.action == Packages.ilapin.common.input.TouchEvent.Action.UP ||
                touchEvent.action == Packages.ilapin.common.input.TouchEvent.Action.CANCEL
            ) {
                position.set(this.handleTransform.localPosition);
                position.x = 0;
                position.z = 0;
                this.handleTransform.position = position;
            } else {
                var eventX = this.gestureConsumer.toLocalX(touchEvent.x);
                var eventY = this.gestureConsumer.toLocalY(touchEvent.y);

                position.set(this.handleTransform.localPosition);

                position.x = eventX - this.width / 2;
                position.z = -(eventY - this.height / 2);

                this.handleTransform.position = position;
            }
        }

        vectorsPool.recycle(position);
    };
}