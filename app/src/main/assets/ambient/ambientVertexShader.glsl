const int MAX_JOINTS = 50;
const int JOINTS_PER_VERTEX = 3;

attribute vec3 vertexCoordinateAttribute;
attribute vec2 uvAttribute;
attribute vec3 jointIndicesAttribute;
attribute vec3 jointWeightsAttribute;

uniform mat4 jointTransformsUniform[MAX_JOINTS];
uniform mat4 mvpMatrixUniform;
uniform bool hasSkeletalAnimationUniform;

varying vec2 uvVarying;

void main() {
    uvVarying = uvAttribute;

    if (hasSkeletalAnimationUniform) {
        vec4 finalVertexCoordinate = vec4(0.0);
        //vec4 finalNormal = vec4(0.0);

        for (int i = 0; i < JOINTS_PER_VERTEX; i++) {
            mat4 jointTransform = jointTransformsUniform[int(jointIndicesAttribute[i])];
            vec4 posedVertexCoordinate = jointTransform * vec4(vertexCoordinateAttribute, 1.0);
            finalVertexCoordinate += posedVertexCoordinate * jointWeightsAttribute[i];

            /*vec4 worldNormal = jointTransform * vec4(in_normal, 0.0);
            finalNormal += worldNormal * in_weights[i];*/
        }

        gl_Position = mvpMatrixUniform * finalVertexCoordinate;
    } else {
        gl_Position = mvpMatrixUniform * vec4(vertexCoordinateAttribute, 1.0);
    }
}
