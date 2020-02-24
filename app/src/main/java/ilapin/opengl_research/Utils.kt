package ilapin.opengl_research

import android.opengl.GLES20

/**
 * @author raynor on 24.02.20.
 */
fun glViewportAndScissor(x: Int, y: Int, width: Int, height: Int) {
    GLES20.glViewport(x, y, width, height)
    GLES20.glScissor(x, y, width, height)
}
