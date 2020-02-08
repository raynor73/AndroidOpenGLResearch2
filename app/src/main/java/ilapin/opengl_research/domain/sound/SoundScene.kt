package ilapin.opengl_research.domain.sound

import android.annotation.SuppressLint
import ilapin.common.math.inverseLerp
import ilapin.common.time.TimeRepository
import ilapin.opengl_research.NANOS_IN_MILLISECOND
import ilapin.opengl_research.ObjectsPool
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
    private val activePlayers = HashMap<Int, ActivePlayer>()
    private val playersToRemove = ArrayList<Int>()
    private val playersToUpdate = ArrayList<ActivePlayer>()

    fun updateSoundListenerPosition(position: Vector3fc) {
        soundListenerPosition.set(position)
        updateActivePlayersVolume()
    }

    fun updateSoundListenerRotation(rotation: Quaternionfc) {
        soundListenerRotation.set(rotation)
        updateActivePlayersVolume()
    }

    fun loadSoundClip(name: String, path: String) {
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

    fun startSoundPlayer(name: String, isLooped: Boolean): Int {
        val player = players[name] ?: error("Sound player $name not found")
        val streamId = soundClipsRepository.playSoundClip(player.soundClipName, isLooped)
        activePlayers[streamId] = ActivePlayer(
            timeRepository.getTimestamp(),
            streamId,
            player,
            isLooped
        )

        updateActivePlayersVolume()

        return streamId
    }

    fun stopSoundPlayer(streamId: Int) {
        activePlayers.remove(streamId) ?: error("Active player with stream ID $streamId not found")
        soundClipsRepository.stopSoundClip(streamId)
    }

    fun updateSoundPlayerPosition(name: String, position: Vector3fc) {
        (players[name] ?: error("Sound player $name not found")).position = position
        updateActivePlayersVolume()
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

        playersToRemove.forEach { activePlayers.remove(it) }

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