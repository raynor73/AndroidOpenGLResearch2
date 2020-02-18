package ilapin.opengl_research.domain.assets_management

/**
 * @author raynor on 18.02.20.
 */
interface GeometryManager {

    fun createStaticVertexBuffer(name: String, vertexData: FloatArray)

    fun createStaticIndexBuffer(name: String, indices: ShortArray)

    fun removeStaticVertexBuffer(name: String)

    fun removeStaticIndexBuffer(name: String)

    fun removeAllBuffers()
}