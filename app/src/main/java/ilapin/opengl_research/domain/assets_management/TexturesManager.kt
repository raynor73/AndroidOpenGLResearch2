package ilapin.opengl_research.domain.assets_management

/**
 * @author raynor on 18.02.20.
 */
interface TexturesManager {

    fun createTexture(name: String, path: String)

    fun createTexture(name: String, width: Int, height: Int, data: IntArray)
}