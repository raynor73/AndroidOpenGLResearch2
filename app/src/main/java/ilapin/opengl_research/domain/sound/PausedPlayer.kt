package ilapin.opengl_research.domain.sound

/**
 * @author raynor on 22.02.20.
 */
class PausedPlayer(
    val activationTimestamp: Long,
    val pauseTimestamp: Long,
    val soundClipStreamId: Int,
    val soundPlayer: SoundPlayer,
    val isLooped: Boolean
) {
    fun toActive(currentTimestamp: Long): ActivePlayer {
        return ActivePlayer(
            currentTimestamp - (pauseTimestamp - activationTimestamp),
            soundClipStreamId, soundPlayer, isLooped
        )
    }
}