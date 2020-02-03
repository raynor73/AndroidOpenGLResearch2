precision mediump float;

uniform sampler2D textureUniform;
uniform sampler2D shadowMapUniform;

varying vec2 uvVarying;
varying vec4 shadowMapUvVariying;

void main() {
    float visibility = 1.0;
    if (texture2D(shadowMapUniform, shadowMapUvVariying.xy).r < shadowMapUvVariying.z) {
        visibility = 0.5;
    }

    gl_FragColor = visibility * texture2D(textureUniform, uvVarying);
}
