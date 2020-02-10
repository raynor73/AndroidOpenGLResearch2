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
                Vector3f(it.vertexCoordinates),
                Vector3f(it.normal),
                Vector2f(it.textureCoordinates)
            )
        }
        _indices += indices
    }

    class Vertex(
        vertexCoordinates: Vector3fc,
        normal: Vector3fc,
        textureCoordinates: Vector2fc
    ) {
        private val _vertexCoordinates = Vector3f(vertexCoordinates)
        private val _normal = Vector3f(normal)
        private val _textureCoordinates = Vector2f(textureCoordinates)

        val vertexCoordinates: Vector3fc
            get() = _vertexCoordinates

        val normal: Vector3fc
            get() = _normal

        val textureCoordinates: Vector2fc
            get() = _textureCoordinates
    }
}