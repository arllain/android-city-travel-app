package br.com.arllain.citytravelapp.workers

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import br.com.arllain.citytravelapp.OUTPUT_PATH
import java.io.File

class CleanupWorker(context: Context, workerParameters: WorkerParameters)
    : Worker(context, workerParameters) {

    override fun doWork(): Result {

        makeStatusNotification("","Cleaning up old temporary files", applicationContext)
        sleep()

        return try {
            val outputDirectory = File(applicationContext.filesDir, OUTPUT_PATH)
            if (outputDirectory.exists()) {
                val entries = outputDirectory.listFiles()
                if (entries != null) {
                    for (entry in entries) {
                        val name = entry.name
                        if (name.isNotEmpty()) {
                         val deleted = entry.deleteRecursively()
                         Log.i("", "Deleted $name - $deleted")
                        }
                    }
                }
            }
            Result.success()
        } catch (exception: Exception) {
            Result.failure()
        }
    }
}
