package com.example.opengltest

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.nio.*

class Canvas {
    var graphics : MutableList<Graphic> = emptyList<Graphic>().toMutableList()

    fun draw() = runBlocking {
        for(i in graphics) {
            launch {
                i.draw()
            }
        }
    }
}

class Animation() {
    var keyframes : MutableList<Int> = emptyList<Int>().toMutableList()
    var FRAMES_PER_KEYFRAME = 1
    var currentFrame = 0
    var framesOnKeyframe = 0

    fun getFrame() : Int {
        var image : Int = keyframes[currentFrame]

        framesOnKeyframe++
        if(framesOnKeyframe == FRAMES_PER_KEYFRAME)
        {
            currentFrame++
            framesOnKeyframe = 0
        }

        if(currentFrame == keyframes.size)
        {
            currentFrame = 0
        }
        return image
    }
}

abstract class Graphic {
    abstract fun draw()
    abstract fun move(moveV: Vector2)
    abstract fun moveTo(posV: Vector2)
}

class Group : Graphic() {
    var shapes : MutableList<Shape> = emptyList<Shape>().toMutableList()

    override fun draw() {
        for(i in shapes) {
            i.draw()
        }
    }

    override fun move(moveV: Vector2) {
        TODO("Not yet implemented")
    }

    override fun moveTo(posV: Vector2) {
        TODO("Not yet implemented")
    }
}

abstract class Shape(var color: Color4) : Graphic() {
    protected var x: Float = 0f
    protected var y: Float = 0f
    protected var posValid: Boolean = false
    abstract fun getPos() : Vector2
    abstract fun wind() : FloatArray

    var vertexBufferValid : Boolean = false
    lateinit var vertexBuffer : FloatBuffer

    var textureBufferValid : Boolean = false
    lateinit var textureBuffer : FloatBuffer
}

class Triangle(var pt1: Vector2, var pt2: Vector2, var pt3: Vector2, inColor: Color4) : Shape(inColor) {

    private var texture = 0
    private var hasTexture = false

    constructor(pt1: Vector2, pt2: Vector2, pt3: Vector2, inColor: Color4, textureHandle : Int) : this(pt1, pt2, pt3, inColor) {
        texture = textureHandle
        hasTexture = true
    }

    override fun wind(): FloatArray {
        return floatArrayOf(
            pt1.x, pt1.y, 0.0f,
            pt2.x, pt2.y, 0.0f,
            pt3.x, pt3.y, 0.0f
        )
    }

    override fun draw() {
        if(hasTexture) {
            CFGLRenderer.texDrawObj(this, texture)
        }
        else {
            CFGLRenderer.drawObj(this)
        }
    }

    override fun getPos() : Vector2 {
        x = (pt1.x + pt2.x + pt3.x) / 3
        y = (pt1.y + pt2.y + pt3.y) / 3

        posValid = true

        return Vector2(x,y)
    }

    override fun moveTo(posV: Vector2) {
        if(!posValid)
        {
            getPos()
        }

        val moveV = Vector2(posV.x - x, posV.y - y)

        move(moveV)
    }

    override fun move(moveV: Vector2) {
        pt1 = Vector2(pt1.x + moveV.x, pt1.y + moveV.y)
        pt2 = Vector2(pt2.x + moveV.x, pt2.y + moveV.y)
        pt3 = Vector2(pt3.x + moveV.x, pt3.y + moveV.y)

        posValid = false
        vertexBufferValid = false
    }
}

class Rectangle(var pt1: Vector2, var pt2: Vector2, var pt3: Vector2, var pt4: Vector2, var inColor: Color4) : Shape(inColor){

    private var texture = 0
    private var hasTexture = false

    private lateinit var animation : Animation
    private var hasAnimation = false

    constructor(pt1: Vector2, pt2: Vector2, pt3: Vector2, pt4: Vector2, inColor: Color4, textureHandle : Int) : this(pt1, pt2, pt3, pt4, inColor) {
        texture = textureHandle
        hasTexture = true
    }

    constructor(pt1: Vector2, pt2: Vector2, pt3: Vector2, pt4: Vector2, inColor: Color4, animationIn: Animation) : this(pt1, pt2, pt3, pt4, inColor) {
        animation = animationIn
        hasAnimation = true
    }

    override fun wind() : FloatArray {
        return floatArrayOf(
            pt1.x, pt1.y, 0.0f,
            pt2.x, pt2.y, 0.0f,
            pt3.x, pt3.y, 0.0f,

            pt1.x, pt1.y, 0.0f,
            pt3.x, pt3.y, 0.0f,
            pt4.x, pt4.y, 0.0f
        )
    }

    override fun draw() {
        if(hasAnimation) {
            CFGLRenderer.texDrawObj(this, animation.getFrame())
        }
        else if(hasTexture) {
            CFGLRenderer.texDrawObj(this, texture)
        }
        else {
            CFGLRenderer.drawObj(this)
        }
    }

    override fun getPos() : Vector2 {
        x = (pt1.x + pt2.x + pt3.x + pt4.x) / 4
        y = (pt1.y + pt2.y + pt3.y + pt4.y) / 4

        posValid = true

        return Vector2(x,y)
    }

    override fun moveTo(posV: Vector2) {
        val moveV = Vector2(posV.x - getPos().x, posV.y - getPos().y)

        this.move(moveV)
    }

    override fun move(moveV: Vector2) {
        pt1 = Vector2(pt1.x + moveV.x, pt1.y + moveV.y)
        pt2 = Vector2(pt2.x + moveV.x, pt2.y + moveV.y)
        pt3 = Vector2(pt3.x + moveV.x, pt3.y + moveV.y)
        pt4 = Vector2(pt4.x + moveV.x, pt4.y + moveV.y)

        posValid = false
        vertexBufferValid = false
    }
}


