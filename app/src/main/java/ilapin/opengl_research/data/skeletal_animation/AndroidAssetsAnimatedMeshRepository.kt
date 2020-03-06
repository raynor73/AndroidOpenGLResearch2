package ilapin.opengl_research.data.skeletal_animation

import android.content.Context
import ilapin.opengl_research.domain.Mesh
import ilapin.opengl_research.domain.skeletal_animation.AnimatedMeshRepository
import ilapin.opengl_research.domain.skeletal_animation.SkeletalAnimationData
import org.w3c.dom.Element
import javax.xml.parsers.DocumentBuilderFactory

/**
 * @author ilapin on 06.03.20.
 */
class AndroidAssetsAnimatedMeshRepository(private val context: Context) : AnimatedMeshRepository {

    override fun loadMesh(path: String): Mesh {
        val documentBuilderFactory = DocumentBuilderFactory.newInstance()
        val documentBuilder = documentBuilderFactory.newDocumentBuilder()

        val inputStream = context.assets.open(path)
        val document = documentBuilder.parse(inputStream)
        inputStream.close()

        val rootElement = document.documentElement
        val geometryLibrary = rootElement.getElementsByTagName("library_geometries").item(0) as Element
        val geometry = geometryLibrary.getElementsByTagName("geometry") as Element
        val mesh = geometry.getElementsByTagName("mesh") as Element
    }

    override fun loadAnimation(path: String): SkeletalAnimationData {
        TODO("Not yet implemented")
    }
}