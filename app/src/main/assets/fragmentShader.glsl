precision mediump float;

varying vec4 varyingPosition;

void main() {
    float normalizedZ = (varyingPosition.z + 1.0) / 2.0;
    float overflow = normalizedZ - 1.0;
    gl_FragColor = vec4(overflow, overflow, overflow, 1.0);
}
