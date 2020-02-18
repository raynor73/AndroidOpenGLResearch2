package ilapin.opengl_research.data.scene_loader

/**
 * @author raynor on 21.01.20.
 */
class GameObjectDto(
    val parent: String?,
    val name: String?,
    val position: FloatArray?,
    val rotation: FloatArray?,
    val scale: FloatArray?,
    val components: List<ComponentDto>?
)