package ilapin.opengl_research.domain.engine

import ilapin.engine3d.GameObject
import ilapin.engine3d.GameObjectComponent
import ilapin.engine3d.TransformationComponent
import ilapin.opengl_research.domain.sound.SoundScene

/**
 * @author raynor on 23.02.20.
 */
class SoundPlayer3DComponent(
    val soundScene: SoundScene,
    val playerName: String,
    val soundClipName: String,
    val duration: Int,
    val maxVolumeDistance: Float,
    val minVolumeDistance: Float,
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
                gameObject?.getComponent(TransformationComponent::class.java)?.position ?: error("No transform"),
                maxVolumeDistance,
                minVolumeDistance,
                volume
            )
        }

    override fun update() {
        super.update()

        // TODO Do something with getComponent() ?: error() copy/paste code
        val transform = gameObject?.getComponent(TransformationComponent::class.java) ?: error("No transform")
        soundScene.updateSoundPlayerPosition(playerName, transform.position)
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