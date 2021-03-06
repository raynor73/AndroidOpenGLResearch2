// var appPriorityReporter = ...
// var touchEventsRepository = ...
// var scene = ...
// var soundClipsRepository = ...
// var soundScene = ...
// var soundScene2D = ...
// var physicsEngine = ...
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

var playerRigidBody;
var playerYAngle = 0;
var playerMeshGameObject;
var playerMeshTransform;
var idlePlayerMeshGameObject;
var idlePlayerMeshTransform;
var playerController;

var leftJoystick;
var leftJoystickHandleTransform;
var leftJoystickGestureConsumer;
var leftJoystickController;

var rightJoystick;
var rightJoystickHandleTransform;
var rightJoystickGestureConsumer;
var rightJoystickController;

var buttonTransform;
var buttonGestureConsumer;
var buttonClickDetector;

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

var fireballEngine;

var fpsText;
var fpsTextTransform;
var fpsCalculator;

var platformAnimator;
var platformRigidBody;
var platformTransform;

var playerCollisionsVisualizer;

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

    playerRigidBody = scene.getRigidBodyComponent(findGameObject(scene.rootGameObject, "player"));
    playerMeshGameObject = findGameObject(scene.rootGameObject, "player_mesh");
    playerMeshTransform = scene.getTransformationComponent(playerMeshGameObject);
    playerController = new PlayerController(leftJoystickController, rightJoystickController);
    scene.getSkeletalAnimatorComponent(playerMeshGameObject).start();

    idlePlayerMeshGameObject = findGameObject(scene.rootGameObject, "player_mesh_idle");
    idlePlayerMeshTransform = scene.getTransformationComponent(idlePlayerMeshGameObject);
    scene.getSkeletalAnimatorComponent(idlePlayerMeshGameObject).start();

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

    var button = findGameObject(scene.rootGameObject, "button");
    buttonTransform = scene.getTransformationComponent(button);
    buttonGestureConsumer = scene.getGestureConsumerComponent(button);
    buttonClickDetector = new ClickDetector(buttonGestureConsumer);

    fireballEngine = new FireballEngine(findGameObject(scene.rootGameObject, "fireball_prefab"));

    var fpsTextGameObject = findGameObject(scene.rootGameObject, "fps_text");
    fpsTextTransform = scene.getTransformationComponent(fpsTextGameObject);
    fpsText = scene.getTextComponent(fpsTextGameObject);
    fpsCalculator = new FpsCalculator();

    var platformGameObject = findGameObject(scene.rootGameObject, "platform");
    platformTransform = scene.getTransformationComponent(platformGameObject);
    platformRigidBody = scene.getRigidBodyComponent(platformGameObject);
    platformAnimator = new Packages.ilapin.opengl_research.domain.animation.ValueAnimator(
        new Packages.ilapin.opengl_research.domain.animation.CycleInterpolator(1),
        0,
        10,
        15,
        0
    );
    platformAnimator.start();

    playerCollisionsVisualizer = new CollisionsVisualizer(
        findGameObject(scene.rootGameObject, "player"),
        findGameObject(scene.rootGameObject, "star_glow_prefab")
    );

    layoutUi();
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

        buttonClickDetector.update();
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

        playerCollisionsVisualizer.update();
        fireballEngine.update(dt);
        rotateKeyboard(dt);
        movePlayer(dt);
        performPlayerActions();
        updateFps(dt);
        updatePlatform(dt);
    } else {
        if (!soundScene.isPaused()) {
            soundScene.pause();
        }
        if (!soundScene2D.isPaused()) {
            soundScene2D.pause();
        }
    }
}

function updatePlatform(dt) {
    var position = vectorsPool.obtain();

    platformAnimator.update(dt);

    position.set(platformTransform.position);
    position.y = platformAnimator.value;
    platformRigidBody.setPosition(position);

    vectorsPool.recycle(position);
}

function rotateKeyboard(dt) {
    var rotation = quaternionsPool.obtain();

    keyboardYAngle += KEYBOARD_ROTATION_SPEED * dt;
    rotation.identity().rotateY(keyboardYAngle).rotateZ(toRadians(10));
    keyboardTransform.rotation = rotation;

    quaternionsPool.recycle(rotation);
}

function performPlayerActions() {
    if (buttonClickDetector.isClickDetected) {
        var direction = vectorsPool.obtain();
        var fireballStartPosition = vectorsPool.obtain();

        direction.set(INITIAL_FORWARD_VECTOR);
        direction.rotateY(playerYAngle);

        fireballStartPosition.set(playerMeshTransform.position);
        fireballStartPosition.y = 1.5;

        fireballEngine.castFireball(
            fireballStartPosition,
            direction
        );

        vectorsPool.recycle(direction);
        vectorsPool.recycle(fireballStartPosition);
    }
}

function movePlayer(dt) {
    var movingVelocity = vectorsPool.obtain();
    //var strafingVelocity = vectorsPool.obtain();
    var playerRotation = quaternionsPool.obtain();

    playerYAngle += playerController.horizontalSteeringFraction * PLAYER_STEERING_SPEED * dt;
    playerRotation.identity().rotateXYZ(Math.PI / 2, 0, Math.PI - playerYAngle);
    playerMeshTransform.rotation = playerRotation;
    idlePlayerMeshTransform.rotation = playerRotation;

    movingVelocity.set(INITIAL_FORWARD_VECTOR);
    movingVelocity.rotateY(playerYAngle);
    movingVelocity.mul(PLAYER_MOVEMENT_SPEED).mul(playerController.movingFraction);

    if (playerController.movingFraction > 0) {
        playerMeshGameObject.setEnabled(true);
        idlePlayerMeshGameObject.setEnabled(false);
    } else {
        playerMeshGameObject.setEnabled(false);
        idlePlayerMeshGameObject.setEnabled(true);
    }

    /*strafingVelocity.set(INITIAL_RIGHT_VECTOR);
    strafingVelocity.rotateY(playerYAngle);
    strafingVelocity.mul(PLAYER_MOVEMENT_SPEED).mul(playerController.strafingFraction);

    movingVelocity.add(strafingVelocity);*/

    playerRigidBody.setVelocityViaMotor(movingVelocity);

    vectorsPool.recycle(movingVelocity);
    //vectorsPool.recycle(strafingVelocity);
    quaternionsPool.recycle(playerRotation);
}

function layoutUi() {
    layoutLeftJoystick();
    layoutRightJoystick();
    layoutRootGestureConsumer();
    layoutKeyboardGestureConsumer();
    layoutButton();
    layoutFpsText();
}

function updateFps(dt) {
    fpsCalculator.update(dt);
    fpsText.text = ("FPS: " + fpsCalculator.fps).substring(0, 10);
}

function layoutFpsText() {
    var scale = vectorsPool.obtain();
    var position = vectorsPool.obtain();

    scale.set(fpsTextTransform.scale);
    scale.mul(pixelDensityFactor);
    scale.z = 1;
    fpsTextTransform.scale = scale;

    position.set(fpsTextTransform.position);
    position.x = scale.x / 2;
    position.y = displayHeight - scale.y / 2;
    fpsTextTransform.position = position;

    vectorsPool.recycle(position);
    vectorsPool.recycle(scale);
}

function layoutButton() {
    var position = vectorsPool.obtain();
    var scale = vectorsPool.obtain();
    var rightJoystickSize = vectorsPool.obtain();

    rightJoystickSize.set(
        scene.getTransformationComponent(findGameObject(rightJoystick, "right_joystick_background")).scale
    );

    scale.set(buttonTransform.localScale);
    scale.mul(pixelDensityFactor);
    buttonTransform.scale = scale

    position.set(buttonTransform.position);
    position.x += displayWidth - scale.x / 2;
    position.y += rightJoystickSize.z + scale.z / 2;
    buttonTransform.position = position;

    buttonGestureConsumer.left = displayWidth - scale.x;
    buttonGestureConsumer.top = rightJoystickSize.z + scale.z;
    buttonGestureConsumer.right = displayWidth;
    buttonGestureConsumer.bottom = rightJoystickSize.z;

    vectorsPool.recycle(position);
    vectorsPool.recycle(scale);
    vectorsPool.recycle(rightJoystickSize);
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
