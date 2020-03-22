function ObjectsPool(createObject) {

    this.createObject = createObject;

    this.pool = Array();

    this.obtain = function() {
        if (this.pool.length == 0) {
            return this.createObject();
        } else {
            return this.pool.shift();
        }
    };

    this.recycle = function(object) {
        this.pool.push(object);
    };
}