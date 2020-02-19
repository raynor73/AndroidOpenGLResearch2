package ilapin.opengl_research.data.scene_loader

/**
 * @author raynor on 21.01.20.
 */
sealed class ComponentDto {
    class DirectionalLightDto(val color: FloatArray?) : ComponentDto()
    class MeshDto(val meshName: String?, val materialName: String?, val layerNames: List<String>?) : ComponentDto()
    class PerspectiveCameraDto(
        val fov: Float?,
        val layerNames: List<String>?,
        val ambientLight: FloatArray?
    ) : ComponentDto()
    class OrthoCameraDto(
        val left: Float?,
        val right: Float?,
        val top: Float?,
        val bottom: Float?,
        val layerNames: List<String>?,
        val ambientLight: FloatArray?
    ) : ComponentDto()
}