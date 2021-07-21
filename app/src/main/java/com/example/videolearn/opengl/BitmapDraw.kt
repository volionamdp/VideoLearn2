package com.example.videolearn.opengl

import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLUtils
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class BitmapDraw(var bitmap: Bitmap) : IDrawer {
    private var mTextureId: Int = -1
    private var mProgram: Int = -1

    private var mVertexPosHandler: Int = -1
    private var mTexturePosHandler: Int = -1
    private var mTextureHandler: Int = -1

    private lateinit var mVertexBuffer: FloatBuffer
    private lateinit var mTextureBuffer: FloatBuffer

    private val TAG = "BitmapDraw"
    private val mVertexCoors = floatArrayOf(
        -1f, -1f,
        -1f, 1f,
        1f, -1f,
        1f, 1f
    )

    //纹理坐标
    private val mTextureCoors = floatArrayOf(
        0f, 1f,
        0f, 0f,
        1f, 1f,
        1f, 0f
    )

    init {
        initPos()
    }

    private fun initPos() {
        val vb = ByteBuffer.allocateDirect(mVertexCoors.size * 4)
        vb.order(ByteOrder.nativeOrder())
        mVertexBuffer = vb.asFloatBuffer()
        mVertexBuffer.put(mVertexCoors)
        mVertexBuffer.position(0)

        val tb = ByteBuffer.allocateDirect(mVertexCoors.size * 4)
        tb.order(ByteOrder.nativeOrder())
        mTextureBuffer = tb.asFloatBuffer()
        mTextureBuffer.put(mTextureCoors)
        mTextureBuffer.position(0)
    }
    override fun setTextureID(id: Int) {
        mTextureId = 1
    }
    override fun draw() {
        Log.d("zzz", "draw: $mTextureId")
        if (mTextureId != -1) {
            createGLProgram()
            activeTexture()
            bindBitmapToTexture()
            doDraw()
        }
    }

    private fun doDraw() {
        GLES20.glEnableVertexAttribArray(mVertexPosHandler)
        GLES20.glEnableVertexAttribArray(mTexturePosHandler)

        GLES20.glVertexAttribPointer(mVertexPosHandler, 2, GLES20.GL_FLOAT, false, 0, mVertexBuffer)
        GLES20.glVertexAttribPointer(
            mTexturePosHandler,
            2,
            GLES20.GL_FLOAT,
            false,
            0,
            mTextureBuffer
        )

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
    }

    private fun createGLProgram() {
        if (mProgram == -1) {
            val ver = loadShader(getVertexShader(), GLES20.GL_VERTEX_SHADER)
            val frag = loadShader(getFragmentShader(), GLES20.GL_FRAGMENT_SHADER)
            mProgram = GLES20.glCreateProgram()
            GLES20.glAttachShader(mProgram, ver)
            GLES20.glAttachShader(mProgram, frag)
            GLES20.glLinkProgram(mProgram)
            mVertexPosHandler = GLES20.glGetAttribLocation(mProgram, "aPosition")
            mTexturePosHandler = GLES20.glGetAttribLocation(mProgram, "aCoordinate")
            mTextureHandler = GLES20.glGetUniformLocation(mProgram, "uTexture")
        }
        GLES20.glUseProgram(mProgram)
    }

    private fun activeTexture() {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId)
        GLES20.glUniform1i(mTextureHandler, 0)

        //sử lí pixel thiếu làm mờ
        GLES20.glTexParameterf(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MIN_FILTER,
            GLES20.GL_LINEAR.toFloat()
        )
        GLES20.glTexParameterf(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MAG_FILTER,
            GLES20.GL_LINEAR.toFloat()
        )
        //lặp lại
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_S,
            GLES20.GL_CLAMP_TO_EDGE
        )
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_T,
            GLES20.GL_CLAMP_TO_EDGE
        )
    }

    private fun bindBitmapToTexture() {
        if (!bitmap.isRecycled) {
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
        }
    }



    private fun getVertexShader(): String {
        return "attribute vec4 aPosition;" +
                "attribute vec2 aCoordinate;" +
                "varying vec2 vCoordinate;" +
                "void main() {" +
                "  gl_Position = aPosition;" +
                "  vCoordinate = aCoordinate;" +
                "}"
    }

    private fun getFragmentShader(): String {
        return "precision mediump float;" +
                "uniform sampler2D uTexture;" +
                "varying vec2 vCoordinate;" +
                "void main() {" +
                "  vec4 color = texture2D(uTexture, vCoordinate);" +
                "  gl_FragColor = color;" +
                "}"
    }

    private fun loadShader(shaderString: String, type: Int): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, shaderString)
        GLES20.glCompileShader(shader)
        return shader
    }

    override fun release() {
        GLES20.glDisableVertexAttribArray(mVertexPosHandler)
        GLES20.glDisableVertexAttribArray(mTexturePosHandler)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        GLES20.glDeleteTextures(1, intArrayOf(mTextureId), 0)
        GLES20.glDeleteProgram(mProgram)
    }

    override fun setVideoSize(videoW: Int, videoH: Int) {

    }

    override fun setWorldSize(worldW: Int, worldH: Int) {

    }

}