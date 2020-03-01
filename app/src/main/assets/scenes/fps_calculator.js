function FpsCalculator() {

    this.MAX_BUFFER_SIZE = 100;

    this.buffer = [];

    this.fps = 0;

    this.update = function(dt) {
        this.buffer.push(dt);
        if (this.buffer.length > this.MAX_BUFFER_SIZE) {
            this.buffer.shift();
        }

        this.fps = 0;
        var that = this;
        this.buffer.forEach(function(item, index, array) {
            that.fps += item;
        });
        this.fps /= this.buffer.length;
        this.fps = 1 / this.fps;
    };
}