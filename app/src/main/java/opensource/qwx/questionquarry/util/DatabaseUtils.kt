package opensource.qwx.questionquarry.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

object DatabaseUtils {
    private const val DB_NAME = "question-quarry-db"

    fun exportDatabase(context: Context, destinationUri: Uri): Boolean {
        return try {
            val dbFile = context.getDatabasePath(DB_NAME)
            if (!dbFile.exists()) return false

            // Important: The database should be checkpointed before export 
            // to ensure all data from -wal and -shm is merged into the main .db file.
            
            context.contentResolver.openOutputStream(destinationUri)?.use { output ->
                FileInputStream(dbFile).use { input ->
                    input.copyTo(output)
                }
            }
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun importDatabase(context: Context, sourceUri: Uri): Boolean {
        return try {
            val dbFile = context.getDatabasePath(DB_NAME)
            val shmFile = File(dbFile.path + "-shm")
            val walFile = File(dbFile.path + "-wal")

            // Close DB before replacement? Usually handled by restarting the app or re-initializing Room.
            
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(dbFile).use { output ->
                    input.copyTo(output)
                }
            }

            // Delete shm and wal to ensure the imported db is used cleanly
            if (shmFile.exists()) shmFile.delete()
            if (walFile.exists()) walFile.delete()

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
