package com.weilizan.kitsunebi.ui

import android.R
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.ViewModelProvider
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.weilizan.kitsunebi.databinding.ActivityQrCodeBinding
import com.weilizan.kitsunebi.model.LogcatActivityViewModel
import com.weilizan.kitsunebi.model.QRActivityViewModel
import ijk.player.videoview.util.toast
import kotlin.math.min


class QRActivity : BaseActivity() {

    private lateinit var binding: ActivityQrCodeBinding
    private lateinit var viewModel: QRActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQrCodeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

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

        binding.saveQr.setOnClickListener {
            val bitmap = viewModel.data.value
            MediaStore.Images.Media.insertImage(contentResolver, bitmap, "扫码连接", "description");
            toast(this, "保存二维码成功")
        }
    }

}