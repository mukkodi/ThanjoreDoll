package com.thanjavur.wobbledoll

import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var dollView: ThanjavurDollView
    @Inject lateinit var sensorManager: SensorManager
    private lateinit var shakeDetector: ShakeDetector
    private var accelerometer: Sensor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Keep screen on while playing with the doll
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContentView(R.layout.activity_main)

        dollView = findViewById(R.id.dollView)

        // Touch handler feedback
        dollView.onTouched = {
            // Optional: add haptic feedback here
            // dollView.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        }

        // Shake detector setup
        accelerometer  = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        shakeDetector = ShakeDetector { force ->
            runOnUiThread {
                val side = if ((0..1).random() == 0) force else -force
                dollView.startWobble(side)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.let {
            sensorManager.registerListener(shakeDetector, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(shakeDetector)
    }
}
