package ilapin.opengl_research.domain.skeletal_animation

import ilapin.opengl_research.domain.Mesh

/**
 * @author ilapin on 06.03.20.
 */
interface AnimatedMeshRepository {

    fun loadMesh(path: String): Mesh

    fun loadAnimation(path: String): SkeletalAnimationData
}