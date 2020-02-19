// var touchEventsRepository = ...
// var scene = ...
// var displayMetricsRepository = ...
// var quaternionsPool = ...
// var vectorsPool = ...

var directionalLight;
var scrollController;

var pixelDensityFactor;

var xAngle = -Math.PI / 2;
var zAngle = -Math.PI / 4;

function start() {
    pixelDensityFactor = displayMetricsRepository.getPixelDensityFactor();

    directionalLight = findGameObject(scene.rootGameObject, "directional_light");
    scrollController = new ScrollController();
}

function update(dt) {
    scrollController.update();

    var scrollEvent = scrollController.scrollEvent;
    if (scrollEvent != null) {
        //println("dx: " + scrollController.scrollEvent.dx + "; dy: " + scrollController.scrollEvent.dy)
        var transform = scene.getTransformationComponent(directionalLight);

        zAngle -= toRadians(scrollEvent.dx / pixelDensityFactor)
        xAngle -= toRadians(scrollEvent.dy / pixelDensityFactor)

        var lightRotation = quaternionsPool.obtain()

        lightRotation.identity()
        lightRotation.rotateZ(zAngle).rotateX(xAngle)
        transform.rotation = lightRotation

        quaternionsPool.recycle(lightRotation)
    }
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

    this.prevTouchEvent = null

    this.scrollEvent = null

    this.update = function() {
        this.scrollEvent = null

        var touchEvent = null
        if (touchEventsRepository.touchEvents.size() > 0) {
            touchEvent = touchEventsRepository.touchEvents.get(0)
        }

        if (touchEvent != null && this.prevTouchEvent != null) {
            this.scrollEvent = new ScrollEvent(
                touchEvent.x - this.prevTouchEvent.x,
                touchEvent.y - this.prevTouchEvent.y
            )
        }
        this.prevTouchEvent = touchEvent
    };
}

function ScrollEvent(dx, dy) {
    this.dx = dx;
    this.dy = dy;
}
