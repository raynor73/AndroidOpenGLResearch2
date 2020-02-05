precision mediump float;

uniform sampler2D textureUniform;
uniform vec4 diffuseColorUniform;
uniform bool shouldUseDiffuseColorUniform;

uniform vec3 ambientColorUniform;

varying vec2 uvVarying;

void main() {
    if (shouldUseDiffuseColorUniform) {
        gl_FragColor = diffuseColorUniform * vec4(ambientColorUniform, 1);
    } else {
        gl_FragColor = texture2D(textureUniform, uvVarying) * vec4(ambientColorUniform, 1);
    }
}
