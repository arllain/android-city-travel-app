package br.com.arllain.citytravelapp.workers

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters

class UnzipFileWorker(context: Context, workerParameters: WorkerParameters)
    : Worker(context, workerParameters) {

    override fun doWork(): Result {
        val filePath = inputData.getString(INPUT_FILE_PATH) + "/cidades.zip"

        makeStatusNotification("", "Unzipping file", applicationContext)

        return try {
            unzip(applicationContext,filePath)
            Result.success()
        } catch (throwable: Throwable) {
            Log.e("UnzipFileWorker", "Error unzipping file ${throwable.message}")
            Result.failure()
        }
    }

    companion object {
        const val INPUT_FILE_PATH = "INPUT_FILE_PATH"
    }

}
