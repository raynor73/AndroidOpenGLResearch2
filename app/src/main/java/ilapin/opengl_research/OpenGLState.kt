package ilapin.opengl_research

/**
 * @author raynor on 24.02.20.
 */
class OpenGLState(
    val viewport: Viewport,
    val scissor: Scissor,
    val blend: Boolean,
    val blendFunction: BlendFunction,
    val depthMask: Boolean,
    val depthFunction: Int
) {
    class BlendFunction(
        val sFactor: Int,
        val dFactor: Int
    )
    class Viewport(
        val x: Int,
        val y: Int,
        val width: Int,
        val height: Int
    )
    class Scissor(
        val x: Int,
        val y: Int,
        val width: Int,
        val height: Int
    )
}