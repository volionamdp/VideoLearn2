package com.example.videolearn.video

import android.media.MediaCodec
import android.media.MediaFormat
import java.nio.ByteBuffer

interface MExtractor {
    fun play()
    fun pause()
    fun stop()
    fun getCurrentTime():Long
    fun getFormat():MediaFormat?
    fun read(byteBuffer: ByteBuffer):Int
}