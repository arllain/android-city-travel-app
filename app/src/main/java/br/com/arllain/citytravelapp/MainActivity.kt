package br.com.arllain.citytravelapp

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
import br.com.arllain.citytravelapp.databinding.ActivityMainBinding
import br.com.arllain.citytravelapp.workers.DownLoadFileWorker
import br.com.arllain.citytravelapp.workers.UnzipFileWorker
import br.com.arllain.citytravelapp.workers.makeStatusNotification
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

        val downLoadFileWorkerRequest = OneTimeWorkRequest.Builder(DownLoadFileWorker::class.java)
            .setConstraints(networkConstraints)
            .setInputData(getIdInputData(DownLoadFileWorker.INPUT_DATA_URL, FILE_URL_DOWNLOAD))
            .build()

        val unzipFileWorkerRequest = OneTimeWorkRequest.Builder(UnzipFileWorker::class.java)
            .setConstraints(networkConstraints)
            .setInputData(getIdInputData(UnzipFileWorker.INPUT_FILE_PATH, File(applicationContext.filesDir, OUTPUT_PATH).absolutePath.toString()))
            .build()

        // Add a chained enqueue request
        workManager.beginWith(downLoadFileWorkerRequest)
            .then(unzipFileWorkerRequest)
            .enqueue()

        // track the progress of the enqueued WorkRequest instances
        workManager.getWorkInfoByIdLiveData(downLoadFileWorkerRequest.id)
            .observe(this, Observer {
                Log.d("MainActivity","DownLoadFileWorker WorkInfo status: ${it.state}")

                when(it.state == WorkInfo.State.SUCCEEDED){
                    true -> {
                        Log.d("MainActivity","File path: ${it.outputData.getString(DownLoadFileWorker.OUTPUT_FILE_PATH)}")
                        makeStatusNotification("", "file downloading finished", applicationContext)
                        showWorkFinished()
                    }
                    false -> {
                        Log.d("MainActivity","DownLoadFileWorker has not finished yet")
                        showWorkInProgress()
                    }
                }
            })

        workManager.getWorkInfoByIdLiveData(unzipFileWorkerRequest.id)
            .observe(this, Observer {
                Log.d("MainActivity","UnzipFileWorkerRequest WorkInfo status: ${it.state}")

                when(it.state == WorkInfo.State.SUCCEEDED){
                    true -> {
                        makeStatusNotification("", "unzip file finished", applicationContext)
                        showWorkFinished()
                    }
                    false -> {
                        Log.d("MainActivity","DownLoadFileWorker has not finished yet")
                        showWorkInProgress()
                    }
                }
            })
    }

    private fun getIdInputData(idKey: String, idValue: String) =
        Data.Builder().putString(idKey, idValue).build()


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