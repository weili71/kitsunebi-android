package com.weilizan.kitsunebi.ui.qrcode

import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import androidx.lifecycle.ViewModelProvider
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.weilizan.kitsunebi.R
import com.weilizan.kitsunebi.databinding.ActivityQrCodeBinding
import com.weilizan.kitsunebi.ui.BaseActivity
import com.weilizan.kitsunebi.ui.getScreenHeight
import com.weilizan.kitsunebi.ui.getScreenWidth
import ijk.player.videoview.util.toast
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

                val barcodeEncoder = BarcodeEncoder()
                val bitmap = barcodeEncoder.encodeBitmap(
                    vmessUrl,
                    BarcodeFormat.QR_CODE,
                    width, width

                )
                viewModel.data.value = bitmap
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
                val bitmap = viewModel.data.value
                MediaStore.Images.Media.insertImage(contentResolver, bitmap, "扫码连接", "description");
                toast(this, "保存二维码成功")
            }
            else -> super.onOptionsItemSelected(item)
        }
        return false
    }
}