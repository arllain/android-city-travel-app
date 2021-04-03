package br.com.arllain.citytravelapp.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import br.com.arllain.citytravelapp.*
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream


fun makeStatusNotification(title: String, message: String, context: Context) {

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            CHANNEL_ID, NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = NOTIFICATION_CHANNEL_DESCRIPTION
        }

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?

        notificationManager?.createNotificationChannel(channel)
    }

    // Create the notification
    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_notifications)
        .setContentTitle(title)
        .setContentText(message)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setVibrate(LongArray(0))

    // Show the notification
    NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build())
}

fun sleep() {
    try {
        Thread.sleep(DELAY_TIME_MILLIS, 0)
    } catch (e: InterruptedException) {
        e.message?.let { Log.e("sleep() :: InterruptedException :: ", it) }
    }

}

fun downloadFile(applicationContext: Context, url: String?, fileName: String): File {

    val downloadUrl = URL(url)
    val urlConnection: HttpURLConnection = downloadUrl.openConnection() as HttpURLConnection

    val outputDir = File(applicationContext.filesDir, OUTPUT_PATH)
    if (!outputDir.exists()) {
        outputDir.mkdirs()
    }

    val downloadedFile = File(outputDir, fileName)

    try {
        urlConnection.connect()

        if (urlConnection.responseCode != HttpURLConnection.HTTP_OK) {
            makeStatusNotification(
                "Server Error ",
                "Server returned HTTP ${urlConnection.responseCode} ${urlConnection.responseMessage}",
                applicationContext
            )
        }

        val fileLength: Int = urlConnection.contentLength
        val `in`: InputStream = BufferedInputStream(urlConnection.getInputStream())

        val fileOutputStream = FileOutputStream(downloadedFile)
        val data = ByteArray(4096)
        var total: Long = 0
        var count: Int

        try {
            while (`in`.read(data).also { count = it } != -1) {
                total += count.toLong()
                if (fileLength > 0) {
                    fileOutputStream.write(data, 0, count)
                }
            }
        } catch (e: Exception) {
        }
    } finally {
        urlConnection.disconnect()
    }

    return downloadedFile
}


fun unzip(applicationContext: Context, fileZipPath: String) {

    val outputDir = File(applicationContext.filesDir, OUTPUT_UNZIP_PATH)
    if (!outputDir.exists()) {
        outputDir.mkdirs()
    }

    val buffer = ByteArray(1024)
    val zis = ZipInputStream(FileInputStream(fileZipPath))
    var zipEntry = zis.nextEntry

    while (zipEntry != null) {
        val newFile = newFile(outputDir, zipEntry)
        if (zipEntry.isDirectory) {
            if (!newFile.isDirectory && !newFile.mkdirs()) {
                throw IOException("Failed to create directory $newFile")
            }
        } else {
            // fix for Windows-created archives
            val parent = newFile.parentFile
            if (!parent.isDirectory && !parent.mkdirs()) {
                throw IOException("Failed to create directory $parent")
            }

            // write file content
            val fos = FileOutputStream(newFile)
            var len: Int
            while (zis.read(buffer).also { len = it } > 0) {
                fos.write(buffer, 0, len)
            }
            fos.close()
        }
        zipEntry = zis.nextEntry
    }
    zis.closeEntry();
    zis.close();
}


@Throws(IOException::class)
fun newFile(destinationDir: File, zipEntry: ZipEntry): File {
    val destFile = File(destinationDir, zipEntry.getName())
    val destDirPath = destinationDir.canonicalPath
    val destFilePath = destFile.canonicalPath
    if (!destFilePath.startsWith(destDirPath + File.separator)) {
        throw IOException("Entry is outside of the target dir: " + zipEntry.getName())
    }
    return destFile
}