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
        if (texture2D(shadowMapUniform, shadowMapUvVariying.xy).r + 0.001 < shadowMapUvVariying.z) {
            gl_FragColor = vec4(0.0, 0.0, 0.0, 0.0);
        } else {
            vec4 resultColor;
            // TODO Pass gl_FragColor directly as out parameter?
            calcColor(resultColor);
            gl_FragColor = resultColor;
        }
    } else {
        vec4 resultColor;
        calcColor(resultColor);
        gl_FragColor = resultColor;
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
