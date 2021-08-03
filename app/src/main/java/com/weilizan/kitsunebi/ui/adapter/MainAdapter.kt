package com.weilizan.kitsunebi.ui.adapter

import android.content.ClipData
import android.content.Intent
import android.graphics.Rect
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.weilizan.kitsunebi.R
import com.weilizan.kitsunebi.databinding.ConfigListItemBinding
import com.weilizan.kitsunebi.model.VmessURL
import com.weilizan.kitsunebi.ui.QRActivity
import com.weilizan.kitsunebi.util.base64Encode
import com.weilizan.kitsunebi.util.copyToClipboard
import ijk.player.videoview.util.toast


class MainAdapter(
    private val list: List<VmessURL>
) : RecyclerView.Adapter<MainAdapter.MainViewHolder>() {

    companion object{
        private val TAG =javaClass.simpleName
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        val binding = ConfigListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val holder=MainViewHolder(binding)

        binding.share.setOnClickListener {
            // View当前PopupMenu显示的相对View的位置
            // View当前PopupMenu显示的相对View的位置
            val popupMenu = PopupMenu(parent.context, it)
            // menu布局
            // menu布局
            popupMenu.menuInflater.inflate(R.menu.menu_share, popupMenu.menu)
            // menu的item点击事件
            // menu的item点击事件
            popupMenu.setOnMenuItemClickListener { item ->
                when(item.itemId){
                    R.id.export_url->{
                        toast(parent.context,"share ${holder.bindingAdapterPosition}")
                        val url=generateVmessUrlString(list[holder.bindingAdapterPosition])
                        Log.d(TAG, "onCreateViewHolder: $url")
                        val clip: ClipData = ClipData.newPlainText("", url)
                        copyToClipboard(parent.context,clip)
                        toast(parent.context,"导出到剪贴板成功")
                    }
                    R.id.qr_code->{
                        val url=generateVmessUrlString(list[holder.bindingAdapterPosition])
                        val intent=Intent(parent.context,QRActivity::class.java)
                        intent.putExtra("vmess_url",url)
                        parent.context.startActivity(intent)
                    }
                    R.id.export_json->{
                        toast(parent.context,"此功能未开发")
                    }
                }
                return@setOnMenuItemClickListener false
            }
            popupMenu.show()


        }
        binding.edit.setOnClickListener {
            toast(parent.context,"edit ${holder.bindingAdapterPosition}")
        }
        binding.delete.setOnClickListener {
            toast(parent.context,"delete ${holder.bindingAdapterPosition}")
        }
        binding.root.setOnClickListener {
            toast(parent.context,"root ${holder.bindingAdapterPosition}")
        }
        return holder
    }

    fun generateVmessUrlString(data: VmessURL) ="vmess://"+base64Encode(Gson().toJson(data))

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        val content = list[position]
        holder.binding.anotherName.text=content.ps
        holder.binding.serverAddress.text=content.address
        holder.binding.info.text="200ms"
    }

    override fun getItemCount() = list.size

    class MainViewHolder(val binding : ConfigListItemBinding) : RecyclerView.ViewHolder(binding.root)

    class SpacesItemDecoration(private val space: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            // 仅为第一个项目添加上边距，以避免项目之间出现双倍间距
            if (parent.getChildAdapterPosition(view) == 0) {
                outRect.top = space
            }
            outRect.bottom = space
            outRect.left = space
            outRect.right = space
        }

    }
}
