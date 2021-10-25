package com.example.opengltest

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment

// The UI laid out over the top of the OpenGLView is setup using fragments
// https://developer.android.com/guide/fragments

class OpenGLMainFragment : Fragment(R.layout.opengl_menu) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        // Setup onClick handlers
        val menuButton = CFGL.Activity.findViewById<Button>(R.id.menu)
        menuButton.setOnClickListener {
            CFGLEngine.resetGame()
            CFGLPhysicsController.resume()

            val intent = Intent(CFGL.Activity, MainMenu::class.java)
            startActivity(intent)


            OpenGLPauseFragment.hide()
            OpenGLDeathFragment.hide()
        }

        val pauseButton = CFGL.Activity.findViewById<Button>(R.id.pause)
        pauseButton.setOnClickListener {
            CFGLPhysicsController.pause()
            OpenGLPauseFragment.show()
        }
    }

    companion object {
        // Updates the score displayed on the UI
        fun updateScore(inText : String) {
            CFGL.Activity.runOnUiThread {
                val tv = CFGL.Activity.findViewById<TextView>(R.id.score)
                tv.text = inText
            }
        }

        // Shows this UI fragment
        fun show() {
            CFGL.Activity.supportFragmentManager.beginTransaction().add(R.id.opengl_main, OpenGLMainFragment(),"MainFragment").commit()
        }

        // Hides this UI fragment
        fun hide() {
            CFGL.Activity.supportFragmentManager.findFragmentByTag("MainFragment")?.let {
                CFGL.Activity.supportFragmentManager.beginTransaction().setReorderingAllowed(true).remove(it).commit()
            }
        }
    }
}

class OpenGLSettingsFragment : Fragment(R.layout.opengl_settings) {

    lateinit var vertText : TextView
    lateinit var horizText : TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get the text objects in the menu
        vertText = CFGL.Activity.findViewById<TextView>(R.id.tvgvv)
        horizText = CFGL.Activity.findViewById<TextView>(R.id.tvghv)

        // Create a GyroData Object to access Gyroscope Data
        // Callback function is passed as a parameter
        val gyro = GyroData(this::displayData)


        // Setup onClick handlers
        val doneButton = CFGL.Activity.findViewById<Button>(R.id.done)
        doneButton.setOnClickListener {
            hide()
            CFGLEngine.disableTap = false
        }

        val zeroButton = CFGL.Activity.findViewById<Button>(R.id.zeroVert)
        zeroButton.setOnClickListener {
            gyro.setZero()
        }
    }

    // Called by the GyroData object whenever new data is available
    fun displayData(x : Float, y : Float) {

        // This has to be run on the UI thread since it directly modifies the UI
        CFGL.Activity.runOnUiThread {
            horizText.text = String.format("%.3f",x)
            vertText.text = String.format("%.3f",y)
        }
    }

    companion object {

        // Shows this UI fragment
        fun show() {
            CFGL.Activity.supportFragmentManager.beginTransaction().add(R.id.opengl_main, OpenGLSettingsFragment(),"SettingsFragment").commit()
        }

        // Hides this UI fragment
        fun hide() {
            CFGL.Activity.supportFragmentManager.findFragmentByTag("SettingsFragment")?.let {
                CFGL.Activity.supportFragmentManager.beginTransaction().setReorderingAllowed(true).remove(it).commit()
            }
        }
    }
}

class OpenGLPauseFragment : Fragment(R.layout.opengl_pause) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup onClick handlers
        val settingsButton = CFGL.Activity.findViewById<Button>(R.id.pauseSettings)
        settingsButton.setOnClickListener {
            OpenGLSettingsFragment.show()
            CFGLEngine.disableTap = true
        }
    }

    companion object {

        // Shows this UI fragment
        fun show() {
            CFGL.Activity.supportFragmentManager.beginTransaction().add(R.id.opengl_main, OpenGLPauseFragment(),"PauseFragment").commit()
        }

        // Hides this UI fragment
        fun hide() {
            CFGL.Activity.supportFragmentManager.findFragmentByTag("PauseFragment")?.let {
                CFGL.Activity.supportFragmentManager.beginTransaction().setReorderingAllowed(true).remove(it).commit()
            }
        }
    }
}

class OpenGLDeathFragment : Fragment(R.layout.opengl_death) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup onClick handlers
        val settingsButton = CFGL.Activity.findViewById<Button>(R.id.pauseSettings)
        settingsButton.setOnClickListener {
            CFGL.Activity.supportFragmentManager.beginTransaction().add(R.id.opengl_main, OpenGLSettingsFragment(),"SettingsFragment").commit()
            CFGLEngine.disableTap = true
        }
    }

    companion object {

        // Shows this UI fragment
        fun show() {
            CFGL.Activity.supportFragmentManager.beginTransaction().add(R.id.opengl_main, OpenGLDeathFragment(),"DeathFragment").commit()
        }

        // Hides this UI fragment
        fun hide() {
            CFGL.Activity.supportFragmentManager.findFragmentByTag("DeathFragment")?.let {
                CFGL.Activity.supportFragmentManager.beginTransaction().setReorderingAllowed(true).remove(it).commit()
            }
        }
    }
}