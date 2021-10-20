package com.example.opengltest

import android.util.Log
import android.view.MotionEvent
import android.view.View
import java.util.*
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

class CFGLEngine {
    companion object {
        lateinit var player : Rectangle
        lateinit var backRect: Rectangle
        var obstacles : MutableList<Group> = emptyList<Group>().toMutableList()
        var obToRemove : Queue<Group> = LinkedList()

        lateinit var lastObstacle : Group
        var distanceBetweenObstacles = 1.5f
        var moveSpeedScalar = 1.0f
        var halt = false
        var died = false
        var disableTap = false
        var score = 0f

        lateinit var left : Animation
        lateinit var right : Animation
        lateinit var center : Animation

        fun start() {
            Log.d("OnCreate","Engine Started")
            CFGLPhysicsController.useGyro()

            val bg1 = Vector2(-1f,-3f)
            val bg2 = Vector2(-1f,1f)
            val bg3 = Vector2(1f,1f)
            val bg4 = Vector2(1f,-3f)

            val background = loadTexture(R.drawable.backgroundspace)

            backRect = Rectangle(bg1, bg2, bg3, bg4, Color4(1f,1f,1f,1f), background)

            CFGLCanvas.add(backRect)



            // Object Creation example
            // First load the textures
            center = Animation()
            center.FRAMES_PER_KEYFRAME = 20
            val centerNo = loadTexture(R.drawable.ship_default_noboost)
            val centerSmall = loadTexture(R.drawable.ship_default_small_boost)
            val centerBig = loadTexture(R.drawable.ship_default_big_boost)
            center.keyframes.add(centerNo)   //Add frames in the order they should be displayed
            center.keyframes.add(centerSmall)
            center.keyframes.add(centerBig)
            center.keyframes.add(centerSmall)

            right = Animation()
            right.FRAMES_PER_KEYFRAME = 20
            val rightNo = loadTexture(R.drawable.ship_right_noboost)
            val rightSmall = loadTexture(R.drawable.ship_right_small_boost)
            val rightBig = loadTexture(R.drawable.ship_right_big_boost)
            right.keyframes.add(rightNo)   //Add frames in the order they should be displayed
            right.keyframes.add(rightSmall)
            right.keyframes.add(rightBig)
            right.keyframes.add(rightSmall)

            left = Animation()
            left.FRAMES_PER_KEYFRAME = 20
            val leftNo = loadTexture(R.drawable.ship_left_no_boost)
            val leftSmall = loadTexture(R.drawable.ship_left_little_boost)
            val leftBig = loadTexture(R.drawable.ship_left_big_boost)
            left.keyframes.add(leftNo)   //Add frames in the order they should be displayed
            left.keyframes.add(leftSmall)
            left.keyframes.add(leftBig)
            left.keyframes.add(leftSmall)


            // Then create the rectangle and give it the animation or texture
            player = Rectangle(Vector2(0.25f, 0.25f).correctAspect(),center)

            // Then move it to its starting position
            player.moveTo(Vector2(0f,-0.8f))

            // And add the object to the canvas
            CFGLCanvas.add(player)


            // Create the initial obstacle
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

                val horizMove = gyroData.x * deltaTime * 0.7f * moveSpeedScalar
                val vertMove = (gyroData.y + 0.0f) * deltaTime * 0.7f * moveSpeedScalar

                player.move(Vector2(horizMove,vertMove).correctAspect())

                if(horizMove > 0.25 * deltaTime)
                {
                    val current = player.getAnimation()
                    if(current != right)
                    {
                        right.currentFrame = current.currentFrame
                        right.framesOnKeyframe = current.framesOnKeyframe
                        player.setAnimation(right)
                    }
                }
                else if(horizMove < -0.25 * deltaTime)
                {
                    val current = player.getAnimation()
                    if(current != left)
                    {
                        left.currentFrame = current.currentFrame
                        left.framesOnKeyframe = current.framesOnKeyframe
                        player.setAnimation(left)
                    }
                }
                else {
                    val current = player.getAnimation()
                    if(current != center)
                    {
                        center.currentFrame = current.currentFrame
                        center.framesOnKeyframe = current.framesOnKeyframe
                        player.setAnimation(center)
                    }
                }


                if(player.getPos().x > 0.85f)
                {
                    player.moveTo(Vector2(0.85f, player.getPos().y))
                }

                if(player.getPos().x < -0.85f)
                {
                    player.moveTo(Vector2(-0.85f, player.getPos().y))
                }

                if(player.getPos().y > 0.9f)
                {
                    player.moveTo(Vector2(player.getPos().x, 0.9f))
                }

                if(player.getPos().y < -0.9f)
                {
                    player.moveTo(Vector2(player.getPos().x, -0.9f))
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
                            if(checkCollision(shape,player))
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

                if(distanceBetweenObstacles / CFGLAspect > 0.65f)
                {
                    distanceBetweenObstacles -= (deltaTime / 70f) / CFGLAspect
                }
                else
                {
                    moveSpeedScalar += (deltaTime / 40f)
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
            if(!disableTap)
            {
                when (e.action) {
                    MotionEvent.ACTION_DOWN -> {

                        if(died)
                        {
                            resetGame()
                        }
                        else {
                            halt = !halt
                            CFGLActivity.togglePause()
                        }
                    }

                    MotionEvent.ACTION_MOVE -> {

                    }

                    MotionEvent.ACTION_UP -> {

                    }
                }
            }
        }

        fun resetGame() {
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
            died = false
        }

        fun checkCollision(obstacle : Shape, player : Shape) : Boolean {
            var minDist =  collisionDistance[obstacle.asteroidType] + 0.1f;
            var playerPos = player.getPos()
            var obsPos = obstacle.getPos()

            var offset = Vector2(abs(obsPos.x - playerPos.x),abs(obsPos.y - playerPos.y) / CFGLAspect)

            var dist = sqrt(offset.x.toDouble().pow(2) + offset.y.toDouble().pow(2))
            return (dist < minDist)
        }

        var collisionDistance : List<Float> = listOf(
            0.035f,
            0.082f,
            0.082f
        )
    }
}