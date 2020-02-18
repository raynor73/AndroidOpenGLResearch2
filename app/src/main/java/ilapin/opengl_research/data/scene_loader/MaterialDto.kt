package ilapin.opengl_research.data.scene_loader

/**
 * @author raynor on 21.01.20.
 */
class MaterialDto(
    val id: String?,
    val textureName: String?,
    val diffuseColor: FloatArray?,
    val isDoubleSided: Boolean?,
    val isWireframe: Boolean?,
    val isUnlit: Boolean?,
    val isTranslucent: Boolean?,
    val castShadows: Boolean?,
    val receiveShadows: Boolean?
)