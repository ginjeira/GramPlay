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

data class AuthState(
    val type: String = "welcome",
    val detail: String = "",
    val busy: Boolean = false,
    val error: String = "",
    val editingPhone: Boolean = false,
    val phoneNumber: String = "",
    val nextCodeType: String = "",
    val resendAt: Long = 0,
    val codeLink: String = "",
    val codeLength: Int = 0,
) {
    fun resendSeconds(nowNanos: Long): Int =
        ((resendAt - nowNanos + 999_999_999L) / 1_000_000_000L).coerceAtLeast(0).toInt()

    val canChangePhone: Boolean
        get() =
            type in
                listOf(
                    "authorizationStateWaitCode",
                    "authorizationStateWaitPassword",
                    "authorizationStateWaitEmailAddress",
                    "authorizationStateWaitEmailCode",
                    "authorizationStateWaitRegistration",
                    "authorizationStateWaitPremiumPurchase",
                    "authorizationStateWaitOtherDeviceConfirmation",
                )
}

data class LibraryState(
    val tracks: List<Track> = emptyList(),
    val sources: List<Source> = emptyList(),
    val playlists: List<Playlist> = emptyList(),
    val profiles: List<UserProfile> = emptyList(),
) {
    val byId by lazy { tracks.associateBy { it.id } }
    val duplicates by lazy { TrackDuplicates(tracks) }
    val localGroups by lazy { tracks.filter { it.local && !it.temporary }.groupBy { it.path } }
    val localTracks by lazy { localView(tracks) }
    val localSize by lazy { localTracks.sumOf { it.size } }
    val localRecordingCount by lazy {
        localTracks.asSequence().map { duplicates.key(it.id) }.distinct().count()
    }
    val allSources by lazy { sources + profiles.filter { it.selected }.map { it.source() } }
    val sourceCounts by lazy {
        tracks.asSequence().filter { it.profileUserId == 0L }.groupingBy { it.chatId }.eachCount()
    }
    val playlistCounts by lazy {
        playlists.associate { playlist -> playlist.id to playlist.tracks.count { it in byId } }
    }

    fun localDisplay(track: Track): Track {
        val liked = localGroups[track.path]?.any { it.liked } ?: track.liked
        return if (liked == track.liked) track else track.copy(liked = liked)
    }

    // Message references remain intact. Only the local-file view groups their shared paths.
    fun localView(candidates: List<Track>): List<Track> {
        val seen = HashSet<String>()
        return candidates.mapNotNull { track ->
            if (track.local && !track.temporary && seen.add(track.path)) localDisplay(track)
            else null
        }
    }
}
