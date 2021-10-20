package com.example.opengltest

import android.graphics.Color
import android.widget.TextView
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.nio.*
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class Canvas {

    private var graphics : MutableList<Graphic> = emptyList<Graphic>().toMutableList()

    private var addQueue : BlockingQueue<Graphic> = LinkedBlockingDeque()
    private var removeQueue : BlockingQueue<Graphic> = LinkedBlockingDeque()

    fun modify() {
        while(!removeQueue.isEmpty())
        {
            graphics.remove(removeQueue.poll())
        }

        while(!addQueue.isEmpty())
        {
            graphics.add(addQueue.poll())
        }
    }

    fun draw() = runBlocking {
        for(i in graphics) {
            launch {
                i.draw()
            }
        }
    }

    fun add(graphic: Graphic)
    {
        addQueue.put(graphic)
    }

    fun remove(graphic: Graphic)
    {
        removeQueue.put(graphic)
    }

    fun numGraphics() : Int {
        return graphics.size
    }
}

class Animation() {
    var keyframes : MutableList<Int> = emptyList<Int>().toMutableList()
    var colors : MutableList<Color4> = emptyList<Color4>().toMutableList()
    var FRAMES_PER_KEYFRAME = 1
    var currentFrame = 0
    var framesOnKeyframe = 0
    var useColors = false;

    fun getFrame() : Frame {
        var image : Int = keyframes[currentFrame]
        var color = Color4(1.0f,1.0f,1.0f,1.0f)

        if(useColors)
        {
            color = colors[currentFrame]
        }

        if(!CFGLActivity.paused)
        {
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
        }

        return Frame(image, color)
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
        for(shape in shapes) {
            shape.move(moveV)
        }
    }

    override fun moveTo(posV: Vector2) {
        val pos = getPos()
        var moveV = Vector2(posV.x - pos.x, posV.y - pos.y)

        for(shape in shapes)
        {
            shape.move(moveV)
        }
    }

    fun getPos() : Vector2 {
        var x = 0f
        var y = 0f

        for(shape in shapes)
        {
            val pos = shape.getPos()
            x += pos.x
            y += pos.y
        }

        x /= shapes.size
        y /= shapes.size

        return Vector2(x,y)
    }
}

abstract class Shape(var color: Color4) : Graphic() {
    val lock = ReentrantLock()
    protected var x: Float = 0f
    protected var y: Float = 0f
    protected var posValid: Boolean = false
    abstract fun getPos() : Vector2
    abstract fun wind() : FloatArray
    abstract fun getBounds() : Bounds
    var asteroidType = 0

    var vertexBufferValid : Boolean = false
    lateinit var vertexBuffer : FloatBuffer

    var textureBufferValid : Boolean = false
    lateinit var textureBuffer : FloatBuffer
}

class Rectangle(var pt1: Vector2, var pt2: Vector2, var pt3: Vector2, var pt4: Vector2, inColor: Color4) : Shape(inColor){

    private var texture = 0
    private var hasTexture = false

    // Lock is used to prevent changing of texture while drawing
    val animLock = ReentrantLock()
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


    constructor(ptCenter: Vector2, width: Float, height: Float, inColor : Color4) : this(
        Vector2(ptCenter.x - (width / 2), ptCenter.y - (height / 2)),
        Vector2(ptCenter.x - (width / 2), ptCenter.y + (height / 2)),
        Vector2(ptCenter.x + (width / 2), ptCenter.y + (height / 2)),
        Vector2(ptCenter.x + (width / 2), ptCenter.y - (height / 2)),
        inColor
    )

    constructor(ptCenter: Vector2, size : Vector2, textureHandle: Int) : this(
        Vector2(ptCenter.x - (size.x / 2), ptCenter.y - (size.y / 2)),
        Vector2(ptCenter.x - (size.x / 2), ptCenter.y + (size.y / 2)),
        Vector2(ptCenter.x + (size.x / 2), ptCenter.y + (size.y / 2)),
        Vector2(ptCenter.x + (size.x / 2), ptCenter.y - (size.y / 2)),
        Color4(1.0f, 1.0f, 1.0f, 1.0f)
    ) {
        texture = textureHandle
        hasTexture = true
    }

    constructor(ptCenter: Vector2, size : Vector2, animationIn: Animation) : this(
        Vector2(ptCenter.x - (size.x / 2), ptCenter.y - (size.y / 2)),
        Vector2(ptCenter.x - (size.x / 2), ptCenter.y + (size.y / 2)),
        Vector2(ptCenter.x + (size.x / 2), ptCenter.y + (size.y / 2)),
        Vector2(ptCenter.x + (size.x / 2), ptCenter.y - (size.y / 2)),
        Color4(1.0f, 1.0f, 1.0f, 1.0f)
    ) {
        animation = animationIn
        hasAnimation = true
    }



    constructor(size : Vector2, inColor : Color4) : this(
        Vector2(0f - (size.x / 2), 0f - (size.y / 2)),
        Vector2(0f - (size.x / 2), 0f + (size.y / 2)),
        Vector2(0f + (size.x / 2), 0f + (size.y / 2)),
        Vector2(0f + (size.x / 2), 0f - (size.y / 2)),
        inColor
    )

    constructor(size : Vector2, textureHandle: Int) : this(
        Vector2(0f - (size.x / 2), 0f - (size.y / 2)),
        Vector2(0f - (size.x / 2), 0f + (size.y / 2)),
        Vector2(0f + (size.x / 2), 0f + (size.y / 2)),
        Vector2(0f + (size.x / 2), 0f - (size.y / 2)),
        Color4(1.0f, 1.0f, 1.0f, 1.0f)
    ) {
        texture = textureHandle
        hasTexture = true
    }

    constructor(size : Vector2, textureHandle: Int, color: Color4) : this(
        Vector2(0f - (size.x / 2), 0f - (size.y / 2)),
        Vector2(0f - (size.x / 2), 0f + (size.y / 2)),
        Vector2(0f + (size.x / 2), 0f + (size.y / 2)),
        Vector2(0f + (size.x / 2), 0f - (size.y / 2)),
        color
    ) {
        texture = textureHandle
        hasTexture = true
    }

    constructor(size : Vector2, animationIn: Animation) : this(
        Vector2(0f - (size.x / 2), 0f - (size.y / 2)),
        Vector2(0f - (size.x / 2), 0f + (size.y / 2)),
        Vector2(0f + (size.x / 2), 0f + (size.y / 2)),
        Vector2(0f + (size.x / 2), 0f - (size.y / 2)),
        Color4(1.0f, 1.0f, 1.0f, 1.0f)
    ) {
        animation = animationIn
        hasAnimation = true
    }

    fun setAnimation(anim : Animation) {
        animLock.withLock {
            animation = anim
            hasAnimation = true
        }
    }

    fun getAnimation() : Animation {
        return animation
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
            animLock.withLock {
                CFGLRenderer.texDrawObj(this, animation.getFrame())
            }
        }
        else if(hasTexture) {
            CFGLRenderer.texDrawObj(this, Frame(texture, this.color))
        }
        else {
            CFGLRenderer.drawObj(this)
        }
    }

    override fun getPos() : Vector2 {
        if(!posValid)
        {
            x = (pt1.x + pt2.x + pt3.x + pt4.x) / 4
            y = (pt1.y + pt2.y + pt3.y + pt4.y) / 4
            posValid = true
        }

        return Vector2(x,y)
    }

    override fun moveTo(posV: Vector2) {
        val moveV = Vector2(posV.x - getPos().x, posV.y - getPos().y)

        this.move(moveV)
    }

    override fun move(moveV: Vector2) {
        lock.withLock {
            pt1 = Vector2(pt1.x + moveV.x, pt1.y + moveV.y)
            pt2 = Vector2(pt2.x + moveV.x, pt2.y + moveV.y)
            pt3 = Vector2(pt3.x + moveV.x, pt3.y + moveV.y)
            pt4 = Vector2(pt4.x + moveV.x, pt4.y + moveV.y)
        }

        posValid = false
        vertexBufferValid = false
    }

    override fun getBounds() : Bounds {
        lock.withLock {
            var bounds = Bounds(pt1)
            bounds.compare(pt2)
            bounds.compare(pt3)
            bounds.compare(pt4)
            return bounds
        }
    }
}


