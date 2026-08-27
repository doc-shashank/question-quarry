package opensource.qwx.questionquarry.util

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID

class StorageManager(private val context: Context) {

    private val TAG = "StorageManager"

    /**
     * Returns the FileProvider authority for the app.
     */
    fun getAuthority(): String {
        return "${context.packageName}.fileprovider"
    }

    /**
     * Creates a temporary URI for the camera to save a full-size image.
     */
    fun createTempImageUri(): Uri? {
        return try {
            val imageDir = File(context.cacheDir, "images")
            if (!imageDir.exists()) {
                val created = imageDir.mkdirs()
                Log.d(TAG, "Created images directory: $created")
            }
            val file = File(imageDir, "temp_image_${UUID.randomUUID()}.jpg")
            val uri = FileProvider.getUriForFile(context, getAuthority(), file)
            Log.d(TAG, "Created temp URI: $uri for file: ${file.absolutePath}")
            uri
        } catch (e: Exception) {
            Log.e(TAG, "Error creating temp image URI", e)
            null
        }
    }

    /**
     * Saves an image from a URI to internal storage by copying the stream.
     * This avoids loading the whole bitmap into memory (preventing OOM).
     */
    fun saveImage(uri: Uri): String? {
        Log.d(TAG, "Saving image from URI: $uri")
        val filename = "${UUID.randomUUID()}.jpg"
        val imageDir = File(context.filesDir, "images")
        if (!imageDir.exists()) {
            imageDir.mkdirs()
        }
        val destinationFile = File(imageDir, filename)
        
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(destinationFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            Log.d(TAG, "Image saved successfully to: ${destinationFile.absolutePath}")
            destinationFile.absolutePath
        } catch (e: Throwable) {
            Log.e(TAG, "Error saving image from URI: $uri", e)
            null
        }
    }

    /**
     * Saves a bitmap to internal storage.
     * Returns the absolute path of the saved file.
     */
    fun saveBitmap(bitmap: Bitmap?): String? {
        if (bitmap == null) return null
        val filename = "${UUID.randomUUID()}.jpg"
        val file = File(context.filesDir, filename)
        
        Log.d(TAG, "Saving bitmap to: ${file.absolutePath}")
        return try {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            file.absolutePath
        } catch (e: Throwable) {
            Log.e(TAG, "Error saving bitmap", e)
            null
        }
    }

    /**
     * Deletes an image file from internal storage.
     */
    fun deleteImage(path: String): Boolean {
        val file = File(path)
        return if (file.exists()) {
            file.delete()
        } else {
            false
        }
    }

    /**
     * Gets a file from internal storage path.
     */
    fun getFile(path: String): File? {
        val file = File(path)
        return if (file.exists()) file else null
    }
}
