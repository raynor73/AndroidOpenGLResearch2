package ilapin.opengl_research.data.skeletal_animation

import android.content.Context
import ilapin.collada_parser.data_structures.JointData
import ilapin.collada_parser.xml_parser.XmlNode
import ilapin.opengl_research.NORMAL_COMPONENTS
import ilapin.opengl_research.TEXTURE_COORDINATE_COMPONENTS
import ilapin.opengl_research.VERTEX_COORDINATE_COMPONENTS
import ilapin.opengl_research.domain.Mesh
import ilapin.opengl_research.domain.skeletal_animation.AnimatedMeshRepository
import ilapin.opengl_research.domain.skeletal_animation.Joint
import ilapin.opengl_research.domain.skeletal_animation.SkeletalAnimationData
import org.joml.*
import org.xml.sax.InputSource
import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
import javax.xml.xpath.XPathExpression
import javax.xml.xpath.XPathFactory

/**
 * @author ilapin on 06.03.20.
 */
class AndroidAssetsAnimatedMeshRepository(private val context: Context) : AnimatedMeshRepository {

    private val xPath = XPathFactory.newInstance().newXPath()

    override fun loadMesh(path: String): Mesh {
        val meshBytes = context.assets.open(path).run {
            val allBytes = readBytes()
            close()
            allBytes
        }

        val positions = parsePositions(meshBytes)
        val normals = parseNormals(meshBytes)
        val textureCoordinates = parseTextureCoordinates(meshBytes)
        val weights = parseWeights(meshBytes)
        val vertexSkinData = parseVertexSkinData(meshBytes, parseEffectiveJointCounts(meshBytes), weights)

        val polylistInputsCount = xPath
            .compile("count(/COLLADA/library_geometries[1]/geometry[1]/mesh[1]/polylist[1]/input)")
            .evaluateWithBytes(meshBytes)
            .toInt()

        val indexCounts = xPath
            .compile("/COLLADA/library_geometries[1]/geometry[1]/mesh[1]/polylist[1]/vcount[1]")
            .evaluateWithBytes(meshBytes)
            .split(" ")
            .mapNotNull { it.takeIf { countString -> countString.isNotBlank() }?.toInt() }

        if (indexCounts.any { it != 3 }) {
            error("Only triangles supported")
        }

        val colladaIndices = xPath
            .compile("/COLLADA/library_geometries[1]/geometry[1]/mesh[1]/polylist[1]/p[1]")
            .evaluateWithBytes(meshBytes)
            .split(" ")
            .map { it.toInt() }

        val vertices = ArrayList<Mesh.Vertex>()
        val indices = ArrayList<Short>()
        val indicesMap = HashMap<ComplexIndex, Short>()
        var currentIndex = 0.toShort()
        for (i in 0 until colladaIndices.size / polylistInputsCount) {
            val positionIndex = colladaIndices[i * polylistInputsCount]
            val normalIndex = colladaIndices[i * polylistInputsCount + 1]
            val textureCoordinateIndex = colladaIndices[i * polylistInputsCount + 2]

            val complexIndex = ComplexIndex(positionIndex, normalIndex, textureCoordinateIndex)
            val skinData = vertexSkinData[positionIndex]
            indicesMap[complexIndex]?.let { existingIndex ->
                indices.add(existingIndex)
            } ?: run {
                indices.add(currentIndex)
                vertices.add(Mesh.Vertex(
                    positions[positionIndex],
                    normals[normalIndex],
                    textureCoordinates[textureCoordinateIndex],
                    skinData.jointIds,
                    skinData.weights
                ))
                indicesMap[complexIndex] = currentIndex
                currentIndex++
            }
        }

        return Mesh(vertices, indices)
    }

    override fun loadAnimation(path: String): SkeletalAnimationData {
        val meshBytes = context.assets.open(path).run {
            val allBytes = readBytes()
            close()
            allBytes
        }

        val rootJointName = xPath
            .compile("/COLLADA/library_visual_scenes[1]/visual_scene[1]/node[@id='Armature']/node[1]/@id")
            .evaluateWithBytes(meshBytes)
        val jointNames = parseJointNames(meshBytes)
        val keyFrameTimes = parseKeyFrameTimes(meshBytes)
        val animationDuration = keyFrameTimes.last()

        //val keyFrames = parseKeyFrames(meshBytes, keyFrameTimes)

        error("Implementation is not completed")
        //return SkeletalAnimationData()
    }

    private fun parseJointWithChildrenData(meshBytes: ByteArray, nodeXPath: String, jointNames: List<String>): Joint {
        val joint = parseJointData(meshBytes, nodeXPath, jointNames)
        for (childNode in jointNode.getChildren("node")) {
            val childJoint = loadJointData(childNode, false)
            if (childJoint != null) {
                joint.addChild(childJoint)
            }
        }
        return joint
    }

    private fun parseJointData(meshBytes: ByteArray, nodeXPath: String, jointNames: List<String>): Joint {
        val jointName = xPath.compile("$nodeXPath/@id").evaluateWithBytes(meshBytes)
        val jointIndex = jointNames.indexOf(jointName).takeIf { it >= 0 } ?: error("Joint $jointName not found")
        val matrixData = xPath
            .compile("$nodeXPath/matrix[1]")
            .evaluateWithBytes(meshBytes)
            .split(" ")
        val matrix = matrixData.toMatrix()
        matrix.transpose()
        return Joint(jointIndex, jointName, matrix)
        /*val rootJointName = xPath
            .compile("/COLLADA/library_visual_scenes[1]/visual_scene[1]/node[@id='Armature']/node[1]/@id")
            .evaluateWithBytes(meshBytes)

        val rootJointIndex = jointNames.indexOf(rootJointName).takeIf { it >= 0 }
            ?: error("Root joint $rootJointName not found")

        val matrixData = xPath
            .compile("/COLLADA/library_visual_scenes[1]/visual_scene[1]/node[@id='Armature']/node[1]/matrix[1]")
            .evaluateWithBytes(meshBytes)
            .split(" ")

        val matrix = matrixData.toMatrix()
        matrix.transpose()
        return Joint(rootJointIndex, rootJointName, matrix)*/
    }

    private fun parseJointNames(meshBytes: ByteArray): List<String> {
        val namesContainerId = xPath
            .compile("/COLLADA/library_controllers[1]/controller[1]/skin[1]/vertex_weights[1]/input[@semantic='JOINT'][1]/@source")
            .evaluateWithBytes(meshBytes)
            .substring(1)

        val namesData = xPath
            .compile("/COLLADA/library_controllers[1]/controller[1]/skin[1]/source[@id='$namesContainerId']/Name_array[1]")
            .evaluateWithBytes(meshBytes)
            .split(" ")

        val namesDataCount = xPath
            .compile("/COLLADA/library_controllers[1]/controller[1]/skin[1]/source[@id='$namesContainerId']/Name_array[1]/@count")
            .evaluateWithBytes(meshBytes)
            .toInt()

        val names = ArrayList<String>()
        repeat(namesDataCount) { i ->
            names.add(namesData[i])
        }

        return names
    }

    private fun parseKeyFrameTimes(meshBytes: ByteArray): List<Float> {
        return xPath
            .compile("/COLLADA/library_animations[1]/animation[1]/source[1]/float_array[1]")
            .evaluateWithBytes(meshBytes)
            .split(" ")
            .map { it.toFloat() }
    }

    private fun parseEffectiveJointCounts(meshBytes: ByteArray): List<Int> {
        val jointCountsData = xPath
            .compile("/COLLADA/library_controllers[1]/controller[1]/skin[1]/vertex_weights[1]/vcount[1]")
            .evaluateWithBytes(meshBytes)
            .split(" ")
        return jointCountsData.mapNotNull { it.takeIf { countString -> countString.isNotBlank() }?.toInt() }
    }

    private fun parseVertexSkinData(
        meshBytes: ByteArray,
        effectiveJointCounts: List<Int>,
        weights: List<Float>
    ): List<VertexSkinData> {
        val rawData = xPath
            .compile("/COLLADA/library_controllers[1]/controller[1]/skin[1]/vertex_weights[1]/v[1]")
            .evaluateWithBytes(meshBytes)
            .split(" ")

        val skinData = ArrayList<VertexSkinData>()
        var pointer = 0
        effectiveJointCounts.forEach { effectiveJointCount ->
            val vertexSkinData = VertexSkinData()
            for (i in 0 until effectiveJointCount) {
                val jointId = rawData[pointer++].toInt()
                val weightId = rawData[pointer++].toInt()
                vertexSkinData.addJointEffect(jointId, weights[weightId])
            }
            vertexSkinData.limitJointNumber(MAX_WEIGHTS)
            skinData.add(vertexSkinData)
        }

        return skinData
    }

    private fun parseWeights(meshBytes: ByteArray): List<Float> {
        val weightsContainerId = xPath
            .compile("/COLLADA/library_controllers[1]/controller[1]/skin[1]/vertex_weights[1]/input[@semantic='WEIGHT'][1]/@source")
            .evaluateWithBytes(meshBytes)
            .substring(1)

        val weightsData = xPath
            .compile("/COLLADA/library_controllers[1]/controller[1]/skin[1]/source[@id='$weightsContainerId']/float_array[1]")
            .evaluateWithBytes(meshBytes)
            .split(" ")

        val weightsDataCount = xPath
            .compile("/COLLADA/library_controllers[1]/controller[1]/skin[1]/source[@id='$weightsContainerId']/float_array[1]/@count")
            .evaluateWithBytes(meshBytes)
            .toInt()

        val weights = ArrayList<Float>()
        repeat(weightsDataCount) { i ->
            weights.add(weightsData[i].toFloat())
        }

        return weights
    }

    private fun parseTextureCoordinates(meshBytes: ByteArray): List<Vector2fc> {
        val coordinatesContainerId = xPath
            .compile("/COLLADA/library_geometries[1]/geometry[1]/mesh[1]/polylist[1]/input[@semantic='TEXCOORD'][1]/@source")
            .evaluateWithBytes(meshBytes)
            .substring(1)

        val coordinatesData = xPath
            .compile("/COLLADA/library_geometries[1]/geometry[1]/mesh[1]/source[@id='$coordinatesContainerId']/float_array[1]")
            .evaluateWithBytes(meshBytes)
            .split(" ")

        val coordinatesDataCount = xPath
            .compile("/COLLADA/library_geometries[1]/geometry[1]/mesh[1]/source[@id='$coordinatesContainerId']/float_array[1]/@count")
            .evaluateWithBytes(meshBytes)
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

    private fun parseNormals(meshBytes: ByteArray): List<Vector3fc> {
        val normalsContainerId = xPath
            .compile("/COLLADA/library_geometries[1]/geometry[1]/mesh[1]/polylist[1]/input[@semantic='NORMAL'][1]/@source")
            .evaluateWithBytes(meshBytes)
            .substring(1)

        val normalsData = xPath
            .compile("/COLLADA/library_geometries[1]/geometry[1]/mesh[1]/source[@id='$normalsContainerId']/float_array[1]")
            .evaluateWithBytes(meshBytes)
            .split(" ")

        val normalsDataCount = xPath
            .compile("/COLLADA/library_geometries[1]/geometry[1]/mesh[1]/source[@id='$normalsContainerId']/float_array[1]/@count")
            .evaluateWithBytes(meshBytes)
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

    private fun parsePositions(meshBytes: ByteArray): List<Vector3fc> {
        val positionsId = xPath
            .compile("/COLLADA/library_geometries[1]/geometry[1]/mesh[1]/vertices[1]/input[1]/@source")
            .evaluateWithBytes(meshBytes)
            .substring(1)

        val positionsData = xPath
            .compile("/COLLADA/library_geometries[1]/geometry[1]/mesh[1]/source[@id='$positionsId']/float_array[1]")
            .evaluateWithBytes(meshBytes)
            .split(" ")

        val positionsDataCount = xPath
            .compile("/COLLADA/library_geometries[1]/geometry[1]/mesh[1]/source[@id='$positionsId']/float_array[1]/@count")
            .evaluateWithBytes(meshBytes)
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

    private fun XPathExpression.evaluateWithBytes(bytes: ByteArray): String {
        return evaluate(InputSource(ByteArrayInputStream(bytes)))
    }

    private fun XPathExpression.evaluateWithFile(path: String): String {
        val inputStream = BufferedInputStream(context.assets.open(path), 102400) // 100k buffer
        val result = evaluate(InputSource(inputStream))
        inputStream.close()
        return result
    }

    private fun List<String>.toMatrix(): Matrix4f {
        val matrixData = FloatArray(16)
        for (i in matrixData.indices) {
            matrixData[i] = get(i).toFloat()
        }
        return Matrix4f().apply { set(matrixData) }
    }

    private data class ComplexIndex(
        val vertexCoordinateIndex: Int,
        val normalIndex: Int,
        val textureCoordinateIndex: Int
    )

    private class VertexSkinData {

        private val _jointIds = ArrayList<Int>()
        private val _weights = ArrayList<Float>()

        val jointIds: List<Int> = _jointIds
        val weights: List<Float> = _weights

        fun addJointEffect(jointId: Int, weight: Float) {
            for (i in _weights.indices) {
                if (weight > _weights[i]) {
                    _jointIds.add(i, jointId)
                    _weights.add(i, weight)
                    return
                }
            }
            _jointIds.add(jointId)
            _weights.add(weight)
        }

        fun limitJointNumber(maxWeights: Int) {
            if (_weights.size > maxWeights) {
                refillWeightList(_weights.take(maxWeights))
                removeExcessJointIds(maxWeights)
            } else if (jointIds.size < maxWeights) {
                fillEmptyWeights(maxWeights)
            }
        }

        private fun refillWeightList(topWeights: List<Float>) {
            _weights.clear()
            val total = topWeights.sum()
            for (i in topWeights.indices) {
                _weights += topWeights[i] / total
            }
        }

        private fun removeExcessJointIds(maxWeights: Int) {
            val jointIdsOfTopWeights = _jointIds.take(maxWeights)
            _jointIds.clear()
            _jointIds += jointIdsOfTopWeights
        }

        private fun fillEmptyWeights(maxWeights: Int) {
            while (_weights.size < maxWeights) {
                _jointIds += 0
                _weights += 0f
            }
        }
    }

    companion object {

        private const val MAX_WEIGHTS = 3
    }
}