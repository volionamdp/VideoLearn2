package com.example.videolearn

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.SurfaceView
import com.example.videolearn.video.MyEx
import com.example.videolearn.video.VideoDecoder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    val path = Environment.getExternalStorageDirectory().absolutePath + "/mvtest_2.mp4"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val sfv:SurfaceView = findViewById(R.id.sfv)
        initPlayer(sfv)
    }
    private fun initPlayer(sfv:SurfaceView) {
        val videoDecoder = VideoDecoder(path, sfv, null)
        val threadPool = Executors.newFixedThreadPool(10)
        threadPool.execute(videoDecoder)

        videoDecoder.notifyDecode()
//        val test = MyEx(path)

//        CoroutineScope(Default).launch {
//            test.getFormat()
//        }
    }
}