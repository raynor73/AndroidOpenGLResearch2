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

var pixelDensityFactor;
var displayWidth;
var displayHeight;

var uiCamera;

var fpsText;
var fpsTextTransform;
var fpsCalculator;

function start() {
    pixelDensityFactor = displayMetricsRepository.getPixelDensityFactor();
    displayWidth = displayMetricsRepository.displayWidth;
    displayHeight = displayMetricsRepository.displayHeight;

    uiCamera = findGameObject(scene.rootGameObject, "ui_camera");

    var fpsTextGameObject = findGameObject(scene.rootGameObject, "fps_text");
    fpsTextTransform = scene.getTransformationComponent(fpsTextGameObject);
    fpsText = scene.getTextComponent(fpsTextGameObject);
    fpsCalculator = new FpsCalculator();

    layoutUi()

    //scene.getSkeletalAnimatorComponent(findGameObject(scene.rootGameObject, "player")).start();
}

function update(dt) {
    updateFps(dt);
}

function layoutUi() {
    var orthoCamera = scene.getOrthoCameraComponent(uiCamera);
    orthoCamera.left = 0;
    orthoCamera.right = displayWidth;
    orthoCamera.bottom = 0;
    orthoCamera.top = displayHeight;

    layoutFpsText();
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

function updateFps(dt) {
    fpsCalculator.update(dt);
    fpsText.text = ("FPS: " + fpsCalculator.fps).substring(0, 10);
}
