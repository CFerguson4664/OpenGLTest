package com.example.opengltest

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class CFGLPhysicsController(): ViewModel() {
    companion object {
        lateinit var gyro : GyroData

        var lock = ReentrantLock()
        var gyroPos : Vector2 = Vector2(0f,0f)
        var gyroBuffer : MutableList<Vector2> = emptyList<Vector2>().toMutableList()
        var bufferSize = 4
        lateinit var dispacher : ExecutorCoroutineDispatcher

        fun onNewGyroData(x : Float, y : Float) {
            if(gyroBuffer.size == bufferSize) {
                gyroBuffer.removeAt(0)
                lock.withLock {
                    gyroPos = gyroCurrent()
                }
            }

            gyroBuffer.add(Vector2(x,y))
        }

        fun useGyro() {
            gyro = GyroData(this::onNewGyroData)
        }

        fun gyroCurrent() : Vector2{
            var x = 0f
            var y = 0f
            for(vect in gyroBuffer)
            {
                x += vect.x
                y += vect.y
            }

            x /= gyroBuffer.size
            y /= gyroBuffer.size

            return Vector2(x,y)
        }

        fun start() {
            // Create a new coroutine to move the execution off the UI thread

            dispacher = newSingleThreadContext("Update")

            GlobalScope.launch(dispacher) {

                var startTime = System.currentTimeMillis()
                while (true) {
                    val FRAME_TIME = 5
                    val endTime = System.currentTimeMillis()
                    val dt = endTime - startTime
                    if (dt < FRAME_TIME) {
                        delay(FRAME_TIME - dt)
                    }
                    startTime = endTime

                    lock.withLock {
                        CFGLEngine.update(dt / 1000f, gyroPos)
                    }
                }
            }
        }

        fun stop() {
            dispacher.close()
        }

    }
}