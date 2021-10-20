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
        Log.d("OnCreate","OnCreate Called")

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

        supportFragmentManager.beginTransaction().setReorderingAllowed(true).add(R.id.opengl_main,OpenGLMainFragment(),"MainFragment").commit()





        CFGLGyro = getSystemService(SENSOR_SERVICE) as SensorManager

        CFGLActivity = this
    }

    fun updateText(inText : String) {
        this.runOnUiThread {
            val tv = findViewById<TextView>(R.id.score)
            tv.text = inText
        }
    }

    fun togglePause() {
        if(!paused)
        {
            this.runOnUiThread {
                supportFragmentManager.beginTransaction().setReorderingAllowed(true).add(R.id.opengl_main,OpenGLPauseFragment(),"PauseFragment").commit()
            }
        }
        else {
            this.runOnUiThread {
                supportFragmentManager.findFragmentByTag("PauseFragment")?.let {
                    supportFragmentManager.beginTransaction().setReorderingAllowed(true).remove(
                        it
                    ).commit()
                }
            }
        }

        paused = !paused
    }

    fun setPause(on : Boolean) {
        if(on)
        {
            if(!paused)
            {
                this.runOnUiThread {
                    supportFragmentManager.beginTransaction().setReorderingAllowed(true).add(R.id.opengl_main,OpenGLPauseFragment(),"PauseFragment").commit()
                }
                paused = true
            }
        }
        else {
            if(paused)
            {
                this.runOnUiThread {
                    supportFragmentManager.findFragmentByTag("PauseFragment")?.let {
                        supportFragmentManager.beginTransaction().setReorderingAllowed(true).remove(
                            it
                        ).commit()
                    }
                }
                paused = false
            }
        }
    }


    override fun onPause() {
        super.onPause()

        if(running) {
            CFGLPhysicsController.stop()
            if(!reset)
            {
                setPause(true)
                CFGLEngine.halt = true
            }
        }

        CFGLView.onPause()
    }

    override fun onResume() {
        Log.d("OnCreate","GLView Resumed")
        super.onResume()
        CFGLView.onResume()

        window.decorView.setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_IMMERSIVE or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        )

        if(running){
            if(reset) {
                CFGLEngine.resetGame()
                setPause(false)
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