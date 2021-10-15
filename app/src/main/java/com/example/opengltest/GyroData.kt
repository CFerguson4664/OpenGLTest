package com.example.opengltest

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlin.math.abs

class GyroData(var CBack: (xValue: Float, yValue: Float) -> Unit) : SensorEventListener {
    var zeroPoint = 0f
    var maxVal = -9.8f
    var lastUpDownVal : Float = 0f

    init {
        CFGLGyro.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also {
            CFGLGyro.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    override fun onSensorChanged(p0: SensorEvent?) {
        if(p0?.sensor?.type == Sensor.TYPE_ACCELEROMETER){
            val sides  = p0.values[0] * -1
            val upDown = p0.values[1] * -1
            lastUpDownVal = upDown
            var upDownMod = upDown - zeroPoint

            val scalar = abs(zeroPoint / (maxVal - zeroPoint)) + 1
            if(upDownMod < 0) {
                upDownMod *= scalar
            }

            Log.d("Zero",upDownMod.toString())
            Log.d("Scalar", scalar.toString())

            CBack(sides, upDownMod)
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        return
    }

    fun setZero() {
        zeroPoint = lastUpDownVal

        Log.d("Zero", lastUpDownVal.toString())
    }
}