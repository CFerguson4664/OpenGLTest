package com.example.opengltest

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

// This class is the driving force behind controlling the movements of all of the objects
// in the game. It is responsible for calling CFGLEngine.Update() at regular intervals
// Animations also listen to the pause variable to decide whether or not to animate

class CFGLPhysicsController : ViewModel() {
    companion object {
        // Dispatcher to handle starting and stopping on the physics thread
        private lateinit var dispatcher : ExecutorCoroutineDispatcher
        private var started : Boolean = false

        // Variables to store GyroData object and supporting objects
        private lateinit var gyro : GyroData
        private var gyroLock = ReentrantLock()
        private var gyroPos : Vector2 = Vector2(0f,0f)

        // Controls the number of values averaged to smooth the gyro data
        private var bufferSize = 4
        private var gyroBuffer : MutableList<Vector2> = emptyList<Vector2>().toMutableList()

        // Variable and lock to control pausing of the physics engine
        private var pausedLock = ReentrantLock()
        private var paused : Boolean = true

        // The function is called by the GyroData object whenever new data is available
        private fun onNewGyroData(x : Float, y : Float) {
            // Add the new data to the buffer
            gyroBuffer.add(Vector2(x,y))

            // If the buffer is over capacity remove the oldest item
            // Can do it this way since we are using a list and not an array
            if(gyroBuffer.size == bufferSize + 1) {
                gyroBuffer.removeAt(0)
                gyroLock.withLock {
                    gyroPos = gyroCurrent()
                }
            }
        }

        // Get the average of the values in the gyro data buffer
        private fun gyroCurrent() : Vector2 {
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


        // Calling this function connects the Gyroscope to the physics engine
        fun useGyro() {
            // The function passed as a parameter is used as a callback whenever new data is available
            gyro = GyroData(this::onNewGyroData)
        }

        // This function returns the current position of the gyroscope
        fun getGyroPos() : Vector2 {
            gyroLock.withLock {
                return gyroPos
            }
        }

        // Pauses the physics engine without destroying the physics thread
        fun pause() {
            pausedLock.withLock {
                paused = true
            }
        }

        // Resumes the physics engine
        fun resume() {
            pausedLock.withLock {
                paused = false
            }
        }

        // Returns whether or not the physics engine is currently paused
        fun isPaused() : Boolean {
            pausedLock.withLock {
                return paused
            }
        }

        fun isStarted() : Boolean {
            return started
        }

        // Starts the physics engine
        // Creates a separate thread to handle calling CFGLEngine.Update() at regular intervals
        fun start() {
            // Create a new coroutine to move the execution off the UI thread
            // https://kotlinlang.org/docs/coroutine-context-and-dispatchers.html#coroutine-scope
            dispatcher = newSingleThreadContext("Update")

            // Launching with Global Scope means this thread will never automatically close
            GlobalScope.launch(dispatcher) {
                var startTime = System.currentTimeMillis()
                while (true) {
                    // Control the repeat rate of CFGLEngine.Update()
                    val FRAME_TIME = 5
                    val endTime = System.currentTimeMillis()
                    val dt = endTime - startTime
                    if (dt < FRAME_TIME) {
                        delay(FRAME_TIME - dt)
                    }
                    startTime = endTime

                    // If the physics engine is not paused call CFGLEngine.Update()
                    if(!isPaused()) {
                        gyroLock.withLock {
                            CFGLEngine.update(dt / 1000f, gyroPos)
                        }
                    }
                }
            }

            resume()
            started = true
        }

        // Stops the physics engine
        // Destroys the thread created in the start() function
        fun stop() {
            dispatcher.close()
            pause()
            started = false
        }
    }
}