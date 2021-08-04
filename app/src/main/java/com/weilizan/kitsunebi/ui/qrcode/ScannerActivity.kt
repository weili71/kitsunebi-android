package com.weilizan.kitsunebi.ui.qrcode

import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.weilizan.kitsunebi.sample.MainActivity2
import com.weilizan.kitsunebi.R
import com.weilizan.kitsunebi.ui.BaseActivity
import ijk.player.videoview.util.toast
import me.dm7.barcodescanner.zbar.Result
import me.dm7.barcodescanner.zbar.ZBarScannerView
import me.dm7.barcodescanner.zbar.ZBarScannerView.ResultHandler


class ScannerActivity : BaseActivity(), ResultHandler {

    private lateinit var scannerView: ZBarScannerView
    private lateinit var vibrator: Vibrator

    public override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        scannerView = ZBarScannerView(this) // 以编程方式初始化扫描仪视图
        setContentView(scannerView) // 将扫描仪视图设置为内容视图
        scannerView.setAutoFocus(true)
        vibrator = if (Build.VERSION.SDK_INT >= 31) {
            this.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as Vibrator
        } else {
            this.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    public override fun onResume() {
        super.onResume()
        scannerView.setResultHandler(this) // 将我们自己注册为扫描结果的处理程序。
        scannerView.startCamera() // 在恢复时启动相机
    }

    public override fun onPause() {
        super.onPause()
        scannerView.stopCamera() // 暂停时停止相机
    }

    override fun handleResult(rawResult: Result) {


        if (vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(
                        100,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            } else {
                vibrator.vibrate(100)
            }
        } else {
            val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val r = RingtoneManager.getRingtone(applicationContext, notification)
            r.play()
        }

        Log.v(TAG, rawResult.contents) // 打印扫描结果
        toast(this, rawResult.contents)
        Log.v(TAG, rawResult.barcodeFormat.name) // 打印扫描格式（qrcode、pdf417 等）
        // 如果您想继续扫描，请在下面调用此方法：
        scannerView.resumeCameraPreview(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_scanner, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.open_flash -> {
                scannerView.flash = !scannerView.flash
            }
            R.id.open_photo_album -> {
                startActivity(Intent(this, MainActivity2::class.java))
            }
            else -> super.onOptionsItemSelected(item)
        }
        return false
    }

}
