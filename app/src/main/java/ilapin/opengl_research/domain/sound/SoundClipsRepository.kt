package ilapin.opengl_research.domain.sound

/**
 * @author raynor on 08.02.20.
 */
interface SoundClipsRepository {

    fun loadSoundClip(name: String, path: String)

    fun playSoundClip(name: String, isLooped: Boolean = false): Int

    fun stopSoundClip(streamId: Int)

    fun changeSoundClipVolume(streamId: Int, leftVolume: Float, rightVolume: Float)
}