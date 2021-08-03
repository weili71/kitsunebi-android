package com.weilizan.kitsunebi.ui

import com.weilizan.kitsunebi.R
import com.weilizan.kitsunebi.databinding.ActivityLogcatBinding
import com.weilizan.kitsunebi.model.LogcatActivityViewModel
import com.weilizan.kitsunebi.util.copyToClipboard
import android.content.ClipData
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import ijk.player.videoview.util.toast
import kotlinx.android.synthetic.main.activity_logcat.*
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.*
import kotlin.concurrent.schedule


class LogcatActivity : BaseActivity() {

    private lateinit var bgThread: Thread
    private lateinit var logBuilder: StringBuilder
    private lateinit var bgTimer: Timer
    private lateinit var binding: ActivityLogcatBinding
    private lateinit var viewModel: LogcatActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityLogcatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel=ViewModelProvider(this).get(LogcatActivityViewModel::class.java)
        viewModel.data.observe(this,{
            binding.logcatText.text = it
        })

        logcatScroll.isSmoothScrollingEnabled = true

        bgThread = object : Thread() {
            override fun run() {
                try {
                    logBuilder = StringBuilder()
                    bgTimer = Timer()
                    val process = Runtime.getRuntime().exec("logcat")
                    val bufferedReader = BufferedReader(InputStreamReader(process.inputStream))
                    fun readAndDisplay() {
                        while (bufferedReader.ready()) {
                            val line = bufferedReader.readLine()
                            logBuilder.append(line + "\n")
                            if (logBuilder.length>=10000){
                                break
                            }
                        }
                        viewModel.data.postValue(logBuilder.toString())
                    }
                    bgTimer.schedule(1000) {
                        readAndDisplay()
                    }
//                    bgTimer.scheduleAtFixedRate(0, 5000) {
//                        readAndDisplay()
//                    }
                } catch (e: IOException) {
                    println(e)
                }
            }
        }
        bgThread.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        bgTimer.cancel()
        bgThread.interrupt()
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_logcat, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
         when (item.itemId) {
            R.id.copy_btn -> {
                val clip: ClipData = ClipData.newPlainText("logcat text", logBuilder.toString())
                copyToClipboard(this,clip)
                toast(this,"复制成功")
            }
            else -> super.onOptionsItemSelected(item)
        }
        return true
    }
}