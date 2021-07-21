package com.example.videolearn

import android.graphics.BitmapFactory
import android.opengl.GLSurfaceView
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.Surface
import android.view.SurfaceView
import com.example.videolearn.opengl.*
import com.example.videolearn.video.MyEx
import com.example.videolearn.video.VideoDecoder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    val path = Environment.getExternalStorageDirectory().absolutePath + "/mvtest_2.mp4"
    val path1 = Environment.getExternalStorageDirectory().absolutePath + "/mvtest_1.mp4"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val sfv:GLSurfaceView = findViewById(R.id.sfv)
        initSuface(sfv)
    }
    private fun initSuface(sfv:GLSurfaceView){
        val mutilVideoRender = MutilVideoRender()
        initRender(mutilVideoRender,path1)
        initRender(mutilVideoRender,path)
        sfv.setEGLContextClientVersion(2)
        sfv.setRenderer(mutilVideoRender)
    }
    private fun initRender(mutilVideoRender: MutilVideoRender,path:String){
        val draw = VideoDraw()
        draw.setVideoSize(1920, 1080)
        draw.getSurfaceTexture {
            initPlayer(Surface(it),path)
        }
        mutilVideoRender.addDrawer(draw)

    }

    private fun initPlayer(sfv:Surface,path:String) {
        //val bitmap = BitmapFactory.decodeResource(resources,R.drawable.ph)

        val videoDecoder = VideoDecoder(path, null, sfv)
        val threadPool = Executors.newFixedThreadPool(10)
        threadPool.execute(videoDecoder)

        videoDecoder.notifyDecode()

      //  val test = MyEx(path)

//        CoroutineScope(Default).launch {
//            test.getFormat()
//        }
    }
}