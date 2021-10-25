package com.example.opengltest

import android.view.MotionEvent
import java.util.*
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

// This file is the main file describing the actual gameplay of the game

class CFGLEngine {
    companion object {
        //Variables to store information for game engine
        lateinit var player : Rectangle
        lateinit var backRect: Rectangle
        var obstacles : MutableList<Group> = emptyList<Group>().toMutableList()
        var obToRemove : Queue<Group> = LinkedList()

        lateinit var lastObstacle : Group
        var distanceBetweenObstacles = 1.5f
        var moveSpeedScalar = 1.0f
        var died = false
        var reset = true
        var disableTap = false
        var score = 0f

        // Variables to store player animations
        lateinit var left : Animation
        lateinit var right : Animation
        lateinit var center : Animation

        fun start() {
            // Activate the Gyro
            CFGLPhysicsController.useGyro()

            // Create the background object
            val bg1 = Vector2(-1f,-3f)
            val bg2 = Vector2(-1f,1f)
            val bg3 = Vector2(1f,1f)
            val bg4 = Vector2(1f,-3f)

            val background = loadTexture(R.drawable.backgroundspace)

            backRect = Rectangle(bg1, bg2, bg3, bg4, Color4(1f,1f,1f,1f), background)

            CFGL.Canvas.add(backRect)


            // Create the player object
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
            CFGL.Canvas.add(player)


            // Create the initial obstacle
            val obstacle = LevelEngine.genObstacle()
            obstacles.add(obstacle)
            CFGL.Canvas.add(obstacle)
            lastObstacle = obstacle

            // Show the main menu fragment
            OpenGLMainFragment.show()
        }

        fun update(deltaTime : Float, gyroData : Vector2) {
            reset = false

            // Move the background down
            backRect.move(Vector2(0.0f,-0.25f * deltaTime).correctAspect())

            // If the background is at the bottom move it back to the top
            if(backRect.getPos().y <= -1)
            {
                backRect.moveTo(Vector2(0.0f,1f))
            }

            // Process the Gyroscope data
            val horizMove = gyroData.x * deltaTime * 0.7f * moveSpeedScalar
            val vertMove = (gyroData.y + 0.0f) * deltaTime * 0.7f * moveSpeedScalar

            // Move the player according to the gyroscope data
            player.move(Vector2(horizMove,vertMove).correctAspect())

            // Set the player's animation according to the gyroscope data
            if(horizMove > 0.25 * deltaTime)
            {
                val current = player.getAnimation()
                if(current != right)
                {
                    // Set the animation data to keep the engine animation consistent
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
                    // Set the animation data to keep the engine animation consistent
                    left.currentFrame = current.currentFrame
                    left.framesOnKeyframe = current.framesOnKeyframe

                    player.setAnimation(left)
                }
            }
            else {
                val current = player.getAnimation()
                if(current != center)
                {
                    // Set the animation data to keep the engine animation consistent
                    center.currentFrame = current.currentFrame
                    center.framesOnKeyframe = current.framesOnKeyframe

                    player.setAnimation(center)
                }
            }

            // Make sure the player stays within bounds
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

            // For every obstacle
            for(obstacle in obstacles)
            {
                // Move the obstacle down the screen
                obstacle.move(Vector2(0.0f,-0.5f * deltaTime * moveSpeedScalar).correctAspect())

                // If the obstacle is at the bottom of the screen
                if(obstacle.getPos().y < -1.5f)
                {
                    // Remove it
                    CFGL.Canvas.remove(obstacle)
                    obToRemove.add(obstacle)
                }
                else
                {
                    // Otherwise, see if it hit the player
                    for(shape in obstacle.shapes)
                    {
                        if(checkCollision(shape,player))
                        {
                            // If it did, stop the game and show the death fragment
                            died = true
                            OpenGLDeathFragment.show()
                            CFGLPhysicsController.pause()
                        }
                    }
                }
            }

            // Increase the score by 10 per second
            score += deltaTime * 10
            OpenGLMainFragment.updateScore(" Score: " + score.toInt())

            // Remove all of the obstacles specified above
            // They could not be removed then because you cannot edit a list while iterating
            // through it
            while(!obToRemove.isEmpty())
            {
                obstacles.remove(obToRemove.poll())
            }

            // Check to see if the obstacles are at the minimum separation
            if(distanceBetweenObstacles / CFGL.Aspect > 0.65f)
            {
                // If they are not move them closer together
                distanceBetweenObstacles -= (deltaTime / 70f) / CFGL.Aspect
            }
            else
            {
                // Otherwise, make them move faster
                moveSpeedScalar += (deltaTime / 40f)
            }


            // Check to see if it is time to create a new obstacle
            if(1.5f - lastObstacle.getPos().y > distanceBetweenObstacles)
            {
                // If it is, make a new obstacle
                val obstacle = LevelEngine.genObstacle()
                lastObstacle = obstacle
                obstacles.add(obstacle)
                CFGL.Canvas.add(obstacle)
            }
        }

        // This function handles all of the touch events
        fun onTouch(e : MotionEvent) {
            if(!disableTap)
            {
                when (e.action) {
                    // This is called when the finger touches the screen
                    MotionEvent.ACTION_DOWN -> {
                        //
                        if(died)
                        {
                            // If the player has died, tapping resets the game
                            resetGame()
                            OpenGLDeathFragment.hide()
                            CFGLPhysicsController.resume()
                        }
                        else if(CFGLPhysicsController.isPaused()) {
                            // Or, if the player is alive and the game is paused, tapping resumes
                            // the game
                            CFGLPhysicsController.resume()
                            OpenGLPauseFragment.hide()
                        }
                        else {
                            // Otherwise, tapping pauses the game
                            CFGLPhysicsController.pause()
                            OpenGLPauseFragment.show()
                        }
                    }

                    // This is called when the finger moves on the screen
                    MotionEvent.ACTION_MOVE -> {

                    }

                    // This is called when the finger leaves the screen
                    MotionEvent.ACTION_UP -> {

                    }
                }
            }
        }

        // This function is called when the OpenGL Activity Closes
        fun onPause() {
            if(!reset) {
                OpenGLPauseFragment.show()
            }
        }

        // This function is called when the OpenGL Activity Resumes
        fun onResume() {

        }

        // This function resets the game engine
        fun resetGame() {
            reset = true

            //Remove all of the obstacles
            for(obstacle in obstacles)
            {
                obToRemove.add(obstacle)
                CFGL.Canvas.remove(obstacle)
            }
            while(!obToRemove.isEmpty())
            {
                obstacles.remove(obToRemove.poll())
            }

            // Reset the obstacle generation controls
            distanceBetweenObstacles = 1.5f
            moveSpeedScalar = 1f

            // Create the starting obstacle
            val obstacle = LevelEngine.genObstacle()
            obstacles.add(obstacle)
            CFGL.Canvas.add(obstacle)
            lastObstacle = obstacle

            // Reset the score
            score = 0f
            died = false
        }

        fun checkCollision(obstacle : Shape, player : Shape) : Boolean {
            var minDist =  collisionDistance[obstacle.asteroidType] + 0.1f;
            var playerPos = player.getPos()
            var obsPos = obstacle.getPos()

            var offset = Vector2(abs(obsPos.x - playerPos.x),abs(obsPos.y - playerPos.y) / CFGL.Aspect)

            var dist = sqrt(offset.x.toDouble().pow(2) + offset.y.toDouble().pow(2))
            return (dist < minDist)
        }

        // This variable stores the collision radii for the different asteroid sizes
        var collisionDistance : List<Float> = listOf(
            0.035f,
            0.082f,
            0.082f
        )
    }
}