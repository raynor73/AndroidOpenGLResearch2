package ilapin.opengl_research

import org.joml.Vector3fc

/**
 * @author ilapin on 25.01.2020.
 */
class Mesh(
    vertexCoordinates: List<Vector3fc>,
    indices: List<Short>
) {
    private val _vertexCoordinates = ArrayList<Vector3fc>()
    private val _indices = ArrayList<Short>()

    val vertexCoordinates: List<Vector3fc> = _vertexCoordinates
    val indices: List<Short> = _indices

    init {
        _vertexCoordinates += vertexCoordinates
        _indices += indices
    }
}