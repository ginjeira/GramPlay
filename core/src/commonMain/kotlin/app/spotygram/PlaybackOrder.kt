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

fun playbackPermutation(
    size: Int,
    order: PlaybackOrder,
    nextInt: (Int) -> Int,
): List<Int> {
    if (size <= 0) return emptyList()
    if (order == PlaybackOrder.REVERSED) return (size - 1 downTo 0).toList()
    val result = (0 until size).toMutableList()
    if (order.shuffled)
        for (i in result.lastIndex downTo 1) {
            val j = nextInt(i + 1)
            val old = result[i]
            result[i] = result[j]
            result[j] = old
        }
    return result
}

fun normalizedRepeat(order: PlaybackOrder, repeat: Int): Int =
    if (order == PlaybackOrder.RANDOM && repeat == Player.REPEAT_MODE_ALL)
        Player.REPEAT_MODE_OFF
    else repeat
