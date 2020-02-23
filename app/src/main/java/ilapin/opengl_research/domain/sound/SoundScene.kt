package ilapin.opengl_research.domain.sound

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

    private val activePlayers = HashMap<String, ActivePlayer>()
    private val pausedPlayers = HashMap<String, PausedPlayer>()
    private val permanentlyPausedPlayers = ArrayList<String>()

    private val playersToRemove = ArrayList<String>()

    private var isPaused = false

    fun updateSoundListenerPosition(position: Vector3fc) {
        soundListenerPosition.set(position)
    }

    fun updateSoundListenerRotation(rotation: Quaternionfc) {
        soundListenerRotation.set(rotation)
    }

    fun addSoundPlayer(
        playerName: String,
        soundClipName: String,
        duration: Int,
        position: Vector3fc,
        maxVolumeDistance: Float,
        minVolumeDistance: Float,
        volume: Float
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
            position,
            volume
        )
    }

    fun removeSoundPlayer(name: String) {
        if (!players.containsKey(name)) {
            error("Trying to add $name sound player multiple times")
        }

        val streamId = activePlayers[name]?.let {
            activePlayers.remove(name)
            it.soundClipStreamId
        } ?: run {
            pausedPlayers[name]?.let {
                pausedPlayers.remove(name)
                it.soundClipStreamId
            }
        } ?: error("No active or paused player $name found")

        soundClipsRepository.stopSoundClip(streamId)

        permanentlyPausedPlayers.remove(name)
        players.remove(name)
    }

    fun startSoundPlayer(name: String, isLooped: Boolean) {
        if (isPaused) {
            L.e(LOG_TAG, "SoundScene.startSoundPlayer() called but SoundScene is paused")
            return
        }

        if (activePlayers.containsKey(name)) {
            L.e(LOG_TAG, "SoundScene.startSoundPlayer() called but player is already started")
            return
        }

        val player = players[name] ?: error("Sound player $name not found")
        val levels = calculateVolumeLevels(
            soundListenerPosition,
            soundListenerRotation,
            player.position,
            player.minVolumeDistance,
            player.maxVolumeDistance
        )
        activePlayers[name] = ActivePlayer(
            timeRepository.getTimestamp(),
            soundClipsRepository.playSoundClip(player.soundClipName, levels.left, levels.right, isLooped),
            player,
            isLooped
        )
    }

    fun resumeSoundPlayer(name: String) {
        if (isPaused) {
            L.e(LOG_TAG, "SoundScene2D.resumeSoundPlayer() called but SoundScene2D is paused")
            return
        }

        if (activePlayers.containsKey(name)) {
            L.e(LOG_TAG, "SoundScene2D.resumeSoundPlayer() called but player is active")
            return
        }

        val activatedPlayer =
            (pausedPlayers[name] ?: error("Sound player $name not found")).toActive(timeRepository.getTimestamp())
        activePlayers[name] = activatedPlayer

        soundClipsRepository.resumeSoundClip(activatedPlayer.soundClipStreamId)
    }

    fun pauseSoundPlayer(name: String) {
        if (isPaused) {
            L.e(LOG_TAG, "SoundScene2D.pauseSoundPlayer() called but SoundScene2D is paused")
            return
        }

        if (pausedPlayers.containsKey(name)) {
            L.e(LOG_TAG, "SoundScene2D.pauseSoundPlayer() called but player is already paused")
            return
        }

        val pausedPlayer =
            (activePlayers[name] ?: error("Sound player $name not found")).toPaused(timeRepository.getTimestamp())
        activePlayers.remove(name)
        pausedPlayers[name] = pausedPlayer

        soundClipsRepository.pauseSoundClip(pausedPlayer.soundClipStreamId)
    }

    fun stopSoundPlayer(name: String) {
        val streamId = activePlayers[name]?.let {
            activePlayers.remove(name)
            it.soundClipStreamId
        } ?: run {
            pausedPlayers[name]?.let {
                pausedPlayers.remove(name)
                it.soundClipStreamId
            }
        } ?: error("No active or paused player $name found")

        soundClipsRepository.stopSoundClip(streamId)
    }

    fun updateSoundPlayerPosition(name: String, position: Vector3fc) {
        (players[name] ?: error("Sound player $name not found")).position = position
    }

    fun updateSoundPlayerVolume(name: String, volume: Float) {
        (players[name] ?: error("Sound player $name not found")).volume = volume
    }

    fun pause() {
        if (isPaused) {
        L.e(LOG_TAG, "SoundScene.pause() called but SoundScene is already paused")
        return
        }

        permanentlyPausedPlayers.clear()
        permanentlyPausedPlayers += pausedPlayers.keys

        activePlayers.values.forEach { player -> pauseSoundPlayer(player.soundPlayer.playerName) }

        isPaused = true
    }

    fun resume() {
        if (!isPaused) {
            L.e(LOG_TAG, "SoundScene.pause() called but SoundScene is not paused")
            return
        }

        pausedPlayers.values.forEach { player ->
            if (!permanentlyPausedPlayers.contains(player.soundPlayer.playerName)) {
                resumeSoundPlayer(player.soundPlayer.playerName)
            }
        }

        permanentlyPausedPlayers.clear()

        isPaused = false
    }

    fun clear() {
        (activePlayers.values.map { it.soundClipStreamId } + pausedPlayers.values.map { it.soundClipStreamId }).forEach {
            soundClipsRepository.stopSoundClip(it)
        }

        activePlayers.clear()
        pausedPlayers.clear()
        players.clear()

        isPaused = false
    }

    fun update() {
        playersToRemove.clear()

        val currentTimestamp = timeRepository.getTimestamp()
        activePlayers.forEach {
            if (
                !it.value.isLooped &&
                currentTimestamp + RESERVE_TIME - it.value.activationTimestamp > it.value.soundPlayer.duration * NANOS_IN_MILLISECOND
            ) {
                playersToRemove += it.key
            }
        }

        playersToRemove.forEach { playerName ->
            activePlayers.remove(playerName)?.soundClipStreamId?.let { soundClipsRepository.stopSoundClip(it) }
        }

        activePlayers.values.forEach { activePlayer ->
            val volumeLevels = calculateVolumeLevels(
                soundListenerPosition,
                soundListenerRotation,
                activePlayer.soundPlayer.position,
                activePlayer.soundPlayer.maxVolumeDistance,
                activePlayer.soundPlayer.minVolumeDistance
            )
            soundClipsRepository.changeSoundClipVolume(
                activePlayer.soundClipStreamId,
                volumeLevels.left * activePlayer.soundPlayer.volume,
                volumeLevels.right * activePlayer.soundPlayer.volume
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