package com.weilizan.kitsunebi.ui.proxylog

import com.weilizan.kitsunebi.storage.Preferences
import com.weilizan.kitsunebi.storage.ProxyLog
import com.weilizan.kitsunebi.storage.ProxyLogDatabase
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList

class ProxyLogViewModel(application: Application) : AndroidViewModel(application) {

    private var proxyLogLiveData: LiveData<PagedList<ProxyLog>>

    init {
        lateinit var factory: DataSource.Factory<Int, ProxyLog>

        // TODO using global constant string
        val isHideDns = Preferences.getBool(getApplication(), "is_hide_dns_logs", null)
        factory = if (isHideDns) {
            ProxyLogDatabase.getInstance(getApplication()).proxyLogDao().getAllNonDnsPaged()
        } else {
            ProxyLogDatabase.getInstance(getApplication()).proxyLogDao().getAllPaged()
        }

        val pagedListBuilder: LivePagedListBuilder<Int, ProxyLog> =
            LivePagedListBuilder<Int, ProxyLog>(
                factory,
                30
            )
        proxyLogLiveData = pagedListBuilder.build()
    }

    fun getProxyLogLiveData() = proxyLogLiveData
}