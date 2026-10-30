package app.spotygram

import org.json.JSONObject

val Track.subtitle: String
    get() = artist.ifBlank {
        if (imported) tr(R.string.from_device) else sourceTitle
    }

val Track.sourceTitle: String
    get() =
        if (profileUserId != 0L) tr(R.string.profile_music_named, source)
        else chatTitle(chatId, source)

val Source.displayTitle: String
    get() = if (profile) title else chatTitle(id, title)

val ChatChoice.displayTitle: String
    get() = chatTitle(id, title)

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
    fun resendSeconds(): Int =
        ((resendAt - System.nanoTime() + 999_999_999L) / 1_000_000_000L).coerceAtLeast(0).toInt()

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

internal fun validAudioCopy(path: String, expectedSize: Long): Boolean {
    if (path.isEmpty()) return false
    val file = java.io.File(path)
    val length = file.length()
    return file.isFile && length > 0 && (expectedSize <= 0 || length == expectedSize)
}

fun json(type: String, vararg fields: Pair<String, Any?>) =
    JSONObject().put("@type", type).apply {
        fields.forEach { (k, v) -> put(k, v ?: JSONObject.NULL) }
    }

fun JSONObject.kind() = optString("@type")

fun seconds(value: Long): String {
    val positive = value.coerceAtLeast(0)
    val remainder = positive % 60
    return "${positive / 60}:${if (remainder < 10) "0" else ""}$remainder"
}

fun bytes(value: Long): String =
    if (value >= 1_073_741_824) tr(R.string.gigabytes).format(value / 1_073_741_824.0)
    else tr(R.string.megabytes).format(value / 1_048_576.0)

fun trackCount(value: Int): String = AppText.plural(R.plurals.track_count, value, value)
