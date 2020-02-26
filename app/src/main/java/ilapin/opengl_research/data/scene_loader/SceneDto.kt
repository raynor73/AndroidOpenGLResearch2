package ilapin.opengl_research.data.scene_loader

/**
 * @author raynor on 18.02.20.
 */
class SceneDto(
    val gravity: List<Float>?,
    val scriptPaths: List<String>?,
    val activeCameras: List<String>?,
    val renderTargets: List<String>?,
    val gameObjects: List<GameObjectDto>?
)