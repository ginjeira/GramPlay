package app.spotygram

data class Source(
    val id: Long,
    val title: String,
    val cursor: Long = 0,
    val complete: Boolean = false,
    val documentCursor: Long = 0,
    val documentsComplete: Boolean = false,
    val newest: Long = 0,
    val documentNewest: Long = 0,
    val profile: Boolean = false,
) {
    val fullyIndexed: Boolean
        get() = complete && documentsComplete

    val key: String
        get() = if (profile) "profile:$id" else "chat:$id"
}

data class UserProfile(
    val id: Long,
    val title: String,
    val username: String = "",
    val photo: String = "",
    val selected: Boolean = false,
    val count: Int = 0,
    val loadedAt: Long = 0,
    val openedAt: Long = 0,
) {
    fun source() = Source(id, title, complete = true, documentsComplete = true, profile = true)
}

data class ChatChoice(val id: Long, val title: String)

data class FileState(
    val id: Int,
    val path: String,
    val size: Long,
    val downloaded: Long,
    val offset: Long,
    val prefix: Long,
    val complete: Boolean,
    val active: Boolean,
    val key: String = "",
    val remote: String = "",
)

data class DownloadState(val trackId: String = "", val progress: Float = 0f, val queued: Int = 0)
