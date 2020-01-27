attribute vec3 vertexCoordinateAttribute;

uniform mat4 mvpMatrixUniform;

varying vec4 varyingPosition;

void main() {
    varyingPosition = mvpMatrixUniform * vec4(vertexCoordinateAttribute, 1.0);
    gl_Position = varyingPosition;
}
