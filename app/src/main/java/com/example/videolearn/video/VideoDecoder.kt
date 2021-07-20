package com.example.videolearn.video

import android.media.MediaCodec
import android.media.MediaFormat
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import java.io.File
import java.nio.ByteBuffer

class VideoDecoder (path: String, sfv: SurfaceView?, surface: Surface?): Runnable{
    private val mSurfaceView = sfv
    private var mSurface = surface
    private val mFilePath: String = path
    private var mBufferInfo = MediaCodec.BufferInfo()
    private var mOutputBuffers: Array<ByteBuffer>? = null
    private var mInputBuffers: Array<ByteBuffer>? = null
    private var mCodec: MediaCodec? = null
    private var mExtractor: MExtractor? = null
    protected var mVideoWidth = 0
    private var mIsEOS = false
    private val mLock = Object()

    protected var mVideoHeight = 0

    private var mDuration: Long = 0
    override fun run() {
        Log.d("zzzz", "run: ")

//        waitDecode()
        if (!init()) return
        while (true){
            Log.d("zzzz", "run: ")
            if (!mIsEOS){
                mIsEOS = pushBufferToDecoder()
            }
            val index = pullBufferFromDecoder()
            if (index >= 0){
                render(mOutputBuffers!![index],mBufferInfo)
                mCodec!!.releaseOutputBuffer(index, true)

            }
            if (mBufferInfo.flags == MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
                break
            }
        }
    }
     fun waitDecode() {
        try {
            synchronized(mLock) {
                mLock.wait()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     *Thông báo cho chuỗi giải mã tiếp tục chạy
     */
     fun notifyDecode() {
        synchronized(mLock) {
            mLock.notifyAll()
        }
    }
    private fun pushBufferToDecoder(): Boolean {
        var inputBufferIndex = mCodec!!.dequeueInputBuffer(2000)
        var isEndOfStream = false
        if (inputBufferIndex >= 0) {
            val inputBuffer = mInputBuffers!![inputBufferIndex]
            val sampleSize = mExtractor!!.read(inputBuffer)

            if (sampleSize < 0) {
                // Nếu dữ liệu đã được tìm nạp, hãy nhấn vào cờ kết thúc dữ liệu: MediaCodec.BUFFER_FLAG_END_OF_STREAM
                mCodec!!.queueInputBuffer(
                    inputBufferIndex, 0, 0,
                    0, MediaCodec.BUFFER_FLAG_END_OF_STREAM
                )
                isEndOfStream = true
            } else {
                mCodec!!.queueInputBuffer(
                    inputBufferIndex, 0,
                    sampleSize, mExtractor!!.getCurrentTime(), 0
                )
            }
        }
        return isEndOfStream
    }
    private fun pullBufferFromDecoder(): Int {
        // Truy vấn xem có dữ liệu được giải mã hay không, khi chỉ mục> = 0, điều đó có nghĩa là dữ liệu hợp lệ và chỉ mục là chỉ mục bộ đệm
        var index = mCodec!!.dequeueOutputBuffer(mBufferInfo, 1000)
        return index
    }
    private fun init():Boolean{
        if (mFilePath.isEmpty() || !File(mFilePath).exists()) {
            return false
        }

        // Khởi tạo trình trích xuất dữ liệu
        mExtractor = initExtractor(mFilePath)
        if (mExtractor == null || mExtractor!!.getFormat() == null) {
            return false
        }
        if (!initParams()) return false
        if (!initCodec()) return false

        return true
    }
    fun initExtractor(path: String): MExtractor {
        return MyEx(path)
    }
    private fun initParams(): Boolean {
        try {
            val format = mExtractor!!.getFormat()!!
            mDuration = format.getLong(MediaFormat.KEY_DURATION) / 1000
        } catch (e: Exception) {
            return false
        }
        return true
    }

    private fun initCodec(): Boolean {
        try {
            val type = mExtractor!!.getFormat()!!.getString(MediaFormat.KEY_MIME)
            mCodec = MediaCodec.createDecoderByType(type!!)
            if (!configCodec(mCodec!!, mExtractor!!.getFormat()!!)) {
                waitDecode()
            }
            mCodec!!.start()

            mInputBuffers = mCodec?.inputBuffers
            mOutputBuffers = mCodec?.outputBuffers
        } catch (e: Exception) {
            return false
        }
        return true
    }
    fun configCodec(codec: MediaCodec, format: MediaFormat): Boolean{
        if (mSurface != null) {
            codec.configure(format, mSurface , null, 0)
            notifyDecode()
        } else {
            mSurfaceView?.holder?.addCallback(object : SurfaceHolder.Callback2 {
                override fun surfaceRedrawNeeded(holder: SurfaceHolder) {
                }

                override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
                }

                override fun surfaceDestroyed(holder: SurfaceHolder) {
                }

                override fun surfaceCreated(holder: SurfaceHolder) {
                    mSurface = holder.surface
                    configCodec(codec, format)
                }
            })

            return false
        }
        return true
    }
    private fun render(
        outputBuffer: ByteBuffer,
        bufferInfo: MediaCodec.BufferInfo
    ){

    }
}