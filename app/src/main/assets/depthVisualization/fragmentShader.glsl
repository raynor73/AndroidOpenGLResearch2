precision mediump float;

varying vec4 varyingPosition;

void main() {
    float normalizedZ = (varyingPosition.z / varyingPosition.w + 1.0) / 2.0;
    gl_FragColor = vec4(normalizedZ, normalizedZ, normalizedZ, 1.0);
}
