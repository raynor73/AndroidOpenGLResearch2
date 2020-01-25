precision mediump float;

varying vec4 varyingPosition;
//uniform vec3 color;

void main() {
    //gl_FragColor = vec4(color, 1.0);

    float normalizedDistance = varyingPosition.z / varyingPosition.w;
    // scale -1.0;1.0 to 0.0;1.0
    normalizedDistance = (normalizedDistance + 1.0) / 2.0;

    gl_FragColor = vec4(normalizedDistance, normalizedDistance, normalizedDistance, 1.0);
}
