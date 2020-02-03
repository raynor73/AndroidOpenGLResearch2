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
        vertices.forEach { _vertices += Vertex(
            Vector3f(it.vertexCoordinates),
            Vector2f(it.textureCoordinates)
        )
        }
        _indices += indices
    }

    class Vertex(val vertexCoordinates: Vector3fc, val textureCoordinates: Vector2fc)
}