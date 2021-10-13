package com.example.opengltest

import android.util.Log

class ObstacleLayout(var id : Int, var p1 : Boolean, var p2 : Boolean, var p3 : Boolean, var p4 : Boolean, var p5 : Boolean, var p6 : Boolean) {
    var layout : List<Boolean> = listOf(p1,p2,p3,p4,p5,p6)
}

class LevelEngine {
    companion object {
        var lastObstacle = 2;
        var txt = loadTexture(R.drawable.whitesquare)

        var colors : List<Color4> = listOf(
            Color4(1.0f,1.0f,1.0f,1.0f),
            Color4(1.0f,0.0f,0.0f,1.0f),
            Color4(0.0f,1.0f,0.0f,1.0f),
            Color4(0.0f,0.0f,1.0f,1.0f),
            Color4(0.5f,1.0f,1.0f,1.0f),
            Color4(1.0f,0.5f,1.0f,1.0f),
            Color4(1.0f,1.0f,0.5f,1.0f)
        )

        fun genObstacle() : Group{
            val randLayout = (0 until ObstacleTable.obstacleTable[lastObstacle].size).random()

            val layout = ObstacleTable.obstacleTable[lastObstacle][randLayout]
            var group = Group()

            for(i in 0 until 6) {
                if(layout.layout[i])
                {
                    val randColor = (0 until colors.size - 1).random()
                    var rect = Rectangle(Vector2(0.3f,0.3f).correctAspect(),  colors[randColor])
                    rect.moveTo(Vector2(-0.75f + (0.3f * i), 1.5f))

                    group.shapes.add(rect)
                }
            }

            lastObstacle = layout.id
            return group
        }
    }
}

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