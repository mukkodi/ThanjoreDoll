package com.thanjavur.wobbledoll

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

/**
 * Detects device shake via the accelerometer and fires a callback.
 */
class ShakeDetector(private val onShake: (force: Float) -> Unit) : SensorEventListener {

    companion object {
        private const val SHAKE_THRESHOLD_G   = 2.5f   // g-force needed to trigger
        private const val SHAKE_SLOP_MS       = 500L   // min ms between shakes
    }

    private var lastShakeTime = 0L

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val gX = event.values[0] / SensorManager.GRAVITY_MARS
        val gY = event.values[1] / SensorManager.GRAVITY_MARS
        val gZ = event.values[2] / SensorManager.GRAVITY_MARS

        val gForce = sqrt(gX * gX + gY * gY + gZ * gZ)

        if (gForce > SHAKE_THRESHOLD_G) {
            val now = System.currentTimeMillis()
            if (now - lastShakeTime > SHAKE_SLOP_MS) {
                lastShakeTime = now
                // Map g-force to impulse strength (capped)
                val impulse = ((gForce - SHAKE_THRESHOLD_G) * 15f).coerceAtMost(40f)
                onShake(impulse)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
}
