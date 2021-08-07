package com.weilizan.kitsunebi.ui.qrcode

import android.content.ContentValues
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import androidx.lifecycle.ViewModelProvider
//import com.google.zxing.BarcodeFormat
//import com.journeyapps.barcodescanner.BarcodeEncoder
import com.weilizan.kitsunebi.R
import com.weilizan.kitsunebi.databinding.ActivityQrCodeBinding
import com.weilizan.kitsunebi.ui.BaseActivity
import com.weilizan.kitsunebi.ui.getScreenHeight
import com.weilizan.kitsunebi.ui.getScreenWidth
import com.weilizan.kitsunebi.util.toast
import kotlin.math.min


class QRActivity : BaseActivity() {

    private lateinit var binding: ActivityQrCodeBinding
    private lateinit var viewModel: QRActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQrCodeBinding.inflate(layoutInflater)
        setContentView(binding.root)


        viewModel = ViewModelProvider(this).get(QRActivityViewModel::class.java)
        viewModel.data.observe(this, {
            binding.qrImage.setImageBitmap(it)
        })

        val vmessUrl = intent.getStringExtra("vmess_url")

        if (viewModel.data.value == null) {
            try {
                val width = min(getScreenWidth(this), getScreenHeight(this))

//                val barcodeEncoder = BarcodeEncoder()
//                val bitmap = barcodeEncoder.encodeBitmap(
//                    vmessUrl,
//                    BarcodeFormat.QR_CODE,
//                    width, width
//
//                )
//                viewModel.data.value = bitmap
            } catch (e: Exception) {
                e.printStackTrace()
                toast(this, "生成二维码失败")
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_qr_code, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.save_qr_code -> {
                val bitmap = viewModel.data.value!!
//                MediaStore.Images.Media.insertImage(contentResolver, bitmap, "扫码连接", "description");
                val displayName = "${System.currentTimeMillis()}.jpg"
                val mimeType = "image/jpeg"
                val compressFormat = Bitmap.CompressFormat.JPEG
                addBitmapToAlbum(bitmap, displayName, mimeType, compressFormat)
                toast(this, "保存二维码成功")
            }
            else -> super.onOptionsItemSelected(item)
        }
        return false
    }

    private fun addBitmapToAlbum(bitmap: Bitmap, displayName: String, mimeType: String, compressFormat: Bitmap.CompressFormat) {
        val values = ContentValues()
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
        values.put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM)
        } else {
            values.put(MediaStore.MediaColumns.DATA, "${Environment.getExternalStorageDirectory().path}/${Environment.DIRECTORY_DCIM}/$displayName")
        }
        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        if (uri != null) {
            val outputStream = contentResolver.openOutputStream(uri)
            if (outputStream != null) {
                bitmap.compress(compressFormat, 100, outputStream)
                outputStream.close()
//                Toast.makeText(this, "Add bitmap to album succeeded.", Toast.LENGTH_SHORT).show()
            }
        }
    }

}