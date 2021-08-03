package com.weilizan.kitsunebi.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LogcatActivityViewModel: ViewModel() {
    var data :MutableLiveData<String> =MutableLiveData()
}