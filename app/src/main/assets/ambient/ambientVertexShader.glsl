attribute vec3 vertexCoordinateAttribute;
attribute vec2 uvAttribute;

uniform mat4 mvpMatrixUniform;

varying vec2 uvVarying;

void main() {
    uvVarying = uvAttribute;
    gl_Position = mvpMatrixUniform * vec4(vertexCoordinateAttribute, 1.0);
}
