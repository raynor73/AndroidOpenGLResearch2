attribute vec3 vertexCoordinateAttribute;

uniform mat4 mvpMatrixUniform;

void main() {
    gl_Position = mvpMatrixUniform * vec4(vertexCoordinateAttribute, 1.0);
}
