package com.example.videolearn.video

import android.media.MediaExtractor
import android.media.MediaFormat
import android.util.Log
import java.nio.ByteBuffer

class MyEx(path: String) : MExtractor {
    private var mediaExtractor: MediaExtractor = MediaExtractor()
    private var videoTrackId = -1
    private var currentTime = 0L
    init {
        mediaExtractor.setDataSource(path)
    }

    override fun play() {
    }

    override fun pause() {
    }

    override fun stop() {
        mediaExtractor.release()
    }

    override fun getCurrentTime():Long {
        return currentTime
    }

    override fun getFormat(): MediaFormat? {
        for (trackId in 0..mediaExtractor.trackCount) {
            val mediaFormat: MediaFormat? = mediaExtractor.getTrackFormat(trackId)
            val mime = mediaFormat?.getString(MediaFormat.KEY_MIME)
            if (mime?.contains("video") == true) {
                videoTrackId = trackId
                mediaExtractor.selectTrack(trackId)
                Log.d("zzz", "getFormat: $mime")
                return mediaFormat
            }
        }
        return null
    }

    override fun read(byteBuffer: ByteBuffer): Int {
        byteBuffer.clear()
        val readSampleCount = mediaExtractor.readSampleData(byteBuffer,0)
        if (readSampleCount < 0) return  -1
        currentTime = mediaExtractor.sampleTime
        mediaExtractor.advance()
        return readSampleCount
    }
}