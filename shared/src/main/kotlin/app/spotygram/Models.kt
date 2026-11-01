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

fun AuthState.resendSeconds(): Int = resendSeconds(System.nanoTime())

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
