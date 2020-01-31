package ilapin.opengl_research

/**
 * @author raynor on 31.01.20.
 */
sealed class FrameBufferInfo {
    class DepthFrameBufferInfo(val frameBuffer: Int, val depthTextureInfo: TextureInfo) : FrameBufferInfo()
}