package ilapin.opengl_research.domain.sound

import org.joml.Vector3f
import org.joml.Vector3fc

/**
 * @author raynor on 08.02.20.
 */
class SoundPlayer(
    val playerName: String,
    val soundClipName: String,
    val duration: Int,
    val maxVolumeDistance: Float,
    val minVolumeDistance: Float,
    position: Vector3fc
) {
    private val _position = Vector3f(position)

    var position: Vector3fc
        set(value) {
            _position.set(value)
        }
        get() {
            return _position
        }
}