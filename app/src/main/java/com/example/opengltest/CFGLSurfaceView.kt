package com.example.opengltest

import android.content.Context
import android.opengl.GLSurfaceView
import android.view.MotionEvent
import android.widget.TextView

class CFGLSurfaceView(context: Context) : GLSurfaceView(context) {
    private val renderer: CFGLRenderer

    init {
        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2)

        renderer = CFGLRenderer()

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(renderer)
        this.preserveEGLContextOnPause = true
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {

        CFGLEngine.onTouch(e)

        return true
    }

    fun close() {
        CFGLPhysicsController.stop()
    }
}