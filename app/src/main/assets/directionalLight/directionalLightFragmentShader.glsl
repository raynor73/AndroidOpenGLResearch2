precision mediump float;

struct DirectionalLight {
    vec3 color;
    vec3 direction;
};

uniform DirectionalLight directionalLightUniform;
uniform sampler2D textureUniform;
uniform vec4 diffuseColorUniform;
uniform sampler2D shadowMapUniform;
uniform bool receiveShadows;
uniform bool useDiffuseColorUniform;

varying vec2 uvVarying;
varying vec4 shadowMapUvVariying;
varying vec3 normalVarying;

void calcColor(out vec4 resultColor);

void main() {
    if (receiveShadows) {
        if (shadowMapUvVariying.x < 0.0 || shadowMapUvVariying.x > 1.0 || shadowMapUvVariying.y < 0.0 || shadowMapUvVariying.y > 1.0) {
            calcColor(gl_FragColor);
        } else if (texture2D(shadowMapUniform, shadowMapUvVariying.xy).r + 0.001 < shadowMapUvVariying.z) {
            gl_FragColor = vec4(0.0, 0.0, 0.0, 0.0);
        } else {
            calcColor(gl_FragColor);
        }
    } else {
        calcColor(gl_FragColor);
    }
}

void calcColor(out vec4 resultColor) {
    if (useDiffuseColorUniform) {
        resultColor =
            diffuseColorUniform * vec4(directionalLightUniform.color, 1.0) *
            dot(normalize(normalVarying), -directionalLightUniform.direction);
    } else {
        resultColor =
            texture2D(textureUniform, uvVarying) * vec4(directionalLightUniform.color, 1.0) *
            dot(normalize(normalVarying), -directionalLightUniform.direction);
    }
}
