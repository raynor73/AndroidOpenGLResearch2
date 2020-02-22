package ilapin.opengl_research.domain.sound_2d

/**
 * @author raynor on 22.02.20.
 */
class ActivePlayer2D(
    val activationTimestamp: Long,
    val soundClipStreamId: Int,
    val soundPlayer: SoundPlayer2D,
    val isLooped: Boolean
) {
    fun toPaused(currentTimestamp: Long): PausedPlayer2D {
        return PausedPlayer2D(activationTimestamp, currentTimestamp, soundClipStreamId, soundPlayer, isLooped)
    }
}