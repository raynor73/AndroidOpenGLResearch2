package ilapin.opengl_research

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

/*fun GameObject.getRendererComponent(): RendererComponent? {
    return getComponent(UnlitRendererComponent::class.java) ?:
    getComponent(DepthVisualizationRendererComponent::class.java)
}*/

/*fun GameObject.findAllRendererComponents(dest: LinkedList<RendererComponent>) {
    getRendererComponent()?.let { dest += it }
    children.forEach { it.findAllRendererComponents(dest) }
}*/