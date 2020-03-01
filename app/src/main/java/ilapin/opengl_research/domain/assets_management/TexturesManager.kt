package ilapin.opengl_research.domain.assets_management

import java.nio.Buffer

/**
 * @author raynor on 18.02.20.
 */
interface TexturesManager {

    fun createTexture(name: String, path: String)

    fun createTexture(name: String, width: Int, height: Int, data: IntArray)

    fun createTexture(name: String, width: Int, height: Int)

    fun copyDataToTexture(name: String, data: IntArray, generateMipmap: Boolean)

    fun copyDataToTexture(name: String, data: Buffer, generateMipmap: Boolean)

    fun removeTexture(name: String)

    fun removeAllTextures()
}