package ilapin.opengl_research.domain.sound_2d

import ilapin.common.android.log.L
import ilapin.common.time.TimeRepository
import ilapin.opengl_research.NANOS_IN_MILLISECOND
import ilapin.opengl_research.app.App.Companion.LOG_TAG
import ilapin.opengl_research.domain.sound.SoundClipsRepository

/**
 * @author raynor on 21.02.20.
 */
class SoundScene2D(
    private val soundClipsRepository: SoundClipsRepository,
    private val timeRepository: TimeRepository
) {

    private val players = HashMap<String, SoundPlayer2D>()

    private val activePlayers = HashMap<String, ActivePlayer2D>()
    private val pausedPlayers = HashMap<String, PausedPlayer2D>()
    private val permanentlyPausedPlayers = ArrayList<String>()

    private val playersToRemove = ArrayList<String>()

    private var _isPaused = false

    val isPaused: Boolean
        get() = _isPaused

    fun addSoundPlayer(playerName: String, soundClipName: String, duration: Int, volume: Float) {
        if (_isPaused) {
            L.e(LOG_TAG, "SoundScene2D.addSoundPlayer() is called but SoundScene2D is paused")
            return
        }

        if (players.containsKey(playerName)) {
            error("Trying to add $playerName sound player multiple times")
        }

        players[playerName] = SoundPlayer2D(playerName, soundClipName, duration, volume)
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
        if (_isPaused) {
            L.e(LOG_TAG, "SoundScene2D.startSoundPlayer() called but SoundScene2D is paused")
            return
        }

        if (activePlayers.containsKey(name)) {
            L.e(LOG_TAG, "SoundScene2D.startSoundPlayer() called but player is already started")
            return
        }

        val player = players[name] ?: error("Sound player $name not found")
        activePlayers[name] = ActivePlayer2D(
            timeRepository.getTimestamp(),
            soundClipsRepository.playSoundClip(player.soundClipName, player.volume, player.volume, isLooped),
            player,
            isLooped
        )
    }

    fun resumeSoundPlayer(name: String) {
        if (_isPaused) {
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
        pausedPlayers.remove(name)

        soundClipsRepository.resumeSoundClip(activatedPlayer.soundClipStreamId)
    }

    fun pauseSoundPlayer(name: String) {
        if (_isPaused) {
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

    fun updateSoundPlayerVolume(name: String, volume: Float) {
        (players[name] ?: error("Sound player $name not found")).volume = volume

        activePlayers[name]?.let { activePlayer ->
            soundClipsRepository.changeSoundClipVolume(
                activePlayer.soundClipStreamId,
                volume, volume
            )
        }
    }

    fun pause() {
        if (_isPaused) {
            L.e(LOG_TAG, "SoundScene2D.pause() called but SoundScene2D is already paused")
            return
        }

        permanentlyPausedPlayers.clear()
        permanentlyPausedPlayers += pausedPlayers.keys

        activePlayers.values.forEach { player -> pauseSoundPlayer(player.soundPlayer.playerName) }

        _isPaused = true
    }

    fun resume() {
        if (!_isPaused) {
            L.e(LOG_TAG, "SoundScene2D.resume() called but SoundScene2D is not paused")
            return
        }

        _isPaused = false
        pausedPlayers.values.forEach { player ->
            if (!permanentlyPausedPlayers.contains(player.soundPlayer.playerName)) {
                resumeSoundPlayer(player.soundPlayer.playerName)
            }
        }

        permanentlyPausedPlayers.clear()
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
    }

    fun clear() {
        (activePlayers.values.map { it.soundClipStreamId } + pausedPlayers.values.map { it.soundClipStreamId }).forEach {
            soundClipsRepository.stopSoundClip(it)
        }

        activePlayers.clear()
        pausedPlayers.clear()
        players.clear()

        _isPaused = false
    }

    companion object {

        private const val RESERVE_TIME = 100 * NANOS_IN_MILLISECOND // ns
    }
}