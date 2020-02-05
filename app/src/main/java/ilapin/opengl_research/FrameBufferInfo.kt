package ilapin.opengl_research

/**
 * @author raynor on 31.01.20.
 */
sealed class FrameBufferInfo(val frameBuffer: Int) {
    class DepthFrameBufferInfo(frameBuffer: Int, val depthTextureInfo: TextureInfo) : FrameBufferInfo(frameBuffer)
    class RenderTargetFrameBufferInfo(frameBuffer: Int, val textureInfo: TextureInfo) : FrameBufferInfo(frameBuffer)
    object DisplayFrameBufferInfo : FrameBufferInfo(0)
}