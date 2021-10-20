package com.example.opengltest

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment

class OpenGLSettingsFragment : Fragment(R.layout.opengl_settings) {


    lateinit var vertText : TextView
    lateinit var horizText : TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val gyro = GyroData(this::displayData)
        super.onViewCreated(view, savedInstanceState)

        vertText = CFGLActivity.findViewById<TextView>(R.id.tvgvv)
        horizText = CFGLActivity.findViewById<TextView>(R.id.tvghv)

        val doneButton = CFGLActivity.findViewById<Button>(R.id.done)
        doneButton.setOnClickListener {
            CFGLActivity.supportFragmentManager.findFragmentByTag("SettingsFragment")?.let {
                CFGLActivity.supportFragmentManager.beginTransaction().setReorderingAllowed(true).remove(it).commit()
            }
            CFGLEngine.disableTap = false
        }

        val zeroButton = CFGLActivity.findViewById<Button>(R.id.zeroVert)
        zeroButton.setOnClickListener {
            gyro.setZero()
        }
    }

    fun displayData(x : Float, y : Float) {
        CFGLActivity.runOnUiThread {
            horizText.text = String.format("%.3f",x)
            vertText.text = String.format("%.3f",y)
        }
    }
}

class OpenGLMainFragment : Fragment(R.layout.opengl_menu) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val menuButton = CFGLActivity.findViewById<Button>(R.id.menu)
        menuButton.setOnClickListener {
            CFGLActivity.reset = true
            val intent = Intent(CFGLActivity, MainMenu::class.java)
            startActivity(intent)
        }
    }
}

class OpenGLPauseFragment : Fragment(R.layout.opengl_pause) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val settingsButton = CFGLActivity.findViewById<Button>(R.id.pauseSettings)
        settingsButton.setOnClickListener {
            CFGLActivity.supportFragmentManager.beginTransaction().add(R.id.opengl_main, OpenGLSettingsFragment(),"SettingsFragment").commit()
            CFGLEngine.disableTap = true
        }
    }
}