package ilapin.opengl_research

import android.opengl.GLES20
import ilapin.opengl_research.domain.Mesh

fun Mesh.verticesAsArray(): FloatArray {
    val totalComponentsPerVertex = VERTEX_COORDINATE_COMPONENTS + TEXTURE_COORDINATE_COMPONENTS
    val vertexComponentsArray = FloatArray(vertices.size * totalComponentsPerVertex)
    for (i in vertices.indices) {
        vertexComponentsArray[0 + i * totalComponentsPerVertex] = vertices[i].vertexCoordinates.x()
        vertexComponentsArray[1 + i * totalComponentsPerVertex] = vertices[i].vertexCoordinates.y()
        vertexComponentsArray[2 + i * totalComponentsPerVertex] = vertices[i].vertexCoordinates.z()
        vertexComponentsArray[3 + i * totalComponentsPerVertex] = vertices[i].textureCoordinates.x()
        vertexComponentsArray[4 + i * totalComponentsPerVertex] = vertices[i].textureCoordinates.y()
    }
    return vertexComponentsArray
}

fun Int.glUniform1i(value: Int) {
    if (this > 0) {
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