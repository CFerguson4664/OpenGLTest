package com.example.opengltest

import android.view.MotionEvent
import kotlin.math.pow

class CFGLEngine {
    companion object {
        lateinit var rect : Rectangle
        lateinit var rect2 : Rectangle
        lateinit var backRect: Rectangle

        lateinit var tri : Triangle
        var directionRight : Boolean = true

        fun start() {
            val pt1 = Vector2(-0.15f,-0.15f).correctAspect()
            val pt2 = Vector2(-0.15f, 0.15f).correctAspect()
            val pt3 = Vector2(0.15f, 0.15f).correctAspect()
            val pt4 = Vector2(0.15f, -0.15f).correctAspect()

            val pt12 = Vector2(-0.9f,-0.3f).correctAspect()
            val pt22 = Vector2(-0.9f, 0.3f).correctAspect()
            val pt32 = Vector2(-0.6f, 0.3f).correctAspect()
            val pt42 = Vector2(-0.6f, -0.3f).correctAspect()

            val pt5 = Vector2(0.0f, -0.3f).correctAspect()

            val textureHandle = loadTexture(R.drawable.pikachu)
            val textureHandle2 = loadTexture(R.drawable.shrek)


            val bg1 = Vector2(-1f,-3f)
            val bg2 = Vector2(-1f,1f)
            val bg3 = Vector2(1f,1f)
            val bg4 = Vector2(1f,-3f)

            val background = loadTexture(R.drawable.backgroundtest)

            backRect = Rectangle(bg1, bg2, bg3, bg4, Color4(1f,1f,1f,1f), background)

            val frame1 = loadTexture(R.drawable.pixil_frame_1)
            val frame2 = loadTexture(R.drawable.pixil_frame_0)
            val frame3 = loadTexture(R.drawable.pixil_frame_4)

            var anim = Animation()
            anim.keyframes.add(frame1)
            //anim.keyframes.add(frame2)
            anim.keyframes.add(frame3)
            anim.FRAMES_PER_KEYFRAME = 30


            // With texture
            rect2 = Rectangle(pt12, pt22, pt32, pt42, Color4(1.0f, 1.0f, 1.0f, 1.0f), textureHandle2)
            rect = Rectangle(pt1, pt2, pt3, pt4, Color4(1.0f,1.0f,1.0f,1.0f), anim)
            //tri = Triangle(pt5, pt2, pt3, Color4(1.0f,1.0f,1.0f,1.0f), textureHandle)

            // Without texture
            //rect = Rectangle(pt1, pt2, pt3, pt4, Color4(1.0f,1.0f,1.0f,1.0f))
            tri = Triangle(pt5, pt2, pt3, Color4(0.0f,1.0f,1.0f,0.3f))


            CFGLCanvas.graphics.add(backRect)
            CFGLCanvas.graphics.add(rect2)
            CFGLCanvas.graphics.add(tri)
            CFGLCanvas.graphics.add(rect)
        }

        fun update(deltaTime : Float) {
            backRect.move(Vector2(0.0f,-0.5f * deltaTime).correctAspect())

            if(backRect.getPos().y <= -1)
            {
                backRect.moveTo(Vector2(0.0f,1f))
            }


            if(directionRight and (rect.getPos().x < 1f))
            {
                rect.move(Vector2(2f * deltaTime,0f))

                if(rect.getPos().x > 1f)
                {
                    rect.moveTo(Vector2(1f, rect.getPos().y))
                }
            }
            else if (!directionRight and (rect.getPos().x > -1f))
            {
                rect.move(Vector2(-2f * deltaTime,0f))

                if(rect.getPos().x < -1f)
                {
                    rect.moveTo(Vector2(-1f, rect.getPos().y))
                }
            }

            val const = 0.3f
            val y = ((0.5f) - ((rect.getPos().x).pow(2.0f)  * const))

            rect.moveTo(Vector2(rect.getPos().x, y) )
        }

        fun onTouch(e : MotionEvent) {
            when (e.action) {
                MotionEvent.ACTION_DOWN -> {
                    directionRight = !directionRight
                }

                MotionEvent.ACTION_MOVE -> {

                }

                MotionEvent.ACTION_UP -> {

                }
            }
        }
    }
}