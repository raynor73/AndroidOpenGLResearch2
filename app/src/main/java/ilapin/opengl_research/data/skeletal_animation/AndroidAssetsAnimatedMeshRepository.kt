package ilapin.opengl_research.data.skeletal_animation

import android.content.Context
import ilapin.opengl_research.NORMAL_COMPONENTS
import ilapin.opengl_research.TEXTURE_COORDINATE_COMPONENTS
import ilapin.opengl_research.VERTEX_COORDINATE_COMPONENTS
import ilapin.opengl_research.domain.Mesh
import ilapin.opengl_research.domain.skeletal_animation.AnimatedMeshRepository
import ilapin.opengl_research.domain.skeletal_animation.SkeletalAnimationData
import org.joml.Vector2f
import org.joml.Vector2fc
import org.joml.Vector3f
import org.joml.Vector3fc
import org.xml.sax.InputSource
import javax.xml.xpath.XPathExpression
import javax.xml.xpath.XPathFactory

/**
 * @author ilapin on 06.03.20.
 */
class AndroidAssetsAnimatedMeshRepository(private val context: Context) : AnimatedMeshRepository {

    private val xPath = XPathFactory.newInstance().newXPath()

    override fun loadMesh(path: String): Mesh {
        val positions = parsePositions(path)
        val normals = parseNormals(path)
        val textureCoordinates = parseTextureCoordinates(path)
        val weights = parseWeights(path)

        //val vertexSkinData =

        //"/COLLADA/library_controllers[1]/controller[1]/skin[1]/vertex_weights[1]/v[1]"
        //EffectiveJointsCounts
        //"/COLLADA/library_controllers[1]/controller[1]/skin[1]/vertex_weights[1]/vcount[1]"

        return Mesh(emptyList(), emptyList())
        /*val positionsId = mesh.getChild("vertices").getChild("input").getAttribute("source").substring(1)
        val positionsData: XmlNode =
            meshData.getChildWithAttribute("source", "id", positionsId).getChild("float_array")
        val count = positionsData.getAttribute("count").toInt()
        val posData = positionsData.data.split(" ").toTypedArray()
        for (i in 0 until count / 3) {
            val x = posData[i * 3].toFloat()
            val y = posData[i * 3 + 1].toFloat()
            val z = posData[i * 3 + 2].toFloat()
            val position = Vector4f(x, y, z, 1)
            vertices.add(
                Vertex(
                    vertices.size,
                    Vector3f(position.x, position.y, position.z),
                    vertexWeights.get(vertices.size)
                )
            )
        }*/
    }

    override fun loadAnimation(path: String): SkeletalAnimationData {
        TODO("Not yet implemented")
    }

    private fun parseWeights(path: String): List<Float> {
        val weightsContainerId = xPath
            .compile("/COLLADA/library_controllers[1]/controller[1]/skin[1]/vertex_weights[1]/input[@semantic='WEIGHT'][1]/@source")
            .evaluateWithFile(path)
            .substring(1)

        val weightsData = xPath
            .compile("/COLLADA/library_controllers[1]/controller[1]/skin[1]/source[@id='$weightsContainerId']/float_array[1]")
            .evaluateWithFile(path)
            .split(" ")

        val weightsDataCount = xPath
            .compile("/COLLADA/library_controllers[1]/controller[1]/skin[1]/source[@id='$weightsContainerId']/float_array[1]/@count")
            .evaluateWithFile(path)
            .toInt()

        val weights = ArrayList<Float>()
        repeat(weightsDataCount) { i ->
            weights.add(weightsData[i].toFloat())
        }

        return weights
    }

    private fun parseTextureCoordinates(path: String): List<Vector2fc> {
        val coordinatesContainerId = xPath
            .compile("/COLLADA/library_geometries[1]/geometry[1]/mesh[1]/polylist[1]/input[@semantic='TEXCOORD'][1]/@source")
            .evaluateWithFile(path)
            .substring(1)

        val coordinatesData = xPath
            .compile("/COLLADA/library_geometries[1]/geometry[1]/mesh[1]/source[@id='$coordinatesContainerId']/float_array[1]")
            .evaluateWithFile(path)
            .split(" ")

        val coordinatesDataCount = xPath
            .compile("/COLLADA/library_geometries[1]/geometry[1]/mesh[1]/source[@id='$coordinatesContainerId']/float_array[1]/@count")
            .evaluateWithFile(path)
            .toInt()

        val textureCoordinates = ArrayList<Vector2fc>()
        repeat(coordinatesDataCount / TEXTURE_COORDINATE_COMPONENTS) { i ->
            textureCoordinates.add(Vector2f(
                coordinatesData[i * TEXTURE_COORDINATE_COMPONENTS + 0].toFloat(),
                coordinatesData[i * TEXTURE_COORDINATE_COMPONENTS + 1].toFloat()
            ))
        }

        return textureCoordinates
    }

    private fun parseNormals(path: String): List<Vector3fc> {
        val normalsContainerId = xPath
            .compile("/COLLADA/library_geometries[1]/geometry[1]/mesh[1]/polylist[1]/input[@semantic='NORMAL'][1]/@source")
            .evaluateWithFile(path)
            .substring(1)

        val normalsData = xPath
            .compile("/COLLADA/library_geometries[1]/geometry[1]/mesh[1]/source[@id='$normalsContainerId']/float_array[1]")
            .evaluateWithFile(path)
            .split(" ")

        val normalsDataCount = xPath
            .compile("/COLLADA/library_geometries[1]/geometry[1]/mesh[1]/source[@id='$normalsContainerId']/float_array[1]/@count")
            .evaluateWithFile(path)
            .toInt()

        val normals = ArrayList<Vector3fc>()
        repeat(normalsDataCount / NORMAL_COMPONENTS) { i ->
            normals.add(Vector3f(
                normalsData[i * NORMAL_COMPONENTS + 0].toFloat(),
                normalsData[i * NORMAL_COMPONENTS + 1].toFloat(),
                normalsData[i * NORMAL_COMPONENTS + 2].toFloat()
            ))
        }

        return normals
    }

    private fun parsePositions(path: String): List<Vector3fc> {
        val positionsId = xPath
            .compile("/COLLADA/library_geometries[1]/geometry[1]/mesh[1]/vertices[1]/input[1]/@source")
            .evaluateWithFile(path)
            .substring(1)

        val positionsData = xPath
            .compile("/COLLADA/library_geometries[1]/geometry[1]/mesh[1]/source[@id='$positionsId']/float_array[1]")
            .evaluateWithFile(path)
            .split(" ")

        val positionsDataCount = xPath
            .compile("/COLLADA/library_geometries[1]/geometry[1]/mesh[1]/source[@id='$positionsId']/float_array[1]/@count")
            .evaluateWithFile(path)
            .toInt()

        val positions = ArrayList<Vector3fc>()
        repeat(positionsDataCount / VERTEX_COORDINATE_COMPONENTS) { i ->
            positions.add(Vector3f(
                positionsData[i * VERTEX_COORDINATE_COMPONENTS + 0].toFloat(),
                positionsData[i * VERTEX_COORDINATE_COMPONENTS + 1].toFloat(),
                positionsData[i * VERTEX_COORDINATE_COMPONENTS + 2].toFloat()
            ))
        }

        return positions
    }

    private fun XPathExpression.evaluateWithFile(path: String): String {
        val inputStream = context.assets.open(path)
        val result = evaluate(InputSource(inputStream))
        inputStream.close()
        return result
    }
}