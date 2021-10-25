package com.example.opengltest


import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.os.Build
import android.view.LayoutInflater
import androidx.annotation.RequiresApi


class OpenGLActivity: AppCompatActivity() {

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create a GLSurfaceView instance and set it
        // as the ContentView for this Activity.
        CFGL.View = CFGLSurfaceView(this)
        setContentView(CFGL.View)

        // Create a relative layout to hold all of the UI fragments that will be added later
        var gi = RelativeLayout(this)
        val inflater: LayoutInflater = this.layoutInflater
        var view = inflater.inflate(R.layout.opengl_main, null)
        gi.addView(view)
        addContentView(gi, ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT))

        // Get the sensor manager so we can use the gyro
        CFGL.Gyro = getSystemService(SENSOR_SERVICE) as SensorManager

        CFGL.Activity = this
    }

    // Called when this activity pauses
    override fun onPause() {
        super.onPause()
        CFGLEngine.onPause()
        CFGL.View.onPause()

        if(running) {
            CFGLPhysicsController.stop()
        }
    }

    // Called when this activity resumes
    override fun onResume() {
        super.onResume()
        CFGLEngine.onResume()
        CFGL.View.onResume()

        // Hide the android menu bars
        window.decorView.setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_IMMERSIVE or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        )

        // If this activity has already been stared we need to restart the physics controller
        // Otherwise it is started in onCreate()
        if(running){
            if(CFGLPhysicsController.isPaused())
            {
                CFGLPhysicsController.start()
                CFGLPhysicsController.pause()
            }
            else {
                CFGLPhysicsController.start()
            }
        }
    }

    // Called when this activity is destroyed
    override fun onDestroy() {
        running = false
        super.onDestroy()
        CFGLPhysicsController.stop()
    }

    companion object {
        var running = false
    }
}