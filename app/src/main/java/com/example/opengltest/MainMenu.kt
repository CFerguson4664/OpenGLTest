package com.example.opengltest

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlin.system.exitProcess

class MainMenu : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_menu);

        window.decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE or
        View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        )


        var backgroundVideo = findViewById<VideoView>(R.id.back_video)
        val videoUri = Uri.parse("android.resource://com.example.opengltest/" + R.raw.background_4);
        backgroundVideo.setVideoURI(videoUri);
        backgroundVideo.start();
        backgroundVideo.setOnCompletionListener {
            backgroundVideo.start();
        }

        val button_start = findViewById<Button>(R.id.button_start_game)
        button_start.setOnClickListener {

            if(!OpenGLActivity.running)
            {
                val intent = Intent(this, OpenGLActivity::class.java)
                startActivity(intent)
            }

            this@MainMenu.finish()
        }

        val button_quit = findViewById<Button>(R.id.button_quit_game)
        button_quit.setOnClickListener {
            if(OpenGLActivity.running) {
                CFGLActivity.finish()
            }

            this@MainMenu.finish()
            exitProcess(0)
        }
    }
}
