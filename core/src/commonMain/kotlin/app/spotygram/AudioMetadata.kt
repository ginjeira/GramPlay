package app.spotygram

data class AudioMetadata(
    val duration: Long,
    val title: String = "",
    val artist: String = "",
    val artwork: ByteArray? = null,
)
