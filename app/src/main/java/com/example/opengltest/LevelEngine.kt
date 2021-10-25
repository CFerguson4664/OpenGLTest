package com.example.opengltest

// Stores a template for making an obstacle
class ObstacleLayout(var id : Int, var p1 : Boolean, var p2 : Boolean, var p3 : Boolean, var p4 : Boolean, var p5 : Boolean, var p6 : Boolean) {
    var layout : List<Boolean> = listOf(p1,p2,p3,p4,p5,p6)
}

// This class is responsible for randomly generating the obstacles in the game
class LevelEngine {
    companion object {
        var lastObstacle = 2;

        // Get all of the possible asteroid textures
        var textures : List<Int> = listOf(
            loadTexture(R.drawable.asteroid_small),
            loadTexture(R.drawable.asteroid_medium),
            loadTexture(R.drawable.asteroid_big)
        )

        // Creates an obstacle
        fun genObstacle() : Group {
            // Pick a random valid obstacle template
            val randLayout = (ObstacleTable.obstacleTable[lastObstacle].indices).random()
            val layout = ObstacleTable.obstacleTable[lastObstacle][randLayout]

            // Create a group to apply the template too
            var group = Group()

            // Apply the template to the group
            for(i in 0 until 6) {
                // The point in the layout is true create a randome asteroid, if it is false, leave
                // it blank
                if(layout.layout[i])
                {
                    // Pick a random asteroid texture
                    val randTexture = (textures.indices).random()

                    // Create the rectangle and apply the texture to it
                    var rect = Rectangle(Vector2(0.3f,0.3f).correctAspect(),  textures[randTexture])

                    // Move it into position
                    rect.moveTo(Vector2(-0.75f + (0.3f * i), 1.5f))
                    rect.asteroidType = randTexture

                    // Add the rectangle to the group
                    group.shapes.add(rect)
                }
            }

            lastObstacle = layout.id

            // Return the group
            return group
        }
    }
}

// This is the table that determines what order the obstacles can spawn in. It ensures the obstacles are passable
class ObstacleTable {
    companion object {
        var obstacleTable: List<List<ObstacleLayout>> = listOf(
            listOf(
                ObstacleLayout(0, false, true, true, true, true, true),
                ObstacleLayout(1, true, false, true, true, true, true)
            ),
            listOf(
                ObstacleLayout(0, false, true, true, true, true, true),
                ObstacleLayout(1, true, false, true, true, true, true),
                ObstacleLayout(2, true, true, false, true, true, true)
            ),
            listOf(
                ObstacleLayout(1, true, false, true, true, true, true),
                ObstacleLayout(2, true, true, false, true, true, true),
                ObstacleLayout(3, true, true, true, false, true, true)
            ),
            listOf(
                ObstacleLayout(2, true, true, false, true, true, true),
                ObstacleLayout(3, true, true, true, false, true, true),
                ObstacleLayout(4, true, true, true, true, false, true)
            ),
            listOf(
                ObstacleLayout(3, true, true, true, false, true, true),
                ObstacleLayout(4, true, true, true, true, false, true),
                ObstacleLayout(5, true, true, true, true, true, false)
            ),
            listOf(
                ObstacleLayout(4, true, true, true, true, false, true),
                ObstacleLayout(5, true, true, true, true, true, false)
            )
        )
    }
}