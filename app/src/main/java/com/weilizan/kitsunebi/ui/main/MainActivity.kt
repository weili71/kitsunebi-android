package com.weilizan.kitsunebi.ui.main

import android.Manifest
import android.content.*
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import cn.bertsir.zbar.QrConfig
import cn.bertsir.zbar.QrManager
import cn.bertsir.zbar.utils.SizeUtil
import cn.bertsir.zbar.view.ScanLineView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.setActionButtonEnabled
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import com.google.gson.Gson
import com.permissionx.guolindev.PermissionX
import com.weilizan.kitsunebi.R
import com.weilizan.kitsunebi.common.showAlert
import com.weilizan.kitsunebi.databinding.ActivityMainBinding
import com.weilizan.kitsunebi.model.VmessURL
import com.weilizan.kitsunebi.service.SimpleVpnService
import com.weilizan.kitsunebi.storage.Preferences
import com.weilizan.kitsunebi.ui.SubscribeConfigActivity
import com.weilizan.kitsunebi.ui.custom.CustomActivity
import com.weilizan.kitsunebi.ui.locat.LogcatActivity
import com.weilizan.kitsunebi.ui.proxylog.ProxyLogActivity
import com.weilizan.kitsunebi.ui.qrcode.QRScanActivity
import com.weilizan.kitsunebi.ui.settings.SettingsActivity
import com.weilizan.kitsunebi.util.base64Decode
import com.weilizan.kitsunebi.util.getClipboardContents
import com.weilizan.kitsunebi.util.toast
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    companion object {
        private val TAG = javaClass.simpleName
    }

    private var running = false
    private var starting = false
    private var stopping = false
    private lateinit var binding: ActivityMainBinding
//    private lateinit var configString: String
//    val mNotificationId = 1
//    var mNotificationManager: NotificationManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
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

        fab.setOnClickListener { view ->
//            if (!running && !starting) {
//                starting = true
//                fab.setImageResource(android.R.drawable.ic_media_ff)
//                configString = configView.text.toString()
//                Preferences.putString(applicationContext, Constants.Companion.PREFERENCE_CONFIG_KEY, configString)
//                val intent = VpnService.prepare(this)
//                if (intent != null) {
//                    startActivityForResult(intent, 1)
//                } else {
//                    onActivityResult(1, Activity.RESULT_OK, null);
//                }
//            } else if (running && !stopping) {
//                stopping = true
//                sendBroadcast(Intent("stop_vpn"))
//            }
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
        sendBroadcast(Intent("ping"))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.import_from_clipboard_btn -> {
                val url = getClipboardContents(this)
                if (url.isNullOrEmpty()) {
                    toast(this, "剪贴板为空")
                } else {
                    val data = parseVmessUrl(url.trim())
                    if (data == null) toast(this, "URL解析失败") else {
                        showConfig(data)
                        toast(this, "导入成功")
                    }
                }
            }
            R.id.add_custom_configuration -> {
                MaterialDialog(this).show {
                    title(null, "名称")
                    input(
                        hint = "请输入名称",
                        maxLength = 20,
                        waitForPositiveButton = false
                    ) { dialog, text ->
                        val inputField = dialog.getInputField()
                        val isValid = inputField.length() < 10

                        inputField.error = if (isValid) null else "文件名不能超过10个字符"
                        dialog.setActionButtonEnabled(WhichButton.POSITIVE, isValid)
                    }
                    negativeButton(text = "取消")
                    positiveButton(text = "确定") {
                        val intent = Intent(this@MainActivity, CustomActivity::class.java)
                        intent.putExtra("filename", it.getInputField().text.toString())
                        Log.d(TAG, "onOptionsItemSelected: ${it.getInputField().text.toString()}")
                        startActivity(intent)
                    }
                }
            }
            R.id.scan_code_to_add -> {
                val qrConfig = QrConfig.Builder()
                    .setShowLight(true) //显示手电筒按钮
                    .setShowTitle(true) //显示Title
                    .setShowAlbum(true) //显示从相册选择按钮
                    .setCornerColor(ContextCompat.getColor(this, R.color.colorPrimary)) //设置扫描框颜色
                    .setLineColor(ContextCompat.getColor(this, R.color.colorPrimary)) //设置扫描线颜色
                    .setLineSpeed(QrConfig.LINE_MEDIUM) //设置扫描线速度
                    .setScanType(QrConfig.SCANVIEW_TYPE_QRCODE) //设置扫码类型（二维码，条形码，全部，自定义，默认为二维码）
                    .setPlaySound(true) //是否扫描成功后bi~的声音
                    .setIsOnlyCenter(true) //是否只识别框中内容(默认为全屏识别)
                    .setTitleText("扫码") //设置Tilte文字
                    .setAutoZoom(false) //是否开启自动缩放(实验性功能，不建议使用)
                    .setFingerZoom(true) //是否开始双指缩放
                    .setDoubleEngine(false) //是否开启双引擎识别(仅对识别二维码有效，并且开启后只识别框内功能将失效)
                    .setLooperScan(false) //是否连续扫描二维码
                    .setScanLineStyle(ScanLineView.styleHybrid) //扫描线样式
                    .setAutoLight(false) //自动灯光
                    .setShowVibrator(false) //是否震动提醒
                    .setScannerSize(SizeUtil.dip2px(this, 300), SizeUtil.dip2px(this, 300))
                    .create()

                QrManager.getInstance().init(qrConfig)
                    .registerCallback(this@MainActivity) { result ->
                        Log.e(TAG, "onScanSuccess: ${result.content}")
                        toast(this,result.content)
                        val content = result.content
                        if (!content.isNullOrEmpty()) {
                            val data = parseVmessUrl(content.trim())
                            Log.d(TAG, "onOptionsItemSelected: $data")
                            if (data != null) {
                                showConfig(data)
                                toast(this, "导入成功")
                            } else {
                                toast(this, "URL解析失败")
                            }
                        }else toast(this, "导入失败")
                    }

                PermissionX.init(this)
                    .permissions(Manifest.permission.CAMERA)
                    .onExplainRequestReason { scope, deniedList ->
                        scope.showRequestReasonDialog(deniedList, "扫描二维码需要相机权限", "去授权", "取消")
                    }
                    .onForwardToSettings { scope, deniedList ->
                        scope.showForwardToSettingsDialog(
                            deniedList,
                            "您需要在“设置”中手动授予必要的权限",
                            "去授权",
                            "取消"
                        )
                    }
                    .request { allGranted, grantedList, deniedList ->
                        if (allGranted) {
                            QRScanActivity.start(this, qrConfig)
                        } else {
                            toast(this, "相机权限被拒绝")
                        }
                    }

            }
            R.id.subscribe_config_btn -> {
                val intent = Intent(this, SubscribeConfigActivity::class.java)
                startActivity(intent)
            }
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
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://github.com/weili71/kitsunebi-android")
                )
                startActivity(intent)
            }
            else -> super.onOptionsItemSelected(item)
        }

        return true
    }

    private fun showConfig(data: VmessURL) {
        val list = mutableListOf<VmessURL>()
        for (i in 0..50) {
            data.ps = i.toString()
            list.add(data.copy())
        }

        binding.configList.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            if (this.itemDecorationCount == 0) {
                addItemDecoration(MainAdapter.SpacesItemDecoration(25))
            }
            this.adapter = MainAdapter(list, 0)
        }
    }

    private fun parseVmessUrl(url: String): VmessURL? {
        try {
            if (url.matches(Regex("""^vmess://.+$"""))) {
                val json = base64Decode(url.replace("vmess://", ""))
                Log.d(TAG, "parseVmessUrl: json: $json")
                return Gson().fromJson(json, VmessURL::class.java)
            }
        } catch (e: Exception) {
            return null
        }
        return null
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
                        showAlert(
                            it,
                            "Start VPN service failed: Not configuring DNS right, must has at least 1 dns server and mustn't include \"localhost\""
                        )
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
                    Preferences.putBool(
                        applicationContext,
                        getString(R.string.vpn_is_running),
                        true
                    )
                }
            }
        }
    }

}