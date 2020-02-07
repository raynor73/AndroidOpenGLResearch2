precision mediump float;

uniform sampler2D textureUniform;
uniform vec4 diffuseColorUniform;
uniform bool useDiffuseColorUniform;

varying vec2 uvVarying;

void main() {
    if (useDiffuseColorUniform) {
        gl_FragColor = diffuseColorUniform;
    } else {
        gl_FragColor = texture2D(textureUniform, uvVarying);
    }
}
