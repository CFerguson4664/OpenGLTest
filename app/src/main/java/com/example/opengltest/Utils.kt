package com.example.opengltest

fun screenToGraphicsCords(x : Float, y : Float) : Vector2 {
    val converted = Vector2(((x - (CFGLWidth / 2f)) / CFGLWidth) * 2 , ((y - (CFGLHeight / 2f)) / -CFGLHeight) * 2)

    return converted
}

class Vector2(var x: Float, var y: Float) {
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

class Frame(var texture : Int, var color : Color4)
{

}


class Bounds(var maxX : Float, var minX : Float, var maxY : Float, var minY : Float) {

    constructor(start : Vector2) : this(start.x, start.x, start.y, start.y)

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

    fun width() : Float {
        return maxX - minX
    }

    fun height() : Float {
        return maxY - minY
    }

    fun contains(pt : Vector2) : Boolean {
        return (pt.x > minX && pt.x < maxX && pt.y > minY && pt.y < maxY)
    }
}