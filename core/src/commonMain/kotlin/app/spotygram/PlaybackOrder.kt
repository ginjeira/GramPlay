package app.spotygram

enum class PlaybackOrder {
    ORDERED,
    REVERSED,
    SHUFFLE,
    RANDOM;

    val shuffled: Boolean
        get() = this == SHUFFLE || this == RANDOM
}

data class PlaybackMode(
    val order: PlaybackOrder = PlaybackOrder.ORDERED,
    val repeat: Int = Player.REPEAT_MODE_OFF,
) {
    val shuffle: Boolean
        get() = order.shuffled

    val random: Boolean
        get() = order == PlaybackOrder.RANDOM
}

data class QueueSnapshot(
    val ids: List<String>,
    val order: List<Int>,
    val current: Int,
    val next: Int = -1,
)
