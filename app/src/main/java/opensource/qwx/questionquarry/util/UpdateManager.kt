package opensource.qwx.questionquarry.util

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import opensource.qwx.questionquarry.BuildConfig
import org.json.JSONObject
import java.io.File

data class UpdateInfo(
    val version: String,
    val downloadUrl: String,
    val releaseNotes: String,
)

class UpdateManager(private val context: Context) {

    private val githubOwner = "doc-shashank"
    private val githubRepo = "question-quarry"
    
    private val latestReleaseUrl = "https://api.github.com/repos/$githubOwner/$githubRepo/releases/latest"
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()

    suspend fun checkForUpdates(): Result<UpdateInfo?> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(latestReleaseUrl)
                .header("Accept", "application/vnd.github+json")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext Result.failure(Exception("HTTP ${response.code}"))

                val json = JSONObject(response.body?.string() ?: "")
                val latestVersion = json.getString("tag_name").removePrefix("v")
                
                if (isNewerVersion(latestVersion, BuildConfig.VERSION_NAME)) {
                    val assets = json.getJSONArray("assets")
                    var downloadUrl: String? = null
                    
                    for (i in 0 until assets.length()) {
                        val asset = assets.getJSONObject(i)
                        val name = asset.getString("name")
                        if (name.endsWith(".apk")) {
                            downloadUrl = asset.getString("browser_download_url")
                            break
                        }
                    }

                    return@withContext downloadUrl?.let {
                        Result.success(
                            UpdateInfo(
                                version = latestVersion,
                                downloadUrl = it,
                                releaseNotes = json.optString("body", "")
                            )
                        )
                    } ?: Result.success(null)
                }
                Result.success(null)
            }
        } catch (e: Exception) {
            Log.e("UpdateManager", "Update check failed", e)
            Result.failure(e)
        }
    }

    private fun isNewerVersion(latest: String, current: String): Boolean {
        val latestParts = latest.split(".").mapNotNull { it.toIntOrNull() }
        val currentParts = current.split(".").mapNotNull { it.toIntOrNull() }
        
        for (i in 0 until maxOf(latestParts.size, currentParts.size)) {
            val l = latestParts.getOrElse(i) { 0 }
            val c = currentParts.getOrElse(i) { 0 }
            if (l > c) return true
            if (l < c) return false
        }
        return false
    }

    fun downloadAndInstall(updateInfo: UpdateInfo) {
        val destination = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "update.apk")
        if (destination.exists()) destination.delete()

        val request = DownloadManager.Request(updateInfo.downloadUrl.toUri())
            .setTitle("Downloading QuestionQuarry Update")
            .setDescription("Version ${updateInfo.version}")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationUri(updateInfo.downloadUrl.toUri()) // Fixed: Destination should be Local File Uri or remove setDestinationUri if handled by DM
            // Reverting setDestinationUri to a file based one as per earlier implementation or let DM decide
            .setDestinationUri(Uri.fromFile(destination))
            .setMimeType("application/vnd.android.package-archive")

        val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = manager.enqueue(request)

        val onComplete = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (id == downloadId) {
                    installApk(destination)
                    context.unregisterReceiver(this)
                }
            }
        }

        context.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), Context.RECEIVER_NOT_EXPORTED)
    }

    private fun installApk(file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(intent)
    }
}
