package com.weilizan.kitsunebi.ui

import com.weilizan.kitsunebi.R
import com.weilizan.kitsunebi.common.Constants
import com.weilizan.kitsunebi.common.showAlert
import com.weilizan.kitsunebi.databinding.ActivityMainBinding
import com.weilizan.kitsunebi.model.VmessURL
import com.weilizan.kitsunebi.service.SimpleVpnService
import com.weilizan.kitsunebi.storage.Preferences
import com.weilizan.kitsunebi.ui.adapter.MainAdapter
import com.weilizan.kitsunebi.ui.proxylog.ProxyLogActivity
import com.weilizan.kitsunebi.ui.settings.SettingsActivity
import com.weilizan.kitsunebi.util.base64Decode
import com.weilizan.kitsunebi.util.getClipboardContents
import android.app.Activity
import android.content.*
import android.net.Uri
import android.net.VpnService
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import ijk.player.videoview.util.toast
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : AppCompatActivity() {

    companion object{
       private val TAG =javaClass.simpleName
    }

    var running = false
    private var starting = false
    private var stopping = false
    private lateinit var configString: String

//    val mNotificationId = 1
    //    var mNotificationManager: NotificationManager? = null

    val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                "vpn_stopped" -> {
                    running = false
                    stopping = false
                    fab.setImageResource(android.R.drawable.ic_media_play)
//                    mNotificationManager?.cancel(mNotificationId)
                }
                "vpn_started" -> {
                    running = true
                    starting = false
                    fab.setImageResource(android.R.drawable.ic_media_pause)
//                    startNotification()
                }
                "vpn_start_err" -> {
                    running = false
                    starting = false
                    fab.setImageResource(android.R.drawable.ic_media_play)
                    context?.let {
                        showAlert(it, "Start VPN service failed")
                    }
                }
                "vpn_start_err_dns" -> {
                    running = false
                    starting = false
                    fab.setImageResource(android.R.drawable.ic_media_play)
                    context?.let {
                        showAlert(it, "Start VPN service failed: Not configuring DNS right, must has at least 1 dns server and mustn't include \"localhost\"")
                    }
                }
                "vpn_start_err_config" -> {
                    running = false
                    starting = false
                    fab.setImageResource(android.R.drawable.ic_media_play)
                    context?.let {
                        showAlert(it, "Start VPN service failed: Invalid V2Ray config.")
                    }
                }
                "pong" -> {
                    fab.setImageResource(android.R.drawable.ic_media_pause)
                    running = true
                    Preferences.putBool(applicationContext, getString(R.string.vpn_is_running), true)
                }
            }
        }
    }

    private fun updateUI() {

    }

    private lateinit var binding:ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

//        mNotificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        registerReceiver(broadcastReceiver, IntentFilter("vpn_stopped"))
        registerReceiver(broadcastReceiver, IntentFilter("vpn_started"))

        // TODO make a list
        registerReceiver(broadcastReceiver, IntentFilter("vpn_start_err"))
        registerReceiver(broadcastReceiver, IntentFilter("vpn_start_err_dns"))
        registerReceiver(broadcastReceiver, IntentFilter("vpn_start_err_config"))

        registerReceiver(broadcastReceiver, IntentFilter("pong"))

        sendBroadcast(Intent("ping"))

        updateUI()

        fab.setOnClickListener { view ->
            if (!running && !starting) {
                starting = true
                fab.setImageResource(android.R.drawable.ic_media_ff)
                configString = configView.text.toString()
                Preferences.putString(applicationContext, Constants.Companion.PREFERENCE_CONFIG_KEY, configString)
                val intent = VpnService.prepare(this)
                if (intent != null) {
                    startActivityForResult(intent, 1)
                } else {
                    onActivityResult(1, Activity.RESULT_OK, null);
                }
            } else if (running && !stopping) {
                stopping = true
                sendBroadcast(Intent("stop_vpn"))
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            val intent = Intent(this, SimpleVpnService::class.java)
            startService(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        updateUI()
        sendBroadcast(Intent("ping"))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.import_from_clipboard_btn->{
                try {
                    var url=getClipboardContents(this)
                    if (url.isNullOrEmpty()) {
                        toast(this, "导入失败：剪贴板为空")
                        return true
                    }

                    url=url.trim()

                    if (url.matches(Regex("""^vmess://.+$"""))){
                        val json = base64Decode(url.replace("vmess://",""))

//                        binding.contentMain.configView.setText(json)

                        val data:VmessURL=Gson().fromJson(json,VmessURL::class.java)

                        Log.d(TAG, "onOptionsItemSelected: $json")
                        toast(this,"导入成功")

                        val adapter=MainAdapter(listOf(data,data,data))

                        binding.configList.apply {
                            layoutManager=LinearLayoutManager(this@MainActivity)
                            if (this.itemDecorationCount==0) {
                                addItemDecoration(MainAdapter.SpacesItemDecoration(25))
                            }
                            this.adapter=adapter
                        }
                    }else{
                        toast(this,"导入失败：URL错误")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    toast(this,"导入失败：${e.message}")
                }
            }
            R.id.scan_code_to_add->{

            }
            R.id.subscribe_config_btn -> {
                val intent = Intent(this, SubscribeConfigActivity::class.java)
                startActivity(intent)
            }
//            R.id.format_btn -> {
//                val prettyText = formatJsonString(configView.text.toString())
//                prettyText?.let {
//                    configView.setText(it, TextView.BufferType.EDITABLE)
//                }
//            }
//            R.id.save_btn -> {
//                val config = configView.text.toString()
//                val prettyText = formatJsonString(config)
//                if (prettyText == null) {
//                    showAlert(this, "Invalid JSON")
//                }else {
//                    Preferences.putString(
//                        applicationContext,
//                        Constants.PREFERENCE_CONFIG_KEY,
//                        prettyText
//                    )
//                }
//            }
            R.id.log_btn -> {
                val intent = Intent(this, ProxyLogActivity::class.java)
                startActivity(intent)
            }
            R.id.logcat_btn -> {
                val intent = Intent(this, LogcatActivity::class.java)
                startActivity(intent)
            }
            R.id.settings_btn -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
            }
            R.id.help_btn -> {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/eycorsican/kitsunebi-android"))
                startActivity(intent)
            }
            else -> super.onOptionsItemSelected(item)
        }

        return true
    }
    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(broadcastReceiver)
    }

//    private fun startNotification() {
//        // Build Notification , setOngoing keeps the notification always in status bar
//        val mBuilder = NotificationCompat.Builder(this, getString(R.string.notification_channel_id))
//                .setContentTitle("Kitsunebi")
//                .setContentText("Touch to open the app")
//                .setSmallIcon(R.drawable.notification_icon_background)
//                .setWhen(System.currentTimeMillis())
//                .setOngoing(true)
//
//        // Create pending intent, mention the Activity which needs to be
//        //triggered when user clicks on notification(StopScript.class in this case)
//        val contentIntent = PendingIntent.getActivity(this, 0,
//                Intent(this, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT)
//
//        mBuilder.setContentIntent(contentIntent)
//
//        // Builds the notification and issues it.
//        mNotificationManager?.notify(mNotificationId, mBuilder.build())
//    }
}