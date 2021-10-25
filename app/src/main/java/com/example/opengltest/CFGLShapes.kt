package com.example.opengltest

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.nio.*
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

// The canvas class is used to contain all of the objects being displayed on the OpenGL View
// at any given time
class Canvas {
    // Stores the graphics objects that should be rendered on the canvas
    private var graphics : MutableList<Graphic> = emptyList<Graphic>().toMutableList()

    // These queues control adding and removing objects from the canvas
    // Queues are used to prevent object from being added or removed while the canvas is
    // is being drawn. Objects are instead added to the queue and then added or removed later
    private var addQueue : BlockingQueue<Graphic> = LinkedBlockingDeque()
    private var removeQueue : BlockingQueue<Graphic> = LinkedBlockingDeque()

    // Empties the add and remove queues and applies the changes to the list of graphics
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

    // Starts a thread pool to draw all of the objects on the canvas
    // Not really sure if this is faster than just doing it with a single thread, but it is
    // working and I am happy with the performance, so I'm not going to change it
    //
    // The multithreading method is detailed here
    // https://kotlinlang.org/docs/coroutines-basics.html
    fun draw() = runBlocking {
        for(i in graphics) {
            launch {
                i.draw()
            }
        }
    }

    // Adds the object to the add queue
    fun add(graphic: Graphic)
    {
        addQueue.put(graphic)
    }

    // Adds the object to the remove queue
    fun remove(graphic: Graphic)
    {
        removeQueue.put(graphic)
    }
}

// This class handles animating the textures on shapes
class Animation() {
    // The number of frames to show each image before switching
    var FRAMES_PER_KEYFRAME = 1

    // The frames of this animation
    var keyframes : MutableList<Int> = emptyList<Int>().toMutableList()

    // The colors used to tint the frames of the animation
    // If used this must be the same length as keyframes
    var colors : MutableList<Color4> = emptyList<Color4>().toMutableList()
    var useColors = false;

    // Stores what keyframe of the animation is currently being displayed
    var currentFrame = 0

    // Stores how many frames we have been displaying the current keyframe of the animation
    var framesOnKeyframe = 0


    // Gets the next frame of the animation to be drawn
    fun getFrame() : Frame {
        // Set the image for the frame to be the current keyframe of the animation
        var image : Int = keyframes[currentFrame]

        // Default the tint color to white (Don't tint)
        var color = Color4(1.0f,1.0f,1.0f,1.0f)

        // If we are using colors, add the color to the frame
        if(useColors)
        {
            color = colors[currentFrame]
        }

        // If the physics engine is paused we do not need to show animations
        if(!CFGLPhysicsController.isPaused())
        {
            // Increment the number of frames this keyframe has been displayed for
            framesOnKeyframe++

            // If we have displayed the keyframe for the correct number of frames
            if(framesOnKeyframe == FRAMES_PER_KEYFRAME)
            {
                // Move onto the next keyframe of the animation
                currentFrame++
                framesOnKeyframe = 0
            }

            // If are through the last frame of the animation
            if(currentFrame == keyframes.size)
            {
                // Start over on the first frame
                currentFrame = 0
            }
        }

        return Frame(image, color)
    }
}

// The base class for all objects displayed on the OpenGL View
abstract class Graphic {
    // Draws the Graphic on the OpenGL View
    abstract fun draw()

    // Moves the object the amount specified by moveV
    abstract fun move(moveV: Vector2)

    // Moves the object to the position specified by moveV
    abstract fun moveTo(posV: Vector2)
}

// Used to allow multiple shapes to be moved together easily
class Group : Graphic() {
    var shapes : MutableList<Shape> = emptyList<Shape>().toMutableList()

    // Draws the shapes on the OpenGL View
    override fun draw() {
        for(i in shapes) {
            i.draw()
        }
    }

    // Moves all shapes in the group the amount specified by moveV
    override fun move(moveV: Vector2) {
        for(shape in shapes) {
            shape.move(moveV)
        }
    }

    // Move the center of the group to the position specified by moveV
    override fun moveTo(posV: Vector2) {
        val pos = getPos()
        var moveV = Vector2(posV.x - pos.x, posV.y - pos.y)

        for(shape in shapes)
        {
            shape.move(moveV)
        }
    }

    // Get the position of the center of the group of shapes
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

// The base class for all shapes
abstract class Shape(var color: Color4) : Graphic() {
    val lock = ReentrantLock()
    protected var x: Float = 0f
    protected var y: Float = 0f
    protected var posValid: Boolean = false

    // Gets the position of the center of this shapeS
    abstract fun getPos() : Vector2

    // Used to generate the vertex array for this shape
    // This provides detail on the general idea, but here a simpler implementation is used
    // since the number of vertices is small
    // https://www.khronos.org/opengl/wiki/Vertex_Specification
    abstract fun wind() : FloatArray

    // Finds the rectangular bounds that contain this shape
    abstract fun getBounds() : Bounds

    var asteroidType = 0

    // The vertex buffer stores the coordinates used by  OpenGL to draw the object
    var vertexBufferValid : Boolean = false
    lateinit var vertexBuffer : FloatBuffer

    // The texture buffer stores teh coordinates used by OpenGL to sample the texture
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



    // Sets the animation displayed on the rectangle
    fun setAnimation(anim : Animation) {
        animLock.withLock {
            animation = anim
            hasAnimation = true
        }
    }

    // Gets the animation currently being displayed on the rectangle
    fun getAnimation() : Animation {
        return animation
    }


    // Used to generate the vertex array for this rectangle
    // This provides detail on the general idea, but here a simpler implementation is used
    // since the number of vertices is small
    // https://www.khronos.org/opengl/wiki/Vertex_Specification
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

    // Draws the rectangle on the OpenGL View
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

    // Gets the position of the center of the rectangle
    override fun getPos() : Vector2 {
        if(!posValid)
        {
            x = (pt1.x + pt2.x + pt3.x + pt4.x) / 4
            y = (pt1.y + pt2.y + pt3.y + pt4.y) / 4
            posValid = true
        }

        return Vector2(x,y)
    }

    // Moves the center of the rectangle to the coordinate specified by moveV
    override fun moveTo(posV: Vector2) {
        val moveV = Vector2(posV.x - getPos().x, posV.y - getPos().y)

        this.move(moveV)
    }

    // Moves the rectangle the amount specified by moveV
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

    // Finds the rectangular bounds that contain this rectangle
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


