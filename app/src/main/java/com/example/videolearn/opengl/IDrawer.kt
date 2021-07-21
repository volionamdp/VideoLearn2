package com.example.videolearn.opengl

import android.graphics.SurfaceTexture

interface IDrawer {
    fun draw()
    fun setTextureID(id: Int)
    fun release()
    fun getSurfaceTexture(cb: (st: SurfaceTexture)->Unit) {}
    fun setVideoSize(videoW: Int, videoH: Int)
    fun setWorldSize(worldW: Int, worldH: Int)

}