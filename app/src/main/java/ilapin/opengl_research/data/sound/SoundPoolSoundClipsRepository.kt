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
        val fileDescriptor = context.assets.openFd(path)
        soundClips[name] = soundPool.load(fileDescriptor, 1)
        fileDescriptor.close()
        // TODO Replace this dirty workaround with proper async solution
        Thread.sleep(1000)
    }

    override fun playSoundClip(name: String, leftVolume: Float, rightVolume: Float, isLooped: Boolean): Int {
        return soundPool.play(
            soundClips[name] ?: error("Sound clip $name not found"),
            leftVolume,
            rightVolume,
            1,
            if (isLooped) -1 else 0,
            1f
        )
    }

    override fun pauseSoundClip(streamId: Int) {
        soundPool.pause(streamId)
    }

    override fun pauseAll() {
        soundPool.autoPause()
    }

    override fun resumeSoundClip(streamId: Int) {
        soundPool.resume(streamId)
    }

    override fun resumeAll() {
        soundPool.autoResume()
    }

    override fun stopSoundClip(streamId: Int) {
        soundPool.stop(streamId)
    }

    override fun changeSoundClipVolume(streamId: Int, leftVolume: Float, rightVolume: Float) {
        soundPool.setVolume(streamId, leftVolume, rightVolume)
    }

    override fun clear() {
        soundClips.values.forEach { soundPool.stop(it) }
        soundClips.clear()
    }
}