package app.spotygram

import kotlin.math.roundToInt

const val MIN_PLAYBACK_SPEED = 0.3f
const val MAX_PLAYBACK_SPEED = 4f
const val PLAYBACK_SPEED_STEP = 0.05f

fun playbackSpeedStep(value: Float): Float =
    ((value / PLAYBACK_SPEED_STEP).roundToInt() * PLAYBACK_SPEED_STEP).coerceIn(
        MIN_PLAYBACK_SPEED,
        MAX_PLAYBACK_SPEED,
    )

data class Playing(
    val id: String = "",
    val playing: Boolean = false,
    val loading: Boolean = false,
    val position: Long = 0,
    val duration: Long = 0,
    val order: PlaybackOrder = PlaybackOrder.ORDERED,
    val repeat: Int = 0,
    val speed: Float = 1f,
    val volume: Float = 1f,
) {
    val shuffle: Boolean
        get() = order.shuffled

    val random: Boolean
        get() = order == PlaybackOrder.RANDOM
}
