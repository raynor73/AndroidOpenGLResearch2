function FireballEngine(prefab) {

    this.VELOCITY = 25;
    this.MAX_COORDINATE_VALUE = 50;

    this.fireballs = [];
    this.fireballIndexesToRemove = [];

    this.prefab = prefab;

    this.prefab.parent.detachChild(prefab);

    this.update = function(dt) {
        this.fireballIndexesToRemove.splice(0);

        var that = this;
        this.fireballs.forEach(function(item, index, array) {
            var position = vectorsPool.obtain();

            var transform = scene.getTransformationComponent(item.gameObject);

            position.set(item.direction).mul(that.VELOCITY * dt);
            position.add(transform.position);
            transform.position = position;

            if (
                Math.abs(transform.position.x()) > that.MAX_COORDINATE_VALUE ||
                Math.abs(transform.position.y()) > that.MAX_COORDINATE_VALUE ||
                Math.abs(transform.position.z()) > that.MAX_COORDINATE_VALUE
            ) {
                that.fireballIndexesToRemove.push(index);
                item.gameObject.parent.removeChild(item.gameObject);
            }

            vectorsPool.recycle(position);
        });


        var i = this.fireballs.length;
        while (i--) {
            if (this.fireballIndexesToRemove.includes(i)) {
                this.fireballs.splice(i, 1);
            }
        }
    };

    this.castFireball = function(position, direction) {
        var gameObject = this.prefab.copy(null);

        var transform = scene.getTransformationComponent(gameObject);
        transform.position = position;

        var fireball = new Fireball(gameObject, direction);
        scene.rootGameObject.addChild(fireball.gameObject);
        this.fireballs.push(fireball);
    };
}

function Fireball(gameObject, direction) {

    this.gameObject = gameObject;

    this.direction = new Packages.org.joml.Vector3f(direction);
}