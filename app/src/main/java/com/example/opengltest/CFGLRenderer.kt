package com.example.opengltest

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.concurrent.withLock

var CFGLWidth : Int = 0
var CFGLHeight : Int = 0
var CFGLAspect : Float = 0.0f
var CFGLCanvas : Canvas = Canvas()


class CFGLRenderer : GLSurfaceView.Renderer {
    private var engineStarted : Boolean = false

    override fun onSurfaceCreated(unused: GL10, config: EGLConfig) {
        // Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
    }

    override fun onDrawFrame(unused: GL10) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        CFGLCanvas.modify()
        CFGLCanvas.draw()
    }

    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)

        CFGLWidth = width
        CFGLHeight = height
        CFGLAspect = width.toFloat() / height.toFloat()

        if(!engineStarted)
        {
            CFGLEngine.start()
            engineStarted = true
            OpenGLActivity.running = true

            CFGLPhysicsController.start()
        }
    }

    companion object {
        private const val COORDS_PER_VERTEX = 3
        private const val TEXT_COORD_SIZE = 2

        fun drawObj(shape: Shape) {
            val mProgram = ShapeShader.getProgram()
            var mPositionHandle: Int = 0
            var mColorHandle : Int = 0
            lateinit var vertexCoords : FloatArray

            shape.lock.withLock {
                 vertexCoords = shape.wind()
            }

            val vertexCount: Int = vertexCoords.size / COORDS_PER_VERTEX
            val vertexStride: Int = COORDS_PER_VERTEX * 4 // 4 bytes per vertex

            if(!shape.vertexBufferValid)
            {
                shape.vertexBuffer =
                        // (# of coordinate values * 4 bytes per float)
                    ByteBuffer.allocateDirect(vertexCoords.size * 4).run {
                        order(ByteOrder.nativeOrder())
                        asFloatBuffer().apply {
                            put(vertexCoords)
                            position(0)
                        }
                    }

                shape.vertexBufferValid = true
            }

            // Add program to OpenGL ES environment
            GLES20.glUseProgram(mProgram)

            mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition")
            GLES20.glEnableVertexAttribArray(mPositionHandle)
            GLES20.glVertexAttribPointer(
                mPositionHandle,
                COORDS_PER_VERTEX,
                GLES20.GL_FLOAT,
                false,
                vertexStride,
                shape.vertexBuffer
            )

            // Get handle to shader's vColor member
            mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor")
            // Set color for drawing the shape
            GLES20.glUniform4fv(mColorHandle, 1, shape.color.getColor(), 0)

            // Draw the vertices
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount)

            // Disable the attribute arrays
            GLES20.glDisableVertexAttribArray(mPositionHandle)
        }

        fun texDrawObj(shape: Shape, frame : Frame) {
            val mProgram = TexShapeShader.getProgram()
            var mPositionHandle: Int = 0
            var mTextureCoordinateHandle: Int = 0
            var mTextureUniformHandle : Int = 0
            var mColorHandle : Int = 0

            lateinit var vertexCoords : FloatArray

            shape.lock.withLock {
                vertexCoords = shape.wind()
            }

            val vertexCount: Int = vertexCoords.size / COORDS_PER_VERTEX
            val vertexStride: Int = COORDS_PER_VERTEX * 4 // 4 bytes per vertex

            if(!shape.textureBufferValid)
            {
                val coords : MutableList<Float> = emptyList<Float>().toMutableList()
                coords.add(0f)
                coords.add(1f)
                coords.add(0f)
                coords.add(0f)
                coords.add(1f)
                coords.add(0f)
                coords.add(0f)
                coords.add(1f)
                coords.add(1f)
                coords.add(0f)
                coords.add(1f)
                coords.add(1f)

                val textureCoords = coords.toTypedArray().toFloatArray()

                shape.textureBuffer =
                    ByteBuffer.allocateDirect(textureCoords.size * 4).run {
                        order(ByteOrder.nativeOrder())
                        asFloatBuffer().apply {
                            put(textureCoords)
                            position(0)
                        }
                    }

                shape.textureBufferValid = true
            }

            if(!shape.vertexBufferValid)
            {
                shape.vertexBuffer =
                    // (# of coordinate values * 4 bytes per float)
                    ByteBuffer.allocateDirect(vertexCoords.size * 4).run {
                        order(ByteOrder.nativeOrder())
                        asFloatBuffer().apply {
                            put(vertexCoords)
                            position(0)
                        }
                    }

                shape.vertexBufferValid = true
            }

            // Add program to OpenGL ES environment
            GLES20.glUseProgram(mProgram)

            mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition")
            GLES20.glEnableVertexAttribArray(mPositionHandle)
            GLES20.glVertexAttribPointer(
                mPositionHandle,
                COORDS_PER_VERTEX,
                GLES20.GL_FLOAT,
                false,
                vertexStride,
                shape.vertexBuffer
            )

            mTextureCoordinateHandle = GLES20.glGetAttribLocation(mProgram, "a_TexCoordinate")
            GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle)
            GLES20.glVertexAttribPointer(
                mTextureCoordinateHandle,
                TEXT_COORD_SIZE,
                GLES20.GL_FLOAT,
                false,
                0,
                shape.textureBuffer
            )

            // Get handle to shader's vColor member
            mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor")
            // Set color for drawing the shape
            GLES20.glUniform4fv(mColorHandle, 1, frame.color.getColor(), 0)

            // Set the active texture unit to texture unit 0.
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
            // Bind the texture to this unit.
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, frame.texture)

            // Get handle to shader's u_Texture member
            mTextureUniformHandle = GLES20.glGetUniformLocation(mProgram, "u_Texture")
            // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
            GLES20.glUniform1i(mTextureUniformHandle, 0)

            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
            GLES20.glEnable(GLES20.GL_BLEND)

            // Draw the vertices
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount)

            // Disable the attribute arrays
            GLES20.glDisableVertexAttribArray(mPositionHandle)
            GLES20.glDisableVertexAttribArray(mTextureCoordinateHandle)
        }
    }
}

class ShapeShader {
    companion object {
        private var program : Int = 0
        private var programSet : Boolean = false

        private const val vertexShaderCode =
            "attribute vec4 vPosition;" +
                    "void main() {" +
                    "  gl_Position = vPosition;" +
                    "}"

        private const val fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}"

        fun getProgram() : Int {
            if(!programSet) {
                val vertexShader: Int = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
                val fragmentShader: Int = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

                // create empty OpenGL ES Program
                program = GLES20.glCreateProgram().also {

                    // add the vertex shader to program
                    GLES20.glAttachShader(it, vertexShader)

                    // add the fragment shader to program
                    GLES20.glAttachShader(it, fragmentShader)

                    // creates OpenGL ES program executables
                    GLES20.glLinkProgram(it)
                }

                programSet = true
            }
            return program
        }
    }
}

class TexShapeShader {
    companion object {
        private var program : Int = 0
        private var programSet : Boolean = false

        private const val vertexShaderCode =
            "attribute vec4 vPosition;" +
                    "attribute vec2 a_TexCoordinate;" + // Per-vertex texture coordinate information we will pass in.
                    "varying vec2 v_TexCoordinate;" + // This will be passed into the fragment shader
                    "void main() {" +
                    " v_TexCoordinate = a_TexCoordinate;" + // Pass through the texture coordinate.
                    "  gl_Position = vPosition;" +
                    "}"

        private const val fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "uniform sampler2D u_Texture;" +    // The input texture.
                    "varying vec2 v_TexCoordinate;" + // Interpolated texture coordinate per fragment.
                    "void main() {" +
                    "  gl_FragColor = (vColor * texture2D(u_Texture, v_TexCoordinate));" +
                    "}"

        fun getProgram() : Int {
            if(!programSet) {
                val vertexShader: Int = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
                val fragmentShader: Int = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

                // create empty OpenGL ES Program
                program = GLES20.glCreateProgram().also {

                    // add the vertex shader to program
                    GLES20.glAttachShader(it, vertexShader)

                    // add the fragment shader to program
                    GLES20.glAttachShader(it, fragmentShader)

                    // creates OpenGL ES program executables
                    GLES20.glLinkProgram(it)
                }

                programSet = true
            }
            return program
        }
    }
}

// Utility function to load shaders
fun loadShader(type: Int, shaderCode: String): Int {

    // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
    // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
    return GLES20.glCreateShader(type).also { shader ->

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)

        Log.d("Shader",type.toString() + " " + GLES20.glGetShaderInfoLog(shader))
    }
}

// Utility function to load textures
fun loadTexture(resourceId: Int) : Int{
    val context = CFGLView.context

    val mTextureHandle : IntArray = arrayOf<Int>(0).toIntArray()

    GLES20.glGenTextures(1, mTextureHandle, 0)

    if (mTextureHandle[0] != 0) {
        val options: BitmapFactory.Options = BitmapFactory.Options()
        options.inScaled = false

        // Read in the resource
        val bitmap: Bitmap = BitmapFactory.decodeResource(context.resources, resourceId, options)

        // Bind to the texture in OpenGL
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureHandle[0])

        // Set filtering
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MIN_FILTER,
            GLES20.GL_NEAREST
        )
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MAG_FILTER,
            GLES20.GL_NEAREST
        )

        // Load the bitmap into the bound texture.
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)

        // Recycle the bitmap, since its data has been loaded into OpenGL.
        bitmap.recycle()
    }

    return mTextureHandle[0]
}