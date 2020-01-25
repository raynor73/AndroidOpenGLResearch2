attribute vec3 vertexCoordinateAttribute;

uniform mat4 mvpMatrixUniform;
uniform mat4 mvMatrixUniform;

varying vec4 varyingPosition;

void main() {
    varyingPosition = mvMatrixUniform * vec4(vertexCoordinateAttribute, 1.0);
    gl_Position = mvpMatrixUniform * vec4(vertexCoordinateAttribute, 1.0);
}
