package ilapin.opengl_research.data.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import ilapin.opengl_research.domain.sound.SoundClipsRepository

/**
 * @author raynor on 08.02.20.
 */
class SoundPoolSoundClipsRepository(
    private val context: Context
) : SoundClipsRepository {

    private val soundPool: SoundPool
    private val soundClips = HashMap<String, Int>()

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .build()
        soundPool = SoundPool.Builder()
            .setAudioAttributes(audioAttributes)
            .setMaxStreams(10)
            .build()
    }

    override fun loadSoundClip(name: String, path: String) {
        soundClips[name] = soundPool.load(context.assets.openFd(path), 1)
    }

    override fun playSoundClip(name: String, isLooped: Boolean): Int {
        return soundPool.play(
            soundClips[name] ?: error("Sound clip $name not found"),
            0f,
            0f,
            0,
            if (isLooped) 1 else 0,
            1f
        )
    }

    override fun stopSoundClip(streamId: Int) {
        soundPool.stop(streamId)
    }

    override fun changeSoundClipVolume(streamId: Int, leftVolume: Float, rightVolume: Float) {
        soundPool.setVolume(streamId, leftVolume, rightVolume)
    }
}