package ilapin.opengl_research.data.scene_loader

/**
 * @author raynor on 21.01.20.
 */
sealed class ComponentDto {
    class DirectionalLightDto(val color: FloatArray?, val layerNames: List<String>?) : ComponentDto()

    class MeshDto(val meshName: String?, val materialName: String?, val layerNames: List<String>?) : ComponentDto()

    class PerspectiveCameraDto(
        val fov: Float?,
        val layerNames: List<String>?,
        val ambientLight: FloatArray?,
        val zNear: Float?,
        val zFar: Float?,
        val viewportX: Float?,
        val viewportY: Float?,
        val viewportWidth: Float?,
        val viewportHeight: Float?
    ) : ComponentDto()

    class OrthoCameraDto(
        val left: Float?,
        val right: Float?,
        val top: Float?,
        val bottom: Float?,
        val layerNames: List<String>?,
        val ambientLight: FloatArray?,
        val zNear: Float?,
        val zFar: Float?,
        val viewportX: Float?,
        val viewportY: Float?,
        val viewportWidth: Float?,
        val viewportHeight: Float?
    ) : ComponentDto()

    class GestureConsumerDto(
        val priority: Int?,
        val left: Int?,
        val top: Int?,
        val right: Int?,
        val bottom: Int?
    ) : ComponentDto()

    class SoundPlayer3DDto(
        val playerName: String?,
        val soundClipName: String?,
        val duration: Int?,
        val maxVolumeDistance: Float?,
        val minVolumeDistance: Float?,
        val volume: Float?
    ) : ComponentDto()

    class SoundPlayer2DDto(
        val playerName: String?,
        val soundClipName: String?,
        val duration: Int?,
        val volume: Float?
    ) : ComponentDto()

    @Suppress("CanSealedSubClassBeObject")
    class SoundListenerDto : ComponentDto()
}