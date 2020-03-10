package ilapin.opengl_research.data.skeletal_animation

import android.content.Context
import ilapin.collada_parser.BufferUtils
import ilapin.collada_parser.data_structures.JointTransformData
import ilapin.collada_parser.data_structures.KeyFrameData
import ilapin.common.android.log.L
import ilapin.opengl_research.NORMAL_COMPONENTS
import ilapin.opengl_research.TEXTURE_COORDINATE_COMPONENTS
import ilapin.opengl_research.VERTEX_COORDINATE_COMPONENTS
import ilapin.opengl_research.app.App.Companion.LOG_TAG
import ilapin.opengl_research.domain.Mesh
import ilapin.opengl_research.domain.skeletal_animation.*
import org.joml.*
import org.w3c.dom.Document
import org.w3c.dom.NodeList
import org.xml.sax.InputSource
import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
import javax.xml.namespace.QName
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathExpression
import javax.xml.xpath.XPathFactory

/**
 * @author ilapin on 06.03.20.
 */
class AndroidAssetsAnimatedMeshRepository(private val context: Context) : AnimatedMeshRepository {

    private val xPath = XPathFactory.newInstance().newXPath()

    override fun loadMesh(path: String): Mesh {
        val documentBuilderFactory = DocumentBuilderFactory.newInstance()
        val documentBuilder = documentBuilderFactory.newDocumentBuilder()
        val document = documentBuilder.parse(context.assets.open(path))

        val positions = parsePositions(document)
        val normals = parseNormals(document)
        val textureCoordinates = parseTextureCoordinates(document)
        val weights = parseWeights(document)
        val vertexSkinData = parseVertexSkinData(document, parseEffectiveJointCounts(document), weights)

        val polylistInputsCount =
            (xPath.compile("count(/COLLADA/library_geometries[1]/geometry[1]/mesh[1]/polylist[1]/input)")
                .evaluateWithDoc(document) as String).toInt()

        val indexCounts =
            (xPath.compile("/COLLADA/library_geometries[1]/geometry[1]/mesh[1]/polylist[1]/vcount[1]")
                .evaluateWithDoc(document) as String)
                .split(" ")
                .mapNotNull { it.takeIf { countString -> countString.isNotBlank() }?.toInt() }

        if (indexCounts.any { it != 3 }) {
            error("Only triangles supported")
        }

        val colladaIndices =
            (xPath.compile("/COLLADA/library_geometries[1]/geometry[1]/mesh[1]/polylist[1]/p[1]").evaluateWithDoc(document) as String)
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
        val documentBuilderFactory = DocumentBuilderFactory.newInstance()
        val documentBuilder = documentBuilderFactory.newDocumentBuilder()
        val document = documentBuilder.parse(context.assets.open(path))

        val rootJointName = xPath
            .compile("/COLLADA/library_visual_scenes[1]/visual_scene[1]/node[@id='Armature']/node[1]/@id")
            .evaluateWithDoc(document)
        val jointNames = parseJointNames(document)
        val keyFrameTimes = parseKeyFrameTimes(document)
        val animationDuration = keyFrameTimes.last()
        val rootJoint = parseJointWithChildrenData(
            document,
            "/COLLADA/library_visual_scenes[1]/visual_scene[1]/node[@id='Armature']/node[1]",
            jointNames
        ) ?: error("Root joint not found")
        val keyFrames = parseKeyFrames(document, keyFrameTimes)

        return SkeletalAnimationData(rootJoint, SkeletalAnimation(animationDuration, emptyList()))
    }

    private fun parseKeyFrames(xmlDoc: Document, keyFrameTimes: List<Float>): List<KeyFrame> {
        val keyFrameNodes = xPath.compile("/COLLADA/library_animations[1]/animation").evaluateWithDoc(xmlDoc, XPathConstants.NODESET) as NodeList

        val keyFrames = ArrayList<KeyFrame>()

        for (i in 0 until keyFrameNodes.length) {
            val jointName =
                (xPath.compile("/COLLADA/library_animations[1]/animation[$i]/channel[1]/@target").evaluate(xmlDoc) as String)
                    .split("/")[0]
            val jointDataId =
                (xPath.compile("/COLLADA/library_animations[1]/animation[$i]/sampler[1]/input[@semantic='OUTPUT'][1]/@source").evaluate(xmlDoc) as String)
                    .substring(1)
            val rawTransformData =
                (xPath.compile("/COLLADA/library_animations[1]/animation[$i]/source[@id='$jointDataId']/float_array[1]").evaluateWithDoc(xmlDoc) as String)
                    .split(" ")
                    .map { it.toFloat() }

            for (keyFrameIndex in keyFrameTimes.indices) {
                val matrixData = FloatArray(16)
                for (j in 0 until 16) {
                    matrixData[i] = rawTransformData[keyFrameIndex * 16 + j]
                }
                val transform = Matrix4f().apply { set(matrixData) }
                transform.transpose()
            }

            keyFrames += KeyFrame()
        }

        return keyFrames
    }

    /*private fun parseKeyFrame(jointName: String, rawData: Array<String>, keyFrames: Array<KeyFrameData>) {
        val buffer = BufferUtils.createFloatBuffer(16)
        val matrixData = FloatArray(16)
        for (i in keyFrames.indices) {
            for (j in 0..15) {
                matrixData[j] = rawData[i * 16 + j].toFloat()
            }
            buffer.clear()
            buffer.put(matrixData)
            buffer.flip()
            val transform = Matrix4f()
            transform.set(buffer)
            transform.transpose()
            if (root) {
                //because up axis in Blender is different to up axis in game
                //Matrix4f.mul(CORRECTION, transform, transform);
            }
            keyFrames[i].addJointTransform(JointTransformData(jointName, transform))
        }
    }*/

    private fun parseJointWithChildrenData(xmlDoc: Document, nodeXPath: String, jointNames: List<String>): Joint? {
        val joint = parseJointData(xmlDoc, nodeXPath, jointNames) ?: return null
        L.d(LOG_TAG, "Joint ${joint.name}")
        val children = xPath.compile("$nodeXPath/node").evaluateWithDoc(xmlDoc, XPathConstants.NODESET) as NodeList
        for (i in 0 until children.length) {
            val childNodeName = children.item(i).nodeName
            val childNodeId = children.item(i).attributes.getNamedItem("id").nodeValue
            L.d(LOG_TAG, "\tJoin child node: $childNodeName, id='$childNodeId'")
            parseJointWithChildrenData(xmlDoc, "$nodeXPath/$childNodeName[@id='$childNodeId']", jointNames)?.let {
                joint.addChild(it)
            }
        }
        return joint
    }

    private fun parseJointData(xmlDoc: Document, nodeXPath: String, jointNames: List<String>): Joint? {
        val jointName = xPath.compile("$nodeXPath/@id").evaluateWithDoc(xmlDoc) as String
        val jointIndex = jointNames.indexOf(jointName).takeIf { it >= 0 } ?: run {
            L.e(LOG_TAG, "Joint $jointName not found")
            return null
        }
        val matrixData = (xPath.compile("$nodeXPath/matrix[1]").evaluateWithDoc(xmlDoc) as String).split(" ")
        val matrix = matrixData.toMatrix()
        matrix.transpose()
        return Joint(jointIndex, jointName, matrix)
    }

    private fun parseJointNames(xmlDoc: Document): List<String> {
        val namesContainerId =
            (xPath.compile("/COLLADA/library_controllers[1]/controller[1]/skin[1]/vertex_weights[1]/input[@semantic='JOINT'][1]/@source").evaluateWithDoc(xmlDoc) as String)
                .substring(1)

        val namesData =
            (xPath.compile("/COLLADA/library_controllers[1]/controller[1]/skin[1]/source[@id='$namesContainerId']/Name_array[1]").evaluateWithDoc(xmlDoc) as String)
                .split(" ")

        val namesDataCount =
            (xPath.compile("/COLLADA/library_controllers[1]/controller[1]/skin[1]/source[@id='$namesContainerId']/Name_array[1]/@count").evaluateWithDoc(xmlDoc) as String)
                .toInt()

        val names = ArrayList<String>()
        repeat(namesDataCount) { i ->
            names.add(namesData[i])
        }

        return names
    }

    private fun parseKeyFrameTimes(xmlDoc: Document): List<Float> {
        return (xPath.compile("/COLLADA/library_animations[1]/animation[1]/source[1]/float_array[1]").evaluateWithDoc(xmlDoc) as String)
            .split(" ")
            .map { it.toFloat() }
    }

    private fun parseEffectiveJointCounts(xmlDoc: Document): List<Int> {
        val jointCountsData =
            (xPath.compile("/COLLADA/library_controllers[1]/controller[1]/skin[1]/vertex_weights[1]/vcount[1]").evaluateWithDoc(xmlDoc) as String)
                .split(" ")
        return jointCountsData.mapNotNull { it.takeIf { countString -> countString.isNotBlank() }?.toInt() }
    }

    private fun parseVertexSkinData(
        xmlDoc: Document,
        effectiveJointCounts: List<Int>,
        weights: List<Float>
    ): List<VertexSkinData> {
        val rawData =
            (xPath.compile("/COLLADA/library_controllers[1]/controller[1]/skin[1]/vertex_weights[1]/v[1]").evaluateWithDoc(xmlDoc) as String)
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

    private fun parseWeights(xmlDoc: Document): List<Float> {
        val weightsContainerId =
            (xPath.compile("/COLLADA/library_controllers[1]/controller[1]/skin[1]/vertex_weights[1]/input[@semantic='WEIGHT'][1]/@source").evaluateWithDoc(xmlDoc) as String)
                .substring(1)

        val weightsData =
            (xPath.compile("/COLLADA/library_controllers[1]/controller[1]/skin[1]/source[@id='$weightsContainerId']/float_array[1]").evaluateWithDoc(xmlDoc) as String)
                .split(" ")

        val weightsDataCount =
            (xPath.compile("/COLLADA/library_controllers[1]/controller[1]/skin[1]/source[@id='$weightsContainerId']/float_array[1]/@count").evaluateWithDoc(xmlDoc) as String)
                .toInt()

        val weights = ArrayList<Float>()
        repeat(weightsDataCount) { i ->
            weights.add(weightsData[i].toFloat())
        }

        return weights
    }

    private fun parseTextureCoordinates(xmlDoc: Document): List<Vector2fc> {
        val coordinatesContainerId =
            (xPath.compile("/COLLADA/library_geometries[1]/geometry[1]/mesh[1]/polylist[1]/input[@semantic='TEXCOORD'][1]/@source").evaluateWithDoc(xmlDoc) as String)
                .substring(1)

        val coordinatesData =
            (xPath.compile("/COLLADA/library_geometries[1]/geometry[1]/mesh[1]/source[@id='$coordinatesContainerId']/float_array[1]").evaluateWithDoc(xmlDoc) as String)
                .split(" ")

        val coordinatesDataCount =
            (xPath.compile("/COLLADA/library_geometries[1]/geometry[1]/mesh[1]/source[@id='$coordinatesContainerId']/float_array[1]/@count").evaluateWithDoc(xmlDoc) as String)
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

    private fun parseNormals(xmlDoc: Document): List<Vector3fc> {
        val normalsContainerId =
            (xPath.compile("/COLLADA/library_geometries[1]/geometry[1]/mesh[1]/polylist[1]/input[@semantic='NORMAL'][1]/@source").evaluateWithDoc(xmlDoc) as String)
                .substring(1)

        val normalsData =
            (xPath.compile("/COLLADA/library_geometries[1]/geometry[1]/mesh[1]/source[@id='$normalsContainerId']/float_array[1]").evaluateWithDoc(xmlDoc) as String)
                .split(" ")

        val normalsDataCount =
            (xPath.compile("/COLLADA/library_geometries[1]/geometry[1]/mesh[1]/source[@id='$normalsContainerId']/float_array[1]/@count").evaluateWithDoc(xmlDoc) as String)
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

    private fun parsePositions(xmlDoc: Document): List<Vector3fc> {
        val positionsId =
            (xPath.compile("/COLLADA/library_geometries[1]/geometry[1]/mesh[1]/vertices[1]/input[1]/@source").evaluateWithDoc(xmlDoc) as String)
                .substring(1)

        val positionsData =
            (xPath.compile("/COLLADA/library_geometries[1]/geometry[1]/mesh[1]/source[@id='$positionsId']/float_array[1]").evaluateWithDoc(xmlDoc) as String)
                .split(" ")

        val positionsDataCount =
            (xPath.compile("/COLLADA/library_geometries[1]/geometry[1]/mesh[1]/source[@id='$positionsId']/float_array[1]/@count").evaluateWithDoc(xmlDoc) as String)
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

    private fun XPathExpression.evaluateWithDoc(xmlDoc: Document, returnType: QName = XPathConstants.STRING): Any {
        return evaluate(xmlDoc, returnType)
    }

    private fun XPathExpression.evaluateWithBytes(bytes: ByteArray, returnType: QName = XPathConstants.STRING): Any {
        return evaluate(InputSource(ByteArrayInputStream(bytes)), returnType)
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