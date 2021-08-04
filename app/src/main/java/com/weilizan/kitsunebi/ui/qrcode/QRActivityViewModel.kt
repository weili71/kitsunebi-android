package com.weilizan.kitsunebi.ui.qrcode

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class QRActivityViewModel : ViewModel() {
    var data: MutableLiveData<Bitmap> = MutableLiveData()
}
