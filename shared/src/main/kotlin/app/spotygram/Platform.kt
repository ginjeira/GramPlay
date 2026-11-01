package app.spotygram

import java.io.File
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** The host owns OS facilities; screens, catalog and playback rules use the same source. */
interface Platform {
    val filesDir: File
    val cacheDir: File
    val fullOptics: Boolean
    val deviceModel: String
    val systemVersion: String
    val locale: Locale
    val releaseAbi: String
    val testDc: Boolean get() = false
    val wifi: Boolean
    val tdlib: TdClient

    fun preferences(name: String): Preferences
    fun string(id: Int, vararg arguments: Any?): String
    fun plural(id: Int, quantity: Int, vararg arguments: Any?): String
    fun openDatabase(
        name: String,
        version: Int,
        create: (SqlDatabase) -> Unit,
        upgrade: (SqlDatabase, Int, Int) -> Unit,
        opened: (SqlDatabase) -> Unit,
    ): SqlDatabase
    fun databaseKey(): String
    fun observeNetwork(onType: (String) -> Unit): AutoCloseable
    fun metadata(path: String, name: String = ""): AudioMetadata
    fun openUrl(url: String)
    fun personalMessageLink(user: Long, message: Long, account: Long): String
    fun languageSettings(app: SpotygramApp)
    fun startDownloads(app: SpotygramApp)
    fun stopDownloads(app: SpotygramApp)
    fun startAudioTransfer(app: SpotygramApp) {}
    fun installer(app: SpotygramApp): UpdateInstallation
    fun assetName(version: String, abi: String = releaseAbi): String
    fun logPlaybackError(code: String)
}

/** Posts after a player callback has settled, preserving Media3 timeline ordering. */
class MainTasks(private val scope: CoroutineScope) {
    private val jobs = mutableMapOf<Runnable, Job>()

    fun post(task: Runnable) = postDelayed(task, 0)
    fun postDelayed(task: Runnable, milliseconds: Long) {
        removeCallbacks(task)
        jobs[task] = scope.launch(Dispatchers.Main) {
            if (milliseconds > 0) delay(milliseconds)
            jobs.remove(task)
            task.run()
        }
    }
    fun removeCallbacks(task: Runnable) { jobs.remove(task)?.cancel() }
}
