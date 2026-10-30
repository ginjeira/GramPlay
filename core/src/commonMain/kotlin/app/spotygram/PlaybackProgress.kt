package app.spotygram

data class PlaybackProgress(
    val position: Long,
    val duration: Long,
    val fileKey: String,
    val size: Long,
)
