package com.example.opengltest

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
import kotlinx.android.synthetic.main.activity_main.*

import android.widget.TextView

lateinit var CFGLView : CFGLSurfaceView
lateinit var CFGLGyro : SensorManager
lateinit var CFGLActivity : OpenGLActivity

class OpenGLActivity: AppCompatActivity() {
    lateinit var tv : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("OnCreate","OnCreate Called")

        super.onCreate(savedInstanceState)




        // Create a GLSurfaceView instance and set it
        // as the ContentView for this Activity.
        CFGLView = CFGLSurfaceView(this)

        var rl = RelativeLayout(this)
        rl.addView(CFGLView)

        val lp: RelativeLayout.LayoutParams =
            RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        lp.addRule(RelativeLayout.ALIGN_TOP)
        tv = TextView(this)
        tv.setTextColor(Color.WHITE)
        tv.layoutParams = lp
        tv.textSize = 36f
        tv.text = " Score: 0"
        tv.setBackgroundColor(0x0060ff00)
        rl.addView(tv)

        setContentView(rl)


        var menu = Button(this)
        menu.text = "Menu"
        menu.setOnClickListener {
            CFGLPhysicsController.stop()
            val intent = Intent(this, MainMenu::class.java)
            startActivity(intent)
        }
        menu.textSize = 28f

        val lp2: RelativeLayout.LayoutParams =
            RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        lp2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
        menu.layoutParams = lp2
        rl.addView(menu)

        var zero = Button(this)
        zero.text = "Zero"
        zero.setOnClickListener {
            CFGLPhysicsController.setGyroZero(CFGLPhysicsController.gyroPos.y)
        }
        zero.textSize = 28f

        val lp3: RelativeLayout.LayoutParams =
            RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        lp3.setMargins(0,150,0,0,)
        lp3.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
        zero.layoutParams = lp3
        rl.addView(zero)


        CFGLGyro = getSystemService(SENSOR_SERVICE) as SensorManager

        CFGLActivity = this
    }

    fun updateText(inText : String) {
        this.runOnUiThread {
            tv.text = inText
        }
    }

    override fun onPause() {
        super.onPause()
        CFGLView.onPause()

        if(running) {
            CFGLPhysicsController.stop()
        }
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
            CFGLEngine.resetGame()
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