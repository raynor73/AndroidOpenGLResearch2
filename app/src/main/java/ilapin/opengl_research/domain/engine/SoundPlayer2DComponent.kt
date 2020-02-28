package ilapin.opengl_research.domain.engine

import ilapin.engine3d.GameObject
import ilapin.engine3d.GameObjectComponent
import ilapin.opengl_research.domain.sound_2d.SoundScene2D

/**
 * @author raynor on 23.02.20.
 */
class SoundPlayer2DComponent(
    private val soundScene: SoundScene2D,
    val playerName: String,
    val soundClipName: String,
    val duration: Int,
    volume: Float
) : GameObjectComponent() {

    private var _volume = volume

    var volume: Float
        get() = _volume
        set(value) {
            _volume = volume
            soundScene.updateSoundPlayerVolume(playerName, value)
        }

    override var gameObject: GameObject?
        get() = super.gameObject
        set(value) {
            super.gameObject = value
            soundScene.addSoundPlayer(
                playerName,
                soundClipName,
                duration,
                volume
            )
        }

    override fun copy(): GameObjectComponent {
        return SoundPlayer2DComponent(soundScene, playerName + nextCopyPostfix(), soundClipName, duration, volume)
    }

    fun play(isLooped: Boolean) {
        soundScene.startSoundPlayer(playerName, isLooped)
    }

    fun pause() {
        soundScene.pauseSoundPlayer(playerName)
    }

    fun resume() {
        soundScene.resumeSoundPlayer(playerName)
    }

    fun stop() {
        soundScene.stopSoundPlayer(playerName)
    }

    override fun deinit() {
        soundScene.removeSoundPlayer(playerName)
    }
}