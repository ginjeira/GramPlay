package app.spotygram

data class Track(
    val id: String,
    val chatId: Long,
    val messageId: Long,
    val fileId: Int,
    val title: String,
    val artist: String,
    val duration: Int,
    val size: Long,
    val source: String,
    val date: Long,
    val art: String = "",
    val path: String = "",
    val liked: Boolean = false,
    val available: Boolean = true,
    val document: Boolean = false,
    val fileKey: String = "",
    val remoteId: String = "",
    val saved: Boolean = chatId == 0L,
    val temporary: Boolean = false,
    val profileUserId: Long = 0,
    val profilePosition: Int = -1,
    val inLibrary: Boolean = true,
) {
    val local: Boolean
        get() = path.isNotEmpty()

    val imported: Boolean
        get() = chatId == 0L && profileUserId == 0L

    val sourceKey: String
        get() = if (profileUserId != 0L) "profile:$profileUserId" else "chat:$chatId"

    // Keep cross-field phrases working without allocating unless the query spans fields.
    fun matches(query: String): Boolean =
        query.isEmpty() ||
            title.contains(query, ignoreCase = true) ||
            artist.contains(query, ignoreCase = true) ||
            source.contains(query, ignoreCase = true) ||
            (' ' in query && "$title $artist $source".contains(query, ignoreCase = true))
}

data class Playlist(val id: Long, val name: String, val tracks: List<String>)
