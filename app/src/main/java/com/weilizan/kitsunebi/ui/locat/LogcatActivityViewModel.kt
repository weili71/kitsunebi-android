package com.weilizan.kitsunebi.ui.locat

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LogcatActivityViewModel: ViewModel() {
    var data :MutableLiveData<String> =MutableLiveData()
}