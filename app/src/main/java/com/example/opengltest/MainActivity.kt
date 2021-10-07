package com.example.opengltest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

lateinit var CFGLView : CFGLSurfaceView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create a GLSurfaceView instance and set it
        // as the ContentView for this Activity.
        CFGLView = CFGLSurfaceView(this)
        setContentView(CFGLView)
    }
}