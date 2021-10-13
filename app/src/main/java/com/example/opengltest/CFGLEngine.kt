package com.example.opengltest

import android.util.Log
import android.view.MotionEvent
import java.util.*
import kotlin.math.pow

class CFGLEngine {
    companion object {
        lateinit var rect : Rectangle
        lateinit var backRect: Rectangle
        var obstacles : MutableList<Group> = emptyList<Group>().toMutableList()
        var obToRemove : Queue<Group> = LinkedList()

        lateinit var lastObstacle : Group
        var distanceBetweenObstacles = 1.5f
        var moveSpeedScalar = 1.0f
        var halt = false
        var died = false
        var score = 0f

        fun start() {
            CFGLPhysicsController.useGyro()

            val bg1 = Vector2(-1f,-3f)
            val bg2 = Vector2(-1f,1f)
            val bg3 = Vector2(1f,1f)
            val bg4 = Vector2(1f,-3f)

            val background = loadTexture(R.drawable.backgroundtest)

            backRect = Rectangle(bg1, bg2, bg3, bg4, Color4(1f,1f,1f,1f), background)

            CFGLCanvas.add(backRect)



            // Object Creation example
            // First load the textures
            val frame1 = loadTexture(R.drawable.pixil_frame_1)
            val frame3 = loadTexture(R.drawable.pixil_frame_4)

            // Then load them into an animation if needed
            var anim = Animation()
            anim.keyframes.add(frame1)
            anim.keyframes.add(frame3)
            anim.FRAMES_PER_KEYFRAME = 30

            // Then create the rectand and give it the animation or texture
            rect = Rectangle(Vector2(0.3f, 0.3f).correctAspect(),frame3)

            // Then move it to its starting position
            rect.moveTo(Vector2(0f,-0.8f))

            // And add the object to the canvas
            CFGLCanvas.add(rect)

            val obstacle = LevelEngine.genObstacle()
            obstacles.add(obstacle)
            CFGLCanvas.add(obstacle)
            lastObstacle = obstacle
        }

        fun update(deltaTime : Float, gyroData : Vector2) {

            if(!halt)
            {
                backRect.move(Vector2(0.0f,-0.25f * deltaTime).correctAspect())

                if(backRect.getPos().y <= -1)
                {
                    backRect.moveTo(Vector2(0.0f,1f))
                }

                rect.move(Vector2(gyroData.x * deltaTime * 0.7f * moveSpeedScalar, (gyroData.y + 0.0f) * deltaTime * 0.7f * moveSpeedScalar).correctAspect())
                if(rect.getPos().x > 0.85f)
                {
                    rect.moveTo(Vector2(0.85f, rect.getPos().y))
                }

                if(rect.getPos().x < -0.85f)
                {
                    rect.moveTo(Vector2(-0.85f, rect.getPos().y))
                }

                if(rect.getPos().y > 0.9f)
                {
                    rect.moveTo(Vector2(rect.getPos().x, 0.9f))
                }

                if(rect.getPos().y < -0.9f)
                {
                    rect.moveTo(Vector2(rect.getPos().x, -0.9f))
                }

                for(obstacle in obstacles)
                {
                    obstacle.move(Vector2(0.0f,-0.5f * deltaTime * moveSpeedScalar).correctAspect())
                    if(obstacle.getPos().y < -1.5f)
                    {
                        CFGLCanvas.remove(obstacle)
                        obToRemove.add(obstacle)
                    }
                    else
                    {
                        for(shape in obstacle.shapes)
                        {
                            if(shape.getBounds().contains(rect.getPos()))
                            {
                                halt = true
                                died = true
                            }
                        }
                    }
                }

                score += deltaTime * 10
                CFGLActivity.updateText(" Score: " + score.toInt())




                while(!obToRemove.isEmpty())
                {
                    obstacles.remove(obToRemove.poll())
                }

                if(distanceBetweenObstacles > 0.3f)
                {
                    distanceBetweenObstacles -= (deltaTime / 100f)
                }
                else
                {
                    moveSpeedScalar += (deltaTime / 50f)
                }

                if(1.5f - lastObstacle.getPos().y > distanceBetweenObstacles)
                {
                    val obstacle = LevelEngine.genObstacle()
                    lastObstacle = obstacle
                    obstacles.add(obstacle)
                    CFGLCanvas.add(obstacle)
                }
            }
        }

        fun onTouch(e : MotionEvent) {
            when (e.action) {
                MotionEvent.ACTION_DOWN -> {

                    if(died)
                    {
                        for(obstacle in obstacles)
                        {
                            obToRemove.add(obstacle)
                            CFGLCanvas.remove(obstacle)
                        }

                        while(!obToRemove.isEmpty())
                        {
                            obstacles.remove(obToRemove.poll())
                        }

                        distanceBetweenObstacles = 1.5f
                        moveSpeedScalar = 1f
                        halt = false

                        val obstacle = LevelEngine.genObstacle()
                        obstacles.add(obstacle)
                        CFGLCanvas.add(obstacle)
                        lastObstacle = obstacle
                        score = 0f
                    }
                    else {
                        halt = !halt
                    }
                }

                MotionEvent.ACTION_MOVE -> {

                }

                MotionEvent.ACTION_UP -> {

                }
            }
        }
    }
}