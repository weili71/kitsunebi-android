package com.weilizan.kitsunebi.ui.proxylog

import com.weilizan.kitsunebi.R
import com.weilizan.kitsunebi.storage.ProxyLogDatabase
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import java.util.*
import kotlin.concurrent.schedule


class ProxyLogActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var viewModel: ProxyLogViewModel
    private lateinit var viewAdapter: ProxyLogAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_proxy_log)

//        viewModel = ViewModelProvider(this,ViewModelProvider.AndroidViewModelFactory(application)).get(ProxyLogViewModel::class.java)
        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        ).get(ProxyLogViewModel::class.java)

        viewManager = LinearLayoutManager(this)
        viewAdapter = ProxyLogAdapter(this)

        recyclerView = findViewById<RecyclerView>(R.id.proxy_log_recycler_view).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }

        viewModel.getProxyLogLiveData().observe(this, {
            if (it != null) viewAdapter.submitList(it)
        })

        findViewById<SwipeRefreshLayout>(R.id.swipeContainer).apply {
            setOnRefreshListener {
                viewAdapter.notifyDataSetChanged()
                this.isRefreshing = false
                Timer().schedule(500) {
                    recyclerView.smoothScrollToPosition(0)
                }
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_proxy_log, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.clear_btn -> {
                Timer().schedule(0) {
                    ProxyLogDatabase.getInstance(getApplication()).proxyLogDao().deleteAll()
                    runOnUiThread {
                        viewAdapter.notifyDataSetChanged()
                    }
                }
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}