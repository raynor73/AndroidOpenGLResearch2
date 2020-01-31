precision mediump float;

uniform sampler2D textureUniform;

varying vec2 uvVarying;

void main() {
    gl_FragColor = texture2D(textureUniform, uvVarying);
    /*float colorComponent = texture2D(textureUniform, uvVarying).z;
    gl_FragColor = vec4(colorComponent, colorComponent, colorComponent, 1.0);*/
}
