package com.example.opengltest

import android.content.Context
import android.opengl.GLSurfaceView
import android.view.MotionEvent
import android.widget.TextView

// Details on drawing graphics with OpenGL ES can be found here
// https://developer.android.com/training/graphics/opengl

class CFGLSurfaceView(context: Context) : GLSurfaceView(context) {
    private val renderer: CFGLRenderer

    init {
        // Create an OpenGL ES 2.0 context
        this.setEGLContextClientVersion(2)

        // Ask the device to hold on to the OpenGL context even when the app is in the
        // background
        this.preserveEGLContextOnPause = true

        // Create an instance of CFGLRenderer
        renderer = CFGLRenderer()

        // Set the Renderer for drawing on the GLSurfaceView
        this.setRenderer(renderer)
    }

    // Captures touch events and passes them to CFGLEngine.onTouch()
    override fun onTouchEvent(e: MotionEvent): Boolean {

        CFGLEngine.onTouch(e)

        return true
    }
}