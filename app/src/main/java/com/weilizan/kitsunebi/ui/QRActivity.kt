package com.weilizan.kitsunebi.ui

import android.R
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.weilizan.kitsunebi.databinding.ActivityQrCodeBinding
import ijk.player.videoview.util.toast
import kotlin.math.min


class QRActivity : BaseActivity() {

    private lateinit var binding: ActivityQrCodeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQrCodeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val vmessUrl = intent.getStringExtra("vmess_url")

        try {
            val width = min(getScreenWidth(this), getScreenHeight(this))

            val barcodeEncoder = BarcodeEncoder()
            val bitmap = barcodeEncoder.encodeBitmap(
                vmessUrl,
                BarcodeFormat.QR_CODE,
                width, width

            )
            binding.qrImage.setImageBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
            toast(this, "生成二维码失败")
        }

        binding.saveQr.setOnClickListener {
            val bitmap = binding.qrImage.drawable.toBitmap()
            MediaStore.Images.Media.insertImage(contentResolver, bitmap, "扫码连接", "description");
            toast(this, "保存二维码成功")
        }
    }

}