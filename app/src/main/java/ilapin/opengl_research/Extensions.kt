package ilapin.opengl_research

import android.opengl.GLES20
import ilapin.collada_parser.data_structures.*
import ilapin.opengl_research.domain.Mesh
import ilapin.opengl_research.domain.skeletal_animation.Joint
import ilapin.opengl_research.domain.skeletal_animation.JointLocalTransform
import ilapin.opengl_research.domain.skeletal_animation.KeyFrame
import ilapin.opengl_research.domain.skeletal_animation.SkeletalAnimation
import org.joml.*
import org.ode4j.math.DQuaternion
import org.ode4j.math.DQuaternionC
import org.ode4j.math.DVector3
import org.ode4j.math.DVector3C

fun Vector4fc.toArgb(): Int {
    return ((get(3) * 255).toInt() shl 24) or
            ((get(0) * 255).toInt() shl 16) or
            ((get(1) * 255).toInt() shl 8) or
            (get(2) * 255).toInt()
}

fun FloatArray.toArgb(): Int {
    return ((get(3) * 255).toInt() shl 24) or
            ((get(0) * 255).toInt() shl 16) or
            ((get(1) * 255).toInt() shl 8) or
            (get(2) * 255).toInt()
}

fun FloatArray.toRgba(dest: Vector4f) {
    dest.x = get(0)
    dest.y = get(1)
    dest.z = get(2)
    dest.w = get(3)
}

fun FloatArray.toRgb(dest: Vector3f) {
    dest.x = get(0)
    dest.y = get(1)
    dest.z = get(2)
}

fun MeshData.toMesh(): Mesh {
    val meshVertices = ArrayList<Mesh.Vertex>()

    repeat(vertexCount) { i ->
        meshVertices += Mesh.Vertex(
            Vector3f(
                vertices[0 + i * VERTEX_COORDINATE_COMPONENTS],
                vertices[1 + i * VERTEX_COORDINATE_COMPONENTS],
                vertices[2 + i * VERTEX_COORDINATE_COMPONENTS]
            ),
            Vector3f(
                normals[0 + i * NORMAL_COMPONENTS],
                normals[1 + i * NORMAL_COMPONENTS],
                normals[2 + i * NORMAL_COMPONENTS]
            ),
            Vector2f(
                textureCoords[0 + i * TEXTURE_COORDINATE_COMPONENTS],
                textureCoords[1 + i * TEXTURE_COORDINATE_COMPONENTS]
            ),
            listOf(
                jointIds[0 + i * NUMBER_OF_JOINT_INDICES],
                jointIds[1 + i * NUMBER_OF_JOINT_INDICES],
                jointIds[2 + i * NUMBER_OF_JOINT_INDICES]
            ),
            listOf(
                vertexWeights[0 + i * NUMBER_OF_JOINT_INDICES],
                vertexWeights[1 + i * NUMBER_OF_JOINT_INDICES],
                vertexWeights[2 + i * NUMBER_OF_JOINT_INDICES]
            )
        )
    }

    return Mesh(meshVertices, indices.map { it.toShort() })
}

fun JointData.toJoint(): Joint {
    val joint = Joint(index, nameId, bindLocalTransform)

    children?.forEach { child -> joint.addChild(child.toJoint()) }

    return joint
}

fun KeyFrameData.toKeyFrame(): KeyFrame {
    val convertedJointTransforms = HashMap<String, JointLocalTransform>()
    val position = Vector3f()
    val rotation = Quaternionf()
    jointTransforms.forEach { jointTransformDto ->
        jointTransformDto.jointLocalTransform.getTranslation(position)
        jointTransformDto.jointLocalTransform.getNormalizedRotation(rotation)
        convertedJointTransforms[jointTransformDto.jointNameId] = JointLocalTransform(
            position, rotation
        )
    }
    return KeyFrame(time, convertedJointTransforms)
}

fun AnimationData.toSkeletalAnimation(): SkeletalAnimation {
    return SkeletalAnimation(lengthSeconds, keyFrames.map { it.toKeyFrame() })
}

fun ilapin.engine3d.MeshComponent.toMesh(): Mesh {
    val convertedVertices = ArrayList<Mesh.Vertex>()
    val convertedIndices = ArrayList<Short>()

    vertices.forEachIndexed { i, vertexCoordinates ->
        convertedVertices += Mesh.Vertex(
            Vector3f(vertexCoordinates),
            Vector3f(normals[i]),
            Vector2f(uvs[i]),
            listOf(0, 0, 0),
            listOf(0f, 0f, 0f)
        )
    }

    convertedIndices += indices.map { it.toShort() }

    return Mesh(convertedVertices, convertedIndices)
}

fun Mesh.vertexCoordinatesOnlyAsArray(): FloatArray {
    val vertexCoordinatesArray = FloatArray(vertices.size * VERTEX_COORDINATE_COMPONENTS)
    for (i in vertices.indices) {
        vertexCoordinatesArray[0 + i * VERTEX_COORDINATE_COMPONENTS] = vertices[i].vertexCoordinates.x()
        vertexCoordinatesArray[1 + i * VERTEX_COORDINATE_COMPONENTS] = vertices[i].vertexCoordinates.y()
        vertexCoordinatesArray[2 + i * VERTEX_COORDINATE_COMPONENTS] = vertices[i].vertexCoordinates.z()
    }
    return vertexCoordinatesArray
}

fun Mesh.cwConvertedIndices(): IntArray {
    val convertedIndices = IntArray(indices.size)
    for (i in 0 until indices.size / VERTICES_IN_TRIANGLE) {
        convertedIndices[0 + i * VERTICES_IN_TRIANGLE] = indices[2 + i * VERTICES_IN_TRIANGLE].toInt()
        convertedIndices[1 + i * VERTICES_IN_TRIANGLE] = indices[1 + i * VERTICES_IN_TRIANGLE].toInt()
        convertedIndices[2 + i * VERTICES_IN_TRIANGLE] = indices[0 + i * VERTICES_IN_TRIANGLE].toInt()
    }
    return convertedIndices
}

fun Mesh.verticesAsArray(): FloatArray {
    val vertexComponentsArray = FloatArray(vertices.size * VERTEX_COMPONENTS)
    for (i in vertices.indices) {
        vertexComponentsArray[ 0 + i * VERTEX_COMPONENTS] = vertices[i].vertexCoordinates.x()
        vertexComponentsArray[ 1 + i * VERTEX_COMPONENTS] = vertices[i].vertexCoordinates.y()
        vertexComponentsArray[ 2 + i * VERTEX_COMPONENTS] = vertices[i].vertexCoordinates.z()

        vertexComponentsArray[ 3 + i * VERTEX_COMPONENTS] = vertices[i].normal.x()
        vertexComponentsArray[ 4 + i * VERTEX_COMPONENTS] = vertices[i].normal.y()
        vertexComponentsArray[ 5 + i * VERTEX_COMPONENTS] = vertices[i].normal.z()

        vertexComponentsArray[ 6 + i * VERTEX_COMPONENTS] = vertices[i].textureCoordinates.x()
        vertexComponentsArray[ 7 + i * VERTEX_COMPONENTS] = vertices[i].textureCoordinates.y()

        vertexComponentsArray[ 8 + i * VERTEX_COMPONENTS] = vertices[i].jointIndices[0].toFloat()
        vertexComponentsArray[ 9 + i * VERTEX_COMPONENTS] = vertices[i].jointIndices[1].toFloat()
        vertexComponentsArray[10 + i * VERTEX_COMPONENTS] = vertices[i].jointIndices[2].toFloat()

        vertexComponentsArray[11 + i * VERTEX_COMPONENTS] = vertices[i].jointWeights[0]
        vertexComponentsArray[12 + i * VERTEX_COMPONENTS] = vertices[i].jointWeights[1]
        vertexComponentsArray[13 + i * VERTEX_COMPONENTS] = vertices[i].jointWeights[2]
    }
    return vertexComponentsArray
}

fun Mesh.applyScale(scale: Vector3fc): Mesh {
    val scaledVertices = ArrayList<Mesh.Vertex>()
    vertices.forEach { vertex ->
        val scaledVertexCoordinates = Vector3f()
        scaledVertexCoordinates.set(vertex.vertexCoordinates)
        scaledVertexCoordinates.mul(scale)
        scaledVertices += Mesh.Vertex(
            scaledVertexCoordinates,
            vertex.normal,
            vertex.textureCoordinates,
            vertex.jointIndices,
            vertex.jointWeights
        )
    }

    return Mesh(scaledVertices, indices)
}

fun Mesh.applyTransform(position: Vector3fc, rotation: Quaternionfc, scale: Vector3fc): Mesh {
    val transformedVertices = ArrayList<Mesh.Vertex>()
    val transformMatrix = Matrix4f()
        .identity()
        .translate(position)
        .rotate(rotation)
        .scale(scale)
    val transformedVertexCoordinates = Vector3f()
    val transformedNormal = Vector3f()
    vertices.forEach { vertex ->
        transformedVertices += Mesh.Vertex(
            transformMatrix.transformPosition(vertex.vertexCoordinates, transformedVertexCoordinates),
            transformMatrix.transformDirection(vertex.normal, transformedNormal),
            vertex.textureCoordinates,
            vertex.jointIndices,
            vertex.jointWeights
        )
    }

    return Mesh(transformedVertices, indices)
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

fun Vector3fc.toVector(): DVector3 {
    return DVector3(x().toDouble(), y().toDouble(), z().toDouble())
}

fun Vector3fc.toVector(dest: DVector3) {
    dest.set(x().toDouble(), y().toDouble(), z().toDouble())
}

fun DVector3C.toVector(): Vector3f {
    return Vector3f(get0().toFloat(), get1().toFloat(), get2().toFloat())
}

fun DVector3C.toVector(dest: Vector3f) {
    dest.set(get0().toFloat(), get1().toFloat(), get2().toFloat())
}

fun Quaternionf.toQuaternion(): DQuaternion {
    return DQuaternion(w.toDouble(), x.toDouble(), y.toDouble(), z.toDouble())
}

fun Quaternionfc.toQuaternion(): DQuaternion {
    return DQuaternion(w().toDouble(), x().toDouble(), y().toDouble(), z().toDouble())
}

fun Quaternionfc.toQuaternion(dest: DQuaternion) {
    dest.set(w().toDouble(), x().toDouble(), y().toDouble(), z().toDouble())
}

fun Quaternionf.toQuaternion(dest: DQuaternion) {
    dest.set(w.toDouble(), x.toDouble(), y.toDouble(), z.toDouble())
}

fun DQuaternionC.toQuaternion(): Quaternionf {
    return Quaternionf(
        get1().toFloat(),
        get2().toFloat(),
        get3().toFloat(),
        get0().toFloat()
    )
}
