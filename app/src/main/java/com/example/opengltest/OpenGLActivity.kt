package com.example.opengltest

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.Path
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RelativeLayout

import android.widget.TextView
import android.os.Build

import android.view.LayoutInflater
import androidx.annotation.RequiresApi


lateinit var CFGLView : CFGLSurfaceView
lateinit var CFGLGyro : SensorManager
lateinit var CFGLActivity : OpenGLActivity

class OpenGLActivity: AppCompatActivity() {
    var paused = false
    var reset = false

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create a GLSurfaceView instance and set it
        // as the ContentView for this Activity.
        CFGLView = CFGLSurfaceView(this)
        setContentView(CFGLView)

        var gi = RelativeLayout(this)
        val inflater: LayoutInflater = this.layoutInflater
        var view = inflater.inflate(R.layout.opengl_main, null)
        gi.addView(view)
        addContentView(gi, ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT))

        CFGLGyro = getSystemService(SENSOR_SERVICE) as SensorManager

        CFGLActivity = this
    }

    override fun onPause() {
        super.onPause()
        CFGLEngine.onPause()

        if(running) {
            CFGLPhysicsController.stop()
            if(!reset)
            {
                OpenGLPauseFragment.show()
                CFGLEngine.halt = true
            }
        }

        CFGLView.onPause()
    }

    override fun onResume() {
        super.onResume()
        CFGLEngine.onResume()
        CFGLView.onResume()

        window.decorView.setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_IMMERSIVE or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        )

        if(running){
            if(reset) {
                CFGLEngine.resetGame()
                OpenGLPauseFragment.hide()
                reset = false
            }

            CFGLPhysicsController.start()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        running = false
    }

    companion object {
        var running = false
    }
}