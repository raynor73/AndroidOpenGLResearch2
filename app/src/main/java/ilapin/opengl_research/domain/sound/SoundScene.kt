package ilapin.opengl_research.domain.sound

import android.annotation.SuppressLint
import ilapin.common.android.log.L
import ilapin.common.math.inverseLerp
import ilapin.common.time.TimeRepository
import ilapin.opengl_research.NANOS_IN_MILLISECOND
import ilapin.opengl_research.ObjectsPool
import ilapin.opengl_research.app.App.Companion.LOG_TAG
import org.joml.Quaternionf
import org.joml.Quaternionfc
import org.joml.Vector3f
import org.joml.Vector3fc

/**
 * @author raynor on 07.02.20.
 */
class SoundScene(
    private val vectorsPool: ObjectsPool<Vector3f>,
    private val timeRepository: TimeRepository,
    private val soundClipsRepository: SoundClipsRepository
) {
    private val soundListenerPosition = Vector3f(0f, 0f, 0f)
    private val soundListenerRotation = Quaternionf().identity()

    private val players = HashMap<String, SoundPlayer>()
    @SuppressLint("UseSparseArrays")
    private val activePlayers = HashMap<String, ActivePlayer>()
    private val playersToRemove = ArrayList<String>()
    private val playersToUpdate = ArrayList<ActivePlayer>()

    private var isPaused = false

    fun updateSoundListenerPosition(position: Vector3fc) {
        if (isPaused) {
            L.e(LOG_TAG, "SoundScene.updateSoundListenerPosition() called but SoundScene is paused")
            return
        }

        soundListenerPosition.set(position)
        updateActivePlayersVolume()
    }

    fun updateSoundListenerRotation(rotation: Quaternionfc) {
        if (isPaused) {
            L.e(LOG_TAG, "SoundScene.updateSoundListenerRotation() called but SoundScene is paused")
            return
        }

        soundListenerRotation.set(rotation)
        updateActivePlayersVolume()
    }

    fun loadSoundClip(name: String, path: String) {
        if (isPaused) {
            L.e(LOG_TAG, "SoundScene.loadSoundClip() called but SoundScene is paused")
            return
        }

        soundClipsRepository.loadSoundClip(name, path)
    }

    fun addSoundPlayer(
        playerName: String,
        soundClipName: String,
        duration: Int,
        position: Vector3fc,
        maxVolumeDistance: Float,
        minVolumeDistance: Float
    ) {
        if (isPaused) {
            L.e(LOG_TAG, "SoundScene.addSoundPlayer() called but SoundScene is paused")
            return
        }

        if (players.containsKey(playerName)) {
            error("Trying to add $playerName sound player multiple times")
        }

        players[playerName] = SoundPlayer(
            playerName,
            soundClipName,
            duration,
            maxVolumeDistance,
            minVolumeDistance,
            position
        )
    }

    fun startSoundPlayer(name: String, isLooped: Boolean) {
        if (isPaused) {
            L.e(LOG_TAG, "SoundScene.startSoundPlayer() called but SoundScene is paused")
            return
        }

        val player = players[name] ?: error("Sound player $name not found")
        activePlayers[name] = ActivePlayer(
            timeRepository.getTimestamp(),
            soundClipsRepository.playSoundClip(player.soundClipName, isLooped),
            player,
            isLooped
        )

        updateActivePlayersVolume()
    }

    fun stopSoundPlayer(name: String) {
        val stoppingPlayer = activePlayers.remove(name) ?: error("Active player $name not found")
        soundClipsRepository.stopSoundClip(stoppingPlayer.soundClipStreamId)
    }

    fun updateSoundPlayerPosition(name: String, position: Vector3fc) {
        if (isPaused) {
            L.e(LOG_TAG, "SoundScene.updateSoundPlayerPosition() called but SoundScene is paused")
            return
        }

        (players[name] ?: error("Sound player $name not found")).position = position
        updateActivePlayersVolume()
    }

    fun pause() {
        if (isPaused) {
            L.e(LOG_TAG, "SoundScene.pause() called but SoundScene is already paused")
            return
        }

        activePlayers.values.forEach { player ->
            soundClipsRepository.pauseSoundClip(player.soundClipStreamId)
        }

        isPaused = true
    }

    fun resume() {
        if (!isPaused) {
            L.e(LOG_TAG, "SoundScene.pause() called but SoundScene is not paused")
            return
        }

        updateActivePlayersVolume()

        activePlayers.values.forEach { player ->
            soundClipsRepository.resumeSoundClip(player.soundClipStreamId)
        }

        isPaused = false
    }

    fun deinit() {
        activePlayers.values.forEach { player ->
            soundClipsRepository.stopSoundClip(player.soundClipStreamId)
        }

        activePlayers.clear()
    }

    private fun updateActivePlayersVolume() {
        playersToRemove.clear()
        playersToUpdate.clear()

        val currentTimestamp = timeRepository.getTimestamp()
        activePlayers.forEach {
            if (
                !it.value.isLooped &&
                currentTimestamp + RESERVE_TIME - it.value.activationTimestamp > it.value.soundPlayer.duration * NANOS_IN_MILLISECOND
            ) {
                playersToRemove += it.key
            } else {
                playersToUpdate += it.value
            }
        }

        playersToRemove.forEach { playerName ->
            activePlayers.remove(playerName)?.soundClipStreamId?.let { soundClipsRepository.stopSoundClip(it) }
        }

        playersToUpdate.forEach { activePlayer ->
            val volumeLevels = calculateVolumeLevels(
                soundListenerPosition,
                soundListenerRotation,
                activePlayer.soundPlayer.position,
                activePlayer.soundPlayer.maxVolumeDistance,
                activePlayer.soundPlayer.minVolumeDistance
            )
            soundClipsRepository.changeSoundClipVolume(
                activePlayer.soundClipStreamId,
                volumeLevels.left,
                volumeLevels.right
            )
        }
    }

    private fun calculateVolumeLevels(
        listenerPosition: Vector3fc,
        listenerRotation: Quaternionfc,
        playerPosition: Vector3fc,
        maxVolumeDistance: Float,
        minVolumeDistance: Float
    ): VolumeLevels {
        val directionToSound = vectorsPool.obtain()
        val rightDirection = vectorsPool.obtain()

        playerPosition.sub(listenerPosition, directionToSound)
        val distanceToSound = directionToSound.length()

        val distanceFactor = when {
            distanceToSound >= minVolumeDistance -> 0f
            distanceToSound <= maxVolumeDistance -> 1f
            else -> 1f - inverseLerp(maxVolumeDistance, minVolumeDistance, distanceToSound)
        }

        rightDirection.set(INITIAL_RIGHT_VECTOR)
        rightDirection.rotate(listenerRotation)

        val rightVolume = (directionToSound.angleCos(rightDirection) + 1f) / 2f
        val leftVolume = 1f - rightVolume

        vectorsPool.recycle(directionToSound)
        vectorsPool.recycle(rightDirection)

        return VolumeLevels(leftVolume * distanceFactor, rightVolume * distanceFactor)
    }

    companion object {

        private val INITIAL_RIGHT_VECTOR: Vector3fc = Vector3f(1f, 0f, 0f)
        private const val RESERVE_TIME = 100 * NANOS_IN_MILLISECOND // ns
    }
}