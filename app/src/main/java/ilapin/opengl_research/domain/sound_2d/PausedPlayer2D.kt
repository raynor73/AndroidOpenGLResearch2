package ilapin.opengl_research.domain.sound_2d

/**
 * @author raynor on 22.02.20.
 */
class PausedPlayer2D(
    val activationTimestamp: Long,
    val pauseTimestamp: Long,
    val soundClipStreamId: Int,
    val soundPlayer: SoundPlayer2D,
    val isLooped: Boolean
) {
    fun toActive(currentTimestamp: Long): ActivePlayer2D {
        return ActivePlayer2D(
            currentTimestamp - (pauseTimestamp - activationTimestamp),
            soundClipStreamId, soundPlayer, isLooped
        )
    }
}