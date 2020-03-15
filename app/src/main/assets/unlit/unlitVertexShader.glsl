const int MAX_JOINTS = 64;
const int JOINTS_PER_VERTEX = 3;

attribute vec3 vertexCoordinateAttribute;
attribute vec2 uvAttribute;
attribute vec3 jointIndicesAttribute;
attribute vec3 jointWeightsAttribute;

uniform mat4 mvpMatrixUniform;
uniform mat4 jointTransformsUniform[MAX_JOINTS];
uniform bool hasSkeletalAnimationUniform;

varying vec2 uvVarying;

void main() {
    uvVarying = uvAttribute;

    if (hasSkeletalAnimationUniform) {
        vec4 finalVertexCoordinate = vec4(0.0);

        for (int i = 0; i < JOINTS_PER_VERTEX; i++) {
            mat4 jointTransform = jointTransformsUniform[int(jointIndicesAttribute[i])];
            vec4 posedVertexCoordinate = jointTransform * vec4(vertexCoordinateAttribute, 1.0);
            finalVertexCoordinate += posedVertexCoordinate * jointWeightsAttribute[i];
        }

        gl_Position = mvpMatrixUniform * finalVertexCoordinate;
    } else {
        gl_Position = mvpMatrixUniform * vec4(vertexCoordinateAttribute, 1.0);
    }
}
