package com.example.videolearn.opengl

import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.Matrix
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class VideoDraw() : IDrawer {
    private var mTextureId: Int = -1
    private var mProgram: Int = -1

    private var mVertexPosHandler: Int = -1
    private var mTexturePosHandler: Int = -1
    private var mTextureHandler: Int = -1
    private var mVertexMatrixHandler: Int = -1


    private var mWorldWidth: Int = -1
    private var mWorldHeight: Int = -1
    private var mVideoWidth: Int = -1
    private var mVideoHeight: Int = -1

    private lateinit var mVertexBuffer: FloatBuffer
    private lateinit var mTextureBuffer: FloatBuffer
    private var mMatrix: FloatArray? = null

    private var mSurfaceTexture: SurfaceTexture? = null

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
    private var mSftCb: ((SurfaceTexture) -> Unit) = {}

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
        mSurfaceTexture = SurfaceTexture(id)
        mSftCb(mSurfaceTexture!!)

    }

    override fun draw() {
        Log.d("zzz", "draw: $mTextureId")
        if (mTextureId != -1) {
            createGLProgram()
            initDefMatrix()
            activeTexture()
            updateTexture()
            doDraw()
        }
    }

    private fun updateTexture() {
        mSurfaceTexture?.updateTexImage()
    }

    private fun doDraw() {
        GLES20.glEnableVertexAttribArray(mVertexPosHandler)
        GLES20.glEnableVertexAttribArray(mTexturePosHandler)

        GLES20.glUniformMatrix4fv(mVertexMatrixHandler, 1, false, mMatrix, 0)
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
            mVertexMatrixHandler = GLES20.glGetUniformLocation(mProgram, "uMatrix")
            mVertexPosHandler = GLES20.glGetAttribLocation(mProgram, "aPosition")
            mTexturePosHandler = GLES20.glGetAttribLocation(mProgram, "aCoordinate")
            mTextureHandler = GLES20.glGetUniformLocation(mProgram, "uTexture")

        }
        GLES20.glUseProgram(mProgram)
    }

    private fun activeTexture() {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_BINDING_EXTERNAL_OES, mTextureId)
        GLES20.glUniform1i(mTextureHandler, 0)

        //sử lí pixel thiếu làm mờ
        GLES20.glTexParameterf(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES20.GL_TEXTURE_MIN_FILTER,
            GLES20.GL_LINEAR.toFloat()
        )
        GLES20.glTexParameterf(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES20.GL_TEXTURE_MAG_FILTER,
            GLES20.GL_LINEAR.toFloat()
        )
        //lặp lại

        GLES20.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES20.GL_TEXTURE_WRAP_S,
            GLES20.GL_CLAMP_TO_EDGE
        )
        GLES20.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES20.GL_TEXTURE_WRAP_T,
            GLES20.GL_CLAMP_TO_EDGE
        )
    }

    private fun bindBitmapToTexture() {

    }


    private fun getVertexShader(): String {
        return "attribute vec4 aPosition;" +
                //【新增4: 矩阵变量】
                "uniform mat4 uMatrix;" +
                "attribute vec2 aCoordinate;" +
                "varying vec2 vCoordinate;" +
                "void main() {" +
                //【新增5: 坐标变换】
                "    gl_Position = aPosition*uMatrix;" +
                "    vCoordinate = aCoordinate;" +
                "}"
    }

    private fun getFragmentShader(): String {
        //一定要加换行"\n"，否则会和下一行的precision混在一起，导致编译出错
        return "#extension GL_OES_EGL_image_external : require\n" +
                "precision mediump float;" +
                "varying vec2 vCoordinate;" +
                "uniform samplerExternalOES uTexture;" +
                "void main() {" +
                "vec4 color = texture2D(uTexture, vCoordinate);"+
                "  gl_FragColor=vec4(color.r,color.g,color.b,0.5);" +
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

    override fun getSurfaceTexture(cb: (st: SurfaceTexture) -> Unit) {
        mSftCb = cb
    }

    override fun setVideoSize(videoW: Int, videoH: Int) {
        mVideoWidth = videoW
        mVideoHeight = videoH
    }

    override fun setWorldSize(worldW: Int, worldH: Int) {
        mWorldWidth = worldW
        mWorldHeight = worldH
    }
    private fun initDefMatrix() {
        Log.d(TAG, "initDefMatrix: $mWorldHeight")
        if (mMatrix != null) return
        if (mVideoWidth != -1 && mVideoHeight != -1 &&
            mWorldWidth != -1 && mWorldHeight != -1) {
            mMatrix = FloatArray(16)
            var prjMatrix = FloatArray(16)
            val originRatio = mVideoWidth / mVideoHeight.toFloat()
            val worldRatio = mWorldWidth / mWorldHeight.toFloat()
            if (mWorldWidth > mWorldHeight) {
                if (originRatio > worldRatio) {
                    val actualRatio = originRatio / worldRatio
                    Matrix.orthoM(
                        prjMatrix, 0,
                        -1f, 1f,
                        -actualRatio, actualRatio,
                        -1f, 3f
                    )
                } else {// 原始比例小于窗口比例，缩放高度度会导致高度超出，因此，高度以窗口为准，缩放宽度
                    val actualRatio = worldRatio / originRatio
                    Matrix.orthoM(
                        prjMatrix, 0,
                        -actualRatio, actualRatio,
                        -1f, 1f,
                        -1f, 3f
                    )
                }
            } else {
                if (originRatio > worldRatio) {
                    val actualRatio = originRatio / worldRatio
                    Matrix.orthoM(
                        prjMatrix, 0,
                        -1f, 1f,
                        -actualRatio, actualRatio,
                        -1f, 3f
                    )
                } else {// 原始比例小于窗口比例，缩放高度会导致高度超出，因此，高度以窗口为准，缩放宽度
                    val actualRatio = worldRatio / originRatio
                    Matrix.orthoM(
                        prjMatrix, 0,
                        -actualRatio, actualRatio,
                        -1f, 1f,
                        -1f, 3f
                    )
                }
            }
            val viewMatrix = FloatArray(16)
            Matrix.setLookAtM(
                viewMatrix, 0,
                0f, 0f, 5.0f,
                0f, 0f, 0f,
                0f, 1.0f, 0f
            )
            //计算变换矩阵
            Matrix.multiplyMM(mMatrix, 0, prjMatrix, 0, viewMatrix, 0)
        }
    }

}