package com.example.tt_bssd5250_91

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity(), SensorEventListener {

    lateinit var infoText: TextView

    private val requestMultiplePermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissions.entries.forEach {
            var currentText = infoText.text
            var isCam = false

            if (it.key == android.Manifest.permission.CAMERA) {
                currentText = "$currentText Camera = "
                isCam = true
            } else if (it.key == android.Manifest.permission.BLUETOOTH_SCAN) {
                currentText = "$currentText Bluetooth = "
            } else {
                currentText = "$currentText Motion = "
            }

            if (it.value) {
                currentText = "$currentText Granted."
                if (isCam) {
                    openCamera()
                } else {
                    readMotion()
                }
            } else {
                currentText = "$currentText Denied."
            }
            infoText.text = currentText
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        readMotion()
        infoText = TextView(this).apply {
            hint = "Click a button"
        }
        val cameraButton = Button(this).apply {
            text = "Camera"
            setOnClickListener {
                when {
                    ContextCompat.checkSelfPermission(
                        applicationContext,
                        android.Manifest.permission.CAMERA,
                    ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                        applicationContext,
                        android.Manifest.permission.ACTIVITY_RECOGNITION
                    ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                        applicationContext,
                        android.Manifest.permission.BLUETOOTH_SCAN
                    ) == PackageManager.PERMISSION_GRANTED -> {
                        openCamera()
                    }
                    shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA) -> {
                        dialogBuilder("Camera")
                    }
                    shouldShowRequestPermissionRationale(android.Manifest.permission.ACTIVITY_RECOGNITION) -> {
                        dialogBuilder("Motion")
                    }
                    shouldShowRequestPermissionRationale(android.Manifest.permission.BLUETOOTH_SCAN) -> {
                        dialogBuilder("Bluetooth")
                    }
                    else -> {
                        requestMultiplePermissionsLauncher.launch(
                            arrayOf(
                                android.Manifest.permission.CAMERA,
                                android.Manifest.permission.ACTIVITY_RECOGNITION,
                                android.Manifest.permission.BLUETOOTH_SCAN
                            )
                        )
                    }
                }
            }
        }
        val mainLayout = LinearLayoutCompat(this).apply {
            orientation = LinearLayoutCompat.VERTICAL
            layoutParams = LinearLayoutCompat.LayoutParams(
                LinearLayoutCompat.LayoutParams.MATCH_PARENT,
                LinearLayoutCompat.LayoutParams.MATCH_PARENT
            )
            addView(infoText)
            addView(cameraButton)
        }
        setContentView(mainLayout)
    }

    private fun readMotion() {
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val mSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        sensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivity(cameraIntent)
    }

    private fun dialogBuilder(name: String) {
        AlertDialog.Builder(this).apply {
            setTitle("$name Features")
            setMessage("You must allow $name permissions to use this feature. Ask again?")
            setPositiveButton("Yes") { _, _ ->
                requestMultiplePermissionsLauncher.launch(
                    arrayOf(
                        android.Manifest.permission.CAMERA,
                        android.Manifest.permission.ACTIVITY_RECOGNITION,
                        android.Manifest.permission.BLUETOOTH_SCAN
                    )
                )
            }
            create()
            show()
        }
    }

    override fun onSensorChanged(p0: SensorEvent?) {
        Log.d("MOTIONS", p0!!.values[0].toString())
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        Log.d("ACCURACY", p1.toString())
    }
}