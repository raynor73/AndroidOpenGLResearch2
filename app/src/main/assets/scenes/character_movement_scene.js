// var appPriorityReporter = ...
// var touchEventsRepository = ...
// var scene = ...
// var soundClipsRepository = ...
// var soundScene = ...
// var soundScene2D = ...
// var displayMetricsRepository = ...
// var quaternionsPool = ...
// var vectorsPool = ...

var isPaused = false

var INITIAL_FORWARD_VECTOR = new Packages.org.joml.Vector3f(0, 0, -1);
var INITIAL_RIGHT_VECTOR = new Packages.org.joml.Vector3f(1, 0, 0);
var PLAYER_MOVEMENT_SPEED = 5; // unit/sec
var PLAYER_STEERING_SPEED = Math.PI; // rad/sec

var rootGestureConsumer;

var uiCamera;
var directionalLight;

var playerTransform;
var playerYAngle = 0;
var playerController;

var leftJoystick;
var leftJoystickHandleTransform;
var leftJoystickGestureConsumer;
var leftJoystickController;

var rightJoystick;
var rightJoystickHandleTransform;
var rightJoystickGestureConsumer;
var rightJoystickController;

var scrollController;

var pixelDensityFactor;
var displayWidth;
var displayHeight;

var xAngle = -Math.PI / 2;
var zAngle = -Math.PI / 4;

var keyboardTransform;
var keyboardGestureConsumer;
var keyboardClickDetector;
var keyboardYAngle = 0;
var keyboardClickSoundPlayer;
var KEYBOARD_ROTATION_SPEED = Math.PI; // rad/sec

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
    leftJoystickHandleTransform = scene.getTransformationComponent(
        findGameObject(leftJoystick, "left_joystick_handle")
    );
    leftJoystickController = new JoystickController(
        leftJoystickGestureConsumer,
        leftJoystickHandleTransform
    );
    rightJoystickHandleTransform = scene.getTransformationComponent(
        findGameObject(rightJoystick, "right_joystick_handle")
    );
    rightJoystickController = new JoystickController(
        rightJoystickGestureConsumer,
        rightJoystickHandleTransform
    );

    playerTransform = scene.getTransformationComponent(findGameObject(scene.rootGameObject, "player"));
    playerController = new PlayerController(leftJoystickController, rightJoystickController);

    var orthoCamera = scene.getOrthoCameraComponent(uiCamera);
    orthoCamera.left = 0;
    orthoCamera.right = displayWidth;
    orthoCamera.bottom = 0;
    orthoCamera.top = displayHeight;

    scene.getSoundPlayer3DComponent(findGameObject(scene.rootGameObject, "fountain_water")).play(true);

    var keyboard = findGameObject(scene.rootGameObject, "keyboard");
    keyboardTransform = scene.getTransformationComponent(keyboard);
    keyboardGestureConsumer = scene.getGestureConsumerComponent(keyboard);
    keyboardClickDetector = new ClickDetector(keyboardGestureConsumer);
    keyboardClickSoundPlayer = scene.getSoundPlayer2DComponent(keyboard);

    layoutUi()
}

function update(dt) {
    isPaused =
        appPriorityReporter.state != Packages.ilapin.opengl_research.domain.AppPriorityReporter.AppState.FOREGROUND;

    if (!isPaused) {
        if (soundScene.isPaused()) {
            soundScene.resume();
        }
        if (soundScene2D.isPaused()) {
            soundScene2D.resume();
        }

        keyboardClickDetector.update();
        scrollController.update();
        leftJoystickController.update();
        rightJoystickController.update();
        playerController.update();

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

        if (keyboardClickDetector.isClickDetected) {
            keyboardClickSoundPlayer.play(false);
        }

        println("!@# Player x: " + playerTransform.position.x() + "; y: " + playerTransform.position.y() + "; z: " + playerTransform.position.z());

        rotateKeyboard(dt);
        //movePlayer(dt);
    } else {
        if (!soundScene.isPaused()) {
            soundScene.pause();
        }
        if (!soundScene2D.isPaused()) {
            soundScene2D.pause();
        }
    }
}

function rotateKeyboard(dt) {
    var rotation = quaternionsPool.obtain();

    keyboardYAngle += KEYBOARD_ROTATION_SPEED * dt;
    rotation.identity().rotateY(keyboardYAngle).rotateZ(toRadians(10));
    keyboardTransform.rotation = rotation;

    quaternionsPool.recycle(rotation);
}

function movePlayer(dt) {
    var playerPosition = vectorsPool.obtain();
    var movingDirection = vectorsPool.obtain();
    var strafingDirection = vectorsPool.obtain();

    movingDirection.set(INITIAL_FORWARD_VECTOR);
    movingDirection.rotate(playerTransform.rotation);
    strafingDirection.set(INITIAL_RIGHT_VECTOR);
    strafingDirection.rotate(playerTransform.rotation);

    playerPosition.set(playerTransform.position);
    playerPosition.add(movingDirection.mul(PLAYER_MOVEMENT_SPEED).mul(playerController.movingFraction).mul(dt));
    playerPosition.add(strafingDirection.mul(PLAYER_MOVEMENT_SPEED).mul(playerController.strafingFraction).mul(dt));
    playerTransform.position = playerPosition;

    var playerRotation = quaternionsPool.obtain();
    playerYAngle += playerController.horizontalSteeringFraction * PLAYER_STEERING_SPEED * dt;
    playerRotation.identity().rotateY(playerYAngle);
    playerTransform.rotation = playerRotation;

    vectorsPool.recycle(playerPosition);
    vectorsPool.recycle(movingDirection);
    vectorsPool.recycle(strafingDirection);

    quaternionsPool.recycle(playerRotation);
}

function layoutUi() {
    layoutLeftJoystick();
    layoutRightJoystick();
    layoutRootGestureConsumer();
    layoutKeyboardGestureConsumer();
}

function layoutKeyboardGestureConsumer() {
    keyboardGestureConsumer.left = displayWidth * 0.75;
    keyboardGestureConsumer.top = displayHeight;
    keyboardGestureConsumer.right = displayWidth;
    keyboardGestureConsumer.bottom = displayHeight * 0.75;
}

function layoutLeftJoystick() {
    var position = vectorsPool.obtain();
    var scale = vectorsPool.obtain();

    var transform = scene.getTransformationComponent(leftJoystick);
    var backgroundTransform =
        scene.getTransformationComponent(findGameObject(leftJoystick, "left_joystick_background"));

    scale.set(leftJoystickHandleTransform.localScale);
    scale.mul(pixelDensityFactor);
    leftJoystickHandleTransform.scale = scale

    scale.set(backgroundTransform.localScale);
    scale.mul(pixelDensityFactor);
    backgroundTransform.scale = scale

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

    scale.set(rightJoystickHandleTransform.localScale);
    scale.mul(pixelDensityFactor);
    rightJoystickHandleTransform.scale = scale

    scale.set(backgroundTransform.localScale);
    scale.mul(pixelDensityFactor);
    backgroundTransform.scale = scale

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

