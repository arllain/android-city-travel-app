package br.com.arllain.citytravelapp.workers

import android.content.Context
import android.util.Log
import android.webkit.URLUtil
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters

class DownLoadFileWorker(context: Context, workerParameters: WorkerParameters)
    : Worker(context, workerParameters) {

    override fun doWork(): Result {
        val url = inputData.getString(INPUT_DATA_URL)

        makeStatusNotification("DownLoadFileWorker", "Downloading file", applicationContext)

        return try {
            if (!URLUtil.isValidUrl(url)) {
                throw Throwable("Invalid URL")
            }

            val downloadedFile = downloadFile(applicationContext, url, "cidades.zip")
            val outputData = Data.Builder().putString(OUTPUT_FILE_PATH, downloadedFile.absolutePath)
                .build()

            Result.success(outputData)
        } catch (throwable: Throwable) {
            Log.e("DownloadWorker", "Error downloading file ${throwable.message}")
            Result.failure()
        }
    }

    companion object {
        const val INPUT_DATA_URL = "INPUT_FILE_URL"
        const val OUTPUT_FILE_PATH = "output_file_path"
    }

}
