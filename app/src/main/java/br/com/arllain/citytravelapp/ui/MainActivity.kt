package br.com.arllain.citytravelapp.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.work.*
import br.com.arllain.citytravelapp.*
import br.com.arllain.citytravelapp.R
import br.com.arllain.citytravelapp.databinding.ActivityMainBinding
import br.com.arllain.citytravelapp.model.Travel
import br.com.arllain.citytravelapp.workers.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.util.*

class MainActivity : AppCompatActivity() {

    private val REQUEST_CODE_PERMISSIONS = 101
    private val MAX_NUMBER_REQUEST_PERMISSIONS = 2

    private val permissions = Arrays.asList(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    private var permissionRequestCount: Int = 0

    private lateinit var binding: ActivityMainBinding

    private val workManager = WorkManager.getInstance(application)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestPermissionsIfNecessary()
    }

    private fun requestPermissionsIfNecessary() {
        if (!checkAllPermissions()) {
            if (permissionRequestCount < MAX_NUMBER_REQUEST_PERMISSIONS) {
                permissionRequestCount += 1
                ActivityCompat.requestPermissions(
                        this,
                        permissions.toTypedArray(),
                        REQUEST_CODE_PERMISSIONS
                )
            } else {
                Toast.makeText(
                        this,
                        R.string.set_permissions_in_settings,
                        Toast.LENGTH_LONG
                ).show()
            }
        }else {
            setupWorkers()
        }
    }

    private fun setupWorkers() {

        // defines a network constraint
        val networkConstraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED).build()

        val cleanupWorkerRequest = OneTimeWorkRequest.Builder(CleanupWorker::class.java)
            .setConstraints(networkConstraints)
            .build()

        val downloadZipFileWorkerRequest = OneTimeWorkRequest.Builder(DownloadZipFileWorker::class.java)
            .setConstraints(networkConstraints)
            .setInputData(getIdInputData(DownloadZipFileWorker.INPUT_FILE_URL, FILE_URL_DOWNLOAD))
            .build()

        val unzipFileWorkerRequest = OneTimeWorkRequest.Builder(UnzipFileWorker::class.java)
            .setConstraints(networkConstraints)
            .setInputData(getIdInputData(UnzipFileWorker.INPUT_FILE_PATH, File(applicationContext.filesDir, OUTPUT_PATH).absolutePath.toString()))
            .build()

        val downloadJsonFileWorkerRequest = OneTimeWorkRequest.Builder(DownloadJsonFileWorker::class.java)
            .setConstraints(networkConstraints)
            .setInputData(getIdInputData(DownloadJsonFileWorker.INPUT_FILE_URL, JSON_URL_DOWNLOAD))
            .build()

        // Add a chained enqueue request
        workManager.beginWith(cleanupWorkerRequest)
            .then(downloadZipFileWorkerRequest)
            .then(unzipFileWorkerRequest)
            .then(downloadJsonFileWorkerRequest)
            .enqueue()

        // track the progress of the enqueued WorkRequest instances
        workManager.getWorkInfoByIdLiveData(cleanupWorkerRequest.id)
            .observe(this, Observer {
                Log.d("MainActivity","CleanupWorker WorkInfo status: ${it.state}")

                when(it.state == WorkInfo.State.SUCCEEDED){
                    true -> {
                        makeStatusNotification("", "CleanUp finished", applicationContext)
                        showWorkFinished()
                    }
                    false -> {
                        Log.d("MainActivity","CleanupWorker has not finished yet")
                        showWorkInProgress()
                    }
                }
            })

        workManager.getWorkInfoByIdLiveData(downloadZipFileWorkerRequest.id)
            .observe(this, Observer {
                Log.d("MainActivity","DownloadZipFileWorker WorkInfo status: ${it.state}")

                when(it.state == WorkInfo.State.SUCCEEDED){
                    true -> {
                        Log.d("MainActivity","File path: ${it.outputData.getString(DownloadZipFileWorker.OUTPUT_FILE_PATH)}")
                        makeStatusNotification("", "Zip File downloading finished", applicationContext)
                        showWorkFinished()
                    }
                    false -> {
                        Log.d("MainActivity","DownloadZipFileWorker has not finished yet")
                        showWorkInProgress()
                    }
                }
            })

        workManager.getWorkInfoByIdLiveData(unzipFileWorkerRequest.id)
            .observe(this, Observer {
                Log.d("MainActivity","UnzipFileWorkerRequest WorkInfo status: ${it.state}")

                when(it.state == WorkInfo.State.SUCCEEDED){
                    true -> {
                        makeStatusNotification("", "File unzip finished", applicationContext)
                        showWorkFinished()
                    }
                    false -> {
                        Log.d("MainActivity","DownLoadFileWorker has not finished yet")
                        showWorkInProgress()
                    }
                }
            })

        workManager.getWorkInfoByIdLiveData(downloadJsonFileWorkerRequest.id)
            .observe(this, Observer {
                Log.d("MainActivity","DownloadJsonFileWorker WorkInfo status: ${it.state}")

                when(it.state == WorkInfo.State.SUCCEEDED){
                    true -> {
                        Log.d("MainActivity","File path: ${it.outputData.getString(DownloadJsonFileWorker.OUTPUT_FILE_PATH)}")
                        makeStatusNotification("", "Json File downloading finished", applicationContext)
                        showWorkFinished()
                        setupUi()
                    }
                    false -> {
                        Log.d("MainActivity","DownloadJsonFileWorker has not finished yet")
                        showWorkInProgress()
                    }
                }
            })
    }

    private fun getIdInputData(idKey: String, idValue: String) =
        Data.Builder().putString(idKey, idValue).build()


    private fun setupUi() {
        val outputDirectory = File(applicationContext.filesDir, OUTPUT_PATH)
        if (outputDirectory.exists()) {
            val entries = outputDirectory.listFiles()
            if (entries != null) {
                for (entry in entries) {
                    val name = entry.name
                    if (name.isNotEmpty() and name.equals("pacotes.json")) {
                        val jsonFileString = getJsonDataFromFile(entry)
                        Log.i("jsonFileString", "jsonFileString $jsonFileString")

                        val gson = Gson()
                        val listPacoteType = object : TypeToken<Travel>() {}.type
                        var travel: Travel = gson.fromJson(jsonFileString, listPacoteType)
                        travel.pacoteList.forEachIndexed { idx, pacote -> Log.i("Pacotes disponiveis", "> Pacote $idx:\n$pacote") }
                    }
                }
            }
        }
    }


    private fun checkAllPermissions(): Boolean {
        var hasPermissions = true
        for (permission in permissions) {
            hasPermissions = hasPermissions and isPermissionGranted(permission)
        }
        return hasPermissions
    }

    private fun isPermissionGranted(permission: String) =
            ContextCompat.checkSelfPermission(this, permission) ==
                    PackageManager.PERMISSION_GRANTED

    override fun onRequestPermissionsResult(requestCode: Int,  permissions: Array<String>,
            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            requestPermissionsIfNecessary() // no-op if permissions are granted already.
        }
    }

    /**
     * Shows and hides views for when the Activity is processing an image
     */
    private fun showWorkInProgress() {
        with(binding) {
            progressBar.visibility = View.VISIBLE
        }
    }

    /**
     * Shows and hides views for when the Activity is done processing an image
     */
    private fun showWorkFinished() {
        with(binding) {
            progressBar.visibility = View.GONE
        }
    }}