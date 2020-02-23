package ilapin.opengl_research.domain.sound

/**
 * @author raynor on 08.02.20.
 */
interface SoundClipsRepository {

    fun loadSoundClip(name: String, path: String)

    fun playSoundClip(name: String, leftVolume: Float, rightVolume: Float, isLooped: Boolean = false): Int

    fun pauseSoundClip(streamId: Int)

    fun pauseAll()

    fun resumeSoundClip(streamId: Int)

    fun resumeAll()

    fun stopSoundClip(streamId: Int)

    fun changeSoundClipVolume(streamId: Int, leftVolume: Float, rightVolume: Float)

    fun clear()
}