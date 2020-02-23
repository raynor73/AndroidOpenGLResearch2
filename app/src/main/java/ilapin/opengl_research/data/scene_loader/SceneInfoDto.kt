package ilapin.opengl_research.data.scene_loader

/**
 * @author raynor on 21.01.20.
 */
class SceneInfoDto(
    val soundClips: List<SoundClipDto>?,
    val textures: List<TextureDto>?,
    val materials: List<MaterialDto>?,
    val meshes: List<MeshDto>?,
    val scene: SceneDto?
)