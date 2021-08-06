package com.weilizan.kitsunebi.ui.qrcode

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.os.*
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.weilizan.kitsunebi.util.toast
import com.esafirm.imagepicker.features.*
import com.esafirm.imagepicker.model.Image
import com.permissionx.guolindev.PermissionX
import com.weilizan.kitsunebi.R
import com.weilizan.kitsunebi.sample.CustomImagePickerComponents
import com.weilizan.kitsunebi.ui.BaseActivity
import me.dm7.barcodescanner.zbar.BarcodeFormat
import me.dm7.barcodescanner.zbar.Result
import me.dm7.barcodescanner.zbar.ZBarScannerView
import me.dm7.barcodescanner.zbar.ZBarScannerView.ResultHandler
import net.sourceforge.zbar.Config
import net.sourceforge.zbar.ImageScanner
import net.sourceforge.zbar.Symbol


class ScannerActivity : BaseActivity(), ResultHandler {

    private lateinit var scanner: ImageScanner
    private lateinit var scannerView: ZBarScannerView
    private lateinit var vibrator: Vibrator
    private val images = arrayListOf<Image>()

    private val imagePickerLauncher = registerImagePicker {
        toast(this,"ok:${it.first().uri}")
    }

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
                PermissionX.init(this)
                    .permissions(Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .explainReasonBeforeRequest()
                    .onExplainRequestReason { scope, deniedList ->
                        scope.showRequestReasonDialog(deniedList, "获取图片需要相册权限", "去授权", "取消")
                    }
                    .onForwardToSettings { scope, deniedList ->
                        scope.showForwardToSettingsDialog(deniedList, "您需要在“设置”中手动授予必要的权限", "去授权", "取消")
                    }
                    .request { allGranted, grantedList, deniedList ->
                        if (allGranted) {
                            imagePickerLauncher.launch(createConfig())
                        } else {
                            toast(this,"相机权限被拒绝")
                        }
                    }
//                startActivity(Intent(this, MainActivity2::class.java))
            }
            else -> super.onOptionsItemSelected(item)
        }
        return false
    }

    private fun createConfig(): ImagePickerConfig {
        val returnAfterCapture = false
        val isSingleMode = true
        val useCustomImageLoader = false
        val folderMode = false
        val includeVideo = false
        val onlyVideo = false
        val isExclude = false

        ImagePickerComponentsHolder.setInternalComponent(
            CustomImagePickerComponents(this, useCustomImageLoader)
        )

        return ImagePickerConfig {

            mode = if (isSingleMode) {
                ImagePickerMode.SINGLE
            } else {
                ImagePickerMode.MULTIPLE // multi mode (default mode)
            }

            language = "in" // Set image picker language
            theme = R.style.ImagePickerTheme

            // set whether pick action or camera action should return immediate result or not. Only works in single mode for image picker
            returnMode = if (returnAfterCapture) ReturnMode.ALL else ReturnMode.NONE

            isFolderMode = folderMode // set folder mode (false by default)
            isIncludeVideo = includeVideo // include video (false by default)
            isOnlyVideo = onlyVideo // include video (false by default)
            arrowColor = Color.RED // set toolbar arrow up color
            folderTitle = "文件夹" // folder selection title
            imageTitle = "选择图片" // image selection title
            doneButtonText = "完成" // done button text
            showDoneButtonAlways = true // Show done button always or not
            limit = 10 // max images can be selected (99 by default)
            isShowCamera = true // show camera or not (true by default)
            savePath = ImagePickerSavePath("Camera") // captured image directory name ("Camera" folder by default)
            savePath = ImagePickerSavePath(Environment.getExternalStorageDirectory().path, isRelative = false) // can be a full path

            if (isExclude) {
                excludedImages = images.toFiles() // don't show anything on this selected images
            } else {
                selectedImages = images  // original selected images, used in multi mode
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == IpCons.RC_IMAGE_PICKER && data != null) {
            images.clear()
            images.addAll(ImagePicker.getImages(data) ?: emptyList())
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun setupScanner() {
        scanner = ImageScanner()
        scanner.setConfig(0, Config.X_DENSITY, 3)
        scanner.setConfig(0, Config.Y_DENSITY, 3)
        scanner.setConfig(Symbol.NONE, Config.ENABLE, 0)
        for (format in getFormats()) {
            scanner.setConfig(format.id, Config.ENABLE, 1)
        }
    }

    private fun getFormats(): Collection<BarcodeFormat> {
        return BarcodeFormat.ALL_FORMATS
    }
}
