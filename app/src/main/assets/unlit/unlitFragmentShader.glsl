precision mediump float;

uniform sampler2D textureUniform;
uniform vec4 diffuseColorUniform;
uniform bool shouldUseDiffuseColorUniform;

varying vec2 uvVarying;

void main() {
    if (shouldUseDiffuseColorUniform) {
        gl_FragColor = diffuseColorUniform;
    } else {
        gl_FragColor = texture2D(textureUniform, uvVarying);
    }
}
