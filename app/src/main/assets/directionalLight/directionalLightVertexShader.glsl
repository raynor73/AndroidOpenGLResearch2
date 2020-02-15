const int MAX_JOINTS = 50;
const int JOINTS_PER_VERTEX = 3;

attribute vec3 vertexCoordinateAttribute;
attribute vec3 normalAttribute;
attribute vec2 uvAttribute;
attribute vec3 jointIndicesAttribute;
attribute vec3 jointWeightsAttribute;

uniform mat4 mvpMatrixUniform;
uniform mat4 modelMatrixUniform;
uniform mat4 lightMvpMatrixUniform;
uniform mat4 biasMatrixUniform;
uniform mat4 jointTransformsUniform[MAX_JOINTS];
uniform bool hasSkeletalAnimationUniform;

varying vec2 uvVarying;
varying vec3 normalVarying;
varying vec4 shadowMapUvVariying;

void main() {
    uvVarying = uvAttribute;
    shadowMapUvVariying = biasMatrixUniform * lightMvpMatrixUniform * vec4(vertexCoordinateAttribute, 1.0);

    if (hasSkeletalAnimationUniform) {
        vec4 finalVertexCoordinate = vec4(0.0);
        vec4 finalNormal = vec4(0.0);

        for (int i = 0; i < JOINTS_PER_VERTEX; i++) {
            mat4 jointTransform = jointTransformsUniform[int(jointIndicesAttribute[i])];
            vec4 posedVertexCoordinate = jointTransform * vec4(vertexCoordinateAttribute, 1.0);
            finalVertexCoordinate += posedVertexCoordinate * jointWeightsAttribute[i];

            vec4 posedNormal = jointTransform * vec4(normalAttribute, 0.0);
            finalNormal += posedNormal * jointWeightsAttribute[i];
        }

        normalVarying = (modelMatrixUniform * finalNormal).xyz;
        gl_Position = mvpMatrixUniform * finalVertexCoordinate;
    } else {
        normalVarying = (modelMatrixUniform * vec4(normalAttribute, 0.0)).xyz;
        gl_Position = mvpMatrixUniform * vec4(vertexCoordinateAttribute, 1.0);
    }
}
