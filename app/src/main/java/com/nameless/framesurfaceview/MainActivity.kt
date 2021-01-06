package com.nameless.framesurfaceview

import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.IOException
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    //将401文件夹拷贝到手机根目录
    //若需要支持assets文件下的或其他资源文件夹下的帧动画，BitmapFactory.decode其他方法
    private val FRAME_NAME = "401"
    val CACHE_DIR1 = (Environment.getExternalStorageDirectory()
            .absolutePath + File.separator + "401")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var frameList = ArrayList<String>()

        val file = File(CACHE_DIR1)

        if (file.exists()) {
            val listFiles = file.listFiles()
            for (listFile in listFiles) {
                if (listFile.isDirectory) {
                    frameList.add(listFile.name)
                }

            }
        }

        start.setOnClickListener {
            frame_surface_view.setList(frameList)
            frame_surface_view.start()
        }
    }
}