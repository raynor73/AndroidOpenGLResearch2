attribute vec3 vertexCoordinateAttribute;
attribute vec2 uvAttribute;

uniform mat4 mvpMatrixUniform;
uniform mat4 lightMvpMatrixUniform;
uniform mat4 biasMatrixUniform;

varying vec2 uvVarying;
varying vec4 shadowMapUvVariying;

void main() {
    shadowMapUvVariying = biasMatrixUniform * lightMvpMatrixUniform * vec4(vertexCoordinateAttribute, 1.0);
    uvVarying = uvAttribute;
    gl_Position = mvpMatrixUniform * vec4(vertexCoordinateAttribute, 1.0);
}
