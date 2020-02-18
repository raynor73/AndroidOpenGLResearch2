package ilapin.opengl_research.domain.scene_loader

/**
 * @author raynor on 21.01.20.
 */
interface SceneLoader {

    fun loadScene(path: String): SceneData
}