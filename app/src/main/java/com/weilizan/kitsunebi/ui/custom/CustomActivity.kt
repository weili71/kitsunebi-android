package com.weilizan.kitsunebi.ui.custom

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.weilizan.kitsunebi.R
import com.weilizan.kitsunebi.common.Constants
import com.weilizan.kitsunebi.databinding.ActivityCustomBinding
import com.weilizan.kitsunebi.storage.Preferences
import com.weilizan.kitsunebi.ui.BaseActivity
import com.weilizan.kitsunebi.util.formatJsonString
import ijk.player.videoview.util.toast
import kotlinx.android.synthetic.main.config_list_item.*
import org.json.JSONException
import java.util.regex.Matcher
import java.util.regex.Pattern


class CustomActivity : BaseActivity() {

    private lateinit var binding: ActivityCustomBinding
    private lateinit var viewModel: CustomActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val filename=intent.getStringExtra("filename")
        Log.d(TAG, "onCreate: $filename")
        title = filename
        viewModel = ViewModelProvider(this).get(CustomActivityViewModel::class.java)
        viewModel.config.observe(this, {
        })

        val config=Preferences.getString(applicationContext, Constants.PREFERENCE_CONFIG_KEY, "")
        binding.configView.setText(config)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_custom, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.format -> {
                try {
                    binding.configView.apply {
                        val position = selectionStart
                        setText(formatJsonString(binding.configView.text.toString()))
                        setSelection(position)
                    }
                    toast(this, "格式化成功")
                } catch (e: JSONException) {
                    toast(this, "无效的配置")
                }
            }
            R.id.save -> {
                try {
                    val config = binding.configView.text.toString()
                    val prettyText = formatJsonString(config)
                    save(prettyText)
                    toast(this, "保存成功")
                } catch (e: JSONException) {
                    toast(this, "无效的配置")
                } catch (e: Exception) {
                    toast(this, "保存失败")
                }
            }
            R.id.generate_template -> {
                val configString = Preferences.getString(
                    applicationContext,
                    Constants.PREFERENCE_CONFIG_KEY,
                    Constants.DEFAULT_CONFIG
                )
                configString?.let {
                    formatJsonString(it).let {
                        binding.configView.setText(it, TextView.BufferType.EDITABLE)
                    }
                }
            }
            else -> super.onOptionsItemSelected(item)
        }
        return false
    }

    private fun save(config:String){
        Preferences.putString(
            applicationContext,
            Constants.PREFERENCE_CONFIG_KEY,
            config
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        save(binding.configView.text.toString())
    }
}