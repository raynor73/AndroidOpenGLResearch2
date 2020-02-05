precision mediump float;

struct DirectionalLight {
    vec3 color;
    vec3 direction;
};

uniform DirectionalLight directionalLightUniform;
uniform sampler2D textureUniform;
uniform sampler2D shadowMapUniform;

varying vec2 uvVarying;
varying vec4 shadowMapUvVariying;
varying vec3 normalVarying;

void main() {
    if (texture2D(shadowMapUniform, shadowMapUvVariying.xy).r < shadowMapUvVariying.z) {
        gl_FragColor = vec4(0.0, 0.0, 0.0, 0.0);
    } else {
        gl_FragColor =
            texture2D(textureUniform, uvVarying) * vec4(directionalLightUniform.color, 1.0) *
            dot(normalize(normalVarying), -directionalLightUniform.direction);
    }
}
