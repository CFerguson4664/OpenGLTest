package com.example.opengltest

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log

class GyroData(var CBack: (xValue: Float, yValue: Float) -> Unit) : SensorEventListener {

    init {
        CFGLGyro.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also {
            CFGLGyro.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    override fun onSensorChanged(p0: SensorEvent?) {
        if(p0?.sensor?.type == Sensor.TYPE_ACCELEROMETER){
            val sides  = p0.values[0] * -1
            val upDown = p0.values[1] * -1

            CBack(sides, upDown)
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        return
    }
}