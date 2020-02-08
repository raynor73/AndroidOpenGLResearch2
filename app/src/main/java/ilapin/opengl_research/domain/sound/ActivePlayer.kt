package ilapin.opengl_research.domain.sound

/**
 * @author raynor on 08.02.20.
 */
class ActivePlayer(
    val activationTimestamp: Long,
    val soundClipStreamId: Int,
    val soundPlayer: SoundPlayer,
    val isLooped: Boolean
)