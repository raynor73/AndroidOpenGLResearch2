function CollisionsVisualizer(gameObject, prefab) {

    this.scene = scene;
    this.gameObject = gameObject;

    this.collisionIndicators = Array();
    this.prefabPool = new ObjectsPool(function() {
        return prefab.copy(null);
    });

    prefab.parent.detachChild(prefab);

    this.update = function() {
        var that = this;
        this.collisionIndicators.forEach(function(item, index, array) {
            item.parent.detachChild(item);
            that.prefabPool.recycle(item);
        });
        this.collisionIndicators.splice(0);

        var collisionsInfo = this.scene.getCollisionsInfoComponent(this.gameObject);
        if (collisionsInfo.collisions.size() > 0) {
            var that = this;
            collisionsInfo.collisions.forEach(function(item, index, array) {
                var collisionIndicator = that.prefabPool.obtain();
                scene.rootGameObject.addChild(collisionIndicator);
                scene.getTransformationComponent(collisionIndicator).position = item.position;
                that.collisionIndicators.push(collisionIndicator);
            });
        }
    };
}