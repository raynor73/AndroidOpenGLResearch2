precision mediump float;

uniform sampler2D textureUniform;

varying vec2 uvVarying;

void main() {
    float colorComponent = texture2D(textureUniform, uvVarying).r;
    gl_FragColor = vec4(colorComponent, colorComponent, colorComponent, 1.0);
}
