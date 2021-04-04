package br.com.arllain.citytravelapp.workers

import android.content.Context
import android.util.Log
import android.webkit.URLUtil
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters

class DownloadJsonFileWorker(context: Context, workerParameters: WorkerParameters)
    : Worker(context, workerParameters) {

    override fun doWork(): Result {
        val url = inputData.getString(DownloadZipFileWorker.INPUT_FILE_URL)

        makeStatusNotification("", "Downloading zip file", applicationContext)

        return try {
            if (!URLUtil.isValidUrl(url)) {
                throw Throwable("Invalid URL")
            }

            val downloadedFile = downloadFile(applicationContext, url, "pacotes.json")
            val outputData = Data.Builder().putString(DownloadZipFileWorker.OUTPUT_FILE_PATH, downloadedFile.absolutePath)
                .build()

            Result.success(outputData)
        } catch (throwable: Throwable) {
            Log.e("DownloadZipFileWorker", "Error downloading json file ${throwable.message}")
            Result.failure()
        }
    }

    companion object {
        const val INPUT_FILE_URL = "INPUT_FILE_URL"
        const val OUTPUT_FILE_PATH = "OUTPUT_FILE_PATH"
    }

}
