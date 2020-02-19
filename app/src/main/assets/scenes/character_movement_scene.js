// var touchEventsRepository = ...
// var scene = ...

var directionalLight;
var scrollController;

var tmpVector = new org.joml.Vector3f();
var tmpQuaternion = new org.joml.Quaternionf();

function start() {
    directionalLight = findGameObject(scene.rootGameObject, "directional_light");
    scrollController = new ScrollController();
}

function update(dt) {
    scrollController.update();

    var a = new org.joml.Vector3f();

    var transform = scene.getTransformationComponent(directionalLight)

    tmpQuaternion.set(transform.rotation)
    tmpQuaternion.rotateX(0.001)
    transform.rotation = tmpQuaternion

    if (scrollController.scrollEvent != null) {
        //println("dx: " + scrollController.scrollEvent.dx + "; dy: " + scrollController.scrollEvent.dy)
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
