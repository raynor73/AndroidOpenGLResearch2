package ilapin.opengl_research.domain.assets_management

/**
 * @author raynor on 18.02.20.
 */
interface GeometryManager {

    fun createStaticVerticesBuffer(name: String, verticesData: FloatArray)

    fun createStaticIndicesBuffer(name: String, indices: ShortArray)
}