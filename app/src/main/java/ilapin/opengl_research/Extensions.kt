package ilapin.opengl_research

import ilapin.engine3d.GameObject
import java.util.*

fun Mesh.verticesAsArray(): FloatArray {
    val vertexComponentsArray = FloatArray(vertexCoordinates.size * VERTEX_COORDINATE_COMPONENTS)
    for (i in indices.indices) {
        vertexComponentsArray[0 + i * VERTEX_COORDINATE_COMPONENTS] = vertexCoordinates[i].x()
        vertexComponentsArray[1 + i * VERTEX_COORDINATE_COMPONENTS] = vertexCoordinates[i].y()
        vertexComponentsArray[2 + i * VERTEX_COORDINATE_COMPONENTS] = vertexCoordinates[i].z()
    }
    return vertexComponentsArray
}

/*fun GameObject.getRendererComponent(): RendererComponent? {
    return getComponent(UnlitRendererComponent::class.java) ?:
    getComponent(DepthVisualizationRendererComponent::class.java)
}*/

/*fun GameObject.findAllRendererComponents(dest: LinkedList<RendererComponent>) {
    getRendererComponent()?.let { dest += it }
    children.forEach { it.findAllRendererComponents(dest) }
}*/