package com.example.opengltest

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlin.math.abs

// The class handles accessing the gyroscope
// The constructor takes a callback function as a parameter
class GyroData(var CBack: (xValue: Float, yValue: Float) -> Unit) : SensorEventListener {
    // Static variables
    companion object {
        var zeroPointVert = 0f
        var maxVal = -9.8f
        var lastUpDownVal : Float = 0f
    }

    // This is called on class instantiation
    init {
        CFGL.Gyro.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also {
            CFGL.Gyro.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    // This is called by android whenever there is new gyro data
    override fun onSensorChanged(p0: SensorEvent?) {
        if(p0?.sensor?.type == Sensor.TYPE_ACCELEROMETER){
            val sides  = p0.values[0] * -1
            val upDown = p0.values[1] * -1

            lastUpDownVal = upDown
            var upDownMod = upDown - zeroPointVert
            val scalar = abs(zeroPointVert / (maxVal - zeroPointVert)) + 1
            if(upDownMod < 0) {
                upDownMod *= scalar
            }

            CBack(sides, upDownMod)
        }
    }

    // We don't really care if the gyro accuracy changes
    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        return
    }

    // Sets the zero point of the gyro in the y axis
    fun setZero() {
        zeroPointVert = lastUpDownVal
    }
}