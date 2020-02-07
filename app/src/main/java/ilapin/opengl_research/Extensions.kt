package ilapin.opengl_research

import android.opengl.GLES20
import ilapin.opengl_research.domain.Mesh
import org.joml.Vector2f
import org.joml.Vector3f

fun ilapin.engine3d.MeshComponent.toMesh(): Mesh {
    val convertedVertices = ArrayList<Mesh.Vertex>()
    val convertedIndices = ArrayList<Short>()

    vertices.forEachIndexed { i, vertexCoordinates ->
        convertedVertices += Mesh.Vertex(
            Vector3f(vertexCoordinates),
            Vector3f(normals[i]),
            Vector2f(uvs[i])
        )
    }

    convertedIndices += indices.map { it.toShort() }

    return Mesh(convertedVertices, convertedIndices)
}

fun Mesh.verticesAsArray(): FloatArray {
    val vertexComponentsArray = FloatArray(vertices.size * VERTEX_COMPONENTS)
    for (i in vertices.indices) {
        vertexComponentsArray[0 + i * VERTEX_COMPONENTS] = vertices[i].vertexCoordinates.x()
        vertexComponentsArray[1 + i * VERTEX_COMPONENTS] = vertices[i].vertexCoordinates.y()
        vertexComponentsArray[2 + i * VERTEX_COMPONENTS] = vertices[i].vertexCoordinates.z()
        vertexComponentsArray[3 + i * VERTEX_COMPONENTS] = vertices[i].normal.x()
        vertexComponentsArray[4 + i * VERTEX_COMPONENTS] = vertices[i].normal.y()
        vertexComponentsArray[5 + i * VERTEX_COMPONENTS] = vertices[i].normal.z()
        vertexComponentsArray[6 + i * VERTEX_COMPONENTS] = vertices[i].textureCoordinates.x()
        vertexComponentsArray[7 + i * VERTEX_COMPONENTS] = vertices[i].textureCoordinates.y()
    }
    return vertexComponentsArray
}

fun Int.glUniform1i(value: Int) {
    if (this >= 0) {
        GLES20.glUniform1i(this, value)
    }
}

fun Boolean.toGLBoolean(): Int {
    return if (this) {
        GLES20.GL_TRUE
    } else {
        GLES20.GL_FALSE
    }
}