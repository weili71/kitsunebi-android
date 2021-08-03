package com.weilizan.kitsunebi.model

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class QRActivityViewModel : ViewModel() {
    var data: MutableLiveData<Bitmap> = MutableLiveData()
}
