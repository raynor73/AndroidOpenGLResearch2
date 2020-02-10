package ilapin.opengl_research.domain

import org.joml.Vector2f
import org.joml.Vector2fc
import org.joml.Vector3f
import org.joml.Vector3fc

/**
 * @author ilapin on 25.01.2020.
 */
class Mesh(
    vertices: List<Vertex>,
    indices: List<Short>
) {
    private val _vertices = ArrayList<Vertex>()
    private val _indices = ArrayList<Short>()

    val vertices: List<Vertex> = _vertices
    val indices: List<Short> = _indices

    init {
        vertices.forEach {
            _vertices += Vertex(
                it.vertexCoordinates,
                it.normal,
                it.textureCoordinates,
                it.jointIndices,
                it.jointWeights
            )
        }
        _indices += indices
    }

    class Vertex(
        vertexCoordinates: Vector3fc,
        normal: Vector3fc,
        textureCoordinates: Vector2fc,
        jointIndices: List<Int>,
        jointWeights: List<Float>
    ) {
        private val _vertexCoordinates = Vector3f(vertexCoordinates)
        private val _normal = Vector3f(normal)
        private val _textureCoordinates = Vector2f(textureCoordinates)
        private val _jointIndices = ArrayList<Int>().apply { addAll(jointIndices) }
        private val _jointWeights = ArrayList<Float>().apply { addAll(jointWeights) }

        val vertexCoordinates: Vector3fc
            get() = _vertexCoordinates

        val normal: Vector3fc
            get() = _normal

        val textureCoordinates: Vector2fc
            get() = _textureCoordinates

        val jointIndices: List<Int>
            get() = _jointIndices

        val jointWeights: List<Float>
            get() = _jointWeights
    }
}