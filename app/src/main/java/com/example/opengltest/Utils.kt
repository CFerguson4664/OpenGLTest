package com.example.opengltest

// Can be used to convert the coordinates of a touch input to coordinate that match
// the graphics engine
fun screenToGraphicsCords(x: Float, y: Float): Vector2 {
    return Vector2(
        ((x - (CFGLWidth / 2f)) / CFGLWidth) * 2,
        ((y - (CFGLHeight / 2f)) / -CFGLHeight) * 2
    )
}

class Vector2(var x: Float, var y: Float) {

    // Used to modify the y value of the Vector2 so that equivalent values
    // will appear the same size when displayed
    fun correctAspect() : Vector2 {
        y *= CFGLAspect
        return this
    }
}

class Color4(var r: Float, var g: Float, var b: Float, var a: Float) {
    fun getColor() : FloatArray {
        return floatArrayOf(r, g, b, a)
    }
}

class Frame(var texture : Int, var color : Color4) {}


// Used to find the rectangular bounds around a group of points
class Bounds(var maxX : Float, var minX : Float, var maxY : Float, var minY : Float) {

    // Create the bounds starting a single point
    constructor(start : Vector2) : this(start.x, start.x, start.y, start.y)

    // Add another point to the bounds
    fun compare(pt : Vector2) {
        if(pt.x > maxX) {
            maxX = pt.x
        }
        else if(pt.x < minX) {
            minX = pt.x
        }

        if(pt.y > maxY) {
            maxY = pt.y
        }
        else if(pt.y < minY) {
            minY = pt.y
        }
    }

    // Get the with of the bounding rectangle
    fun width() : Float {
        return maxX - minX
    }

    // Get the height of the bounding rectangle
    fun height() : Float {
        return maxY - minY
    }

    // Determine if a point is within the bounding rectangle
    fun contains(pt : Vector2) : Boolean {
        return (pt.x > minX && pt.x < maxX && pt.y > minY && pt.y < maxY)
    }
}