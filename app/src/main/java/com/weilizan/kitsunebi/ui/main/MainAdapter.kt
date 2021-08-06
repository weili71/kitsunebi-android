package com.weilizan.kitsunebi.ui.main

import android.content.ClipData
import android.content.Intent
import android.graphics.Rect
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.weilizan.kitsunebi.R
import com.weilizan.kitsunebi.databinding.ConfigListItemBinding
import com.weilizan.kitsunebi.model.VmessURL
import com.weilizan.kitsunebi.ui.qrcode.QRActivity
import com.weilizan.kitsunebi.util.base64Encode
import com.weilizan.kitsunebi.util.copyToClipboard
import com.weilizan.kitsunebi.util.toast


class MainAdapter(
    private val list: List<VmessURL>,
    private var selectedPosition: Int
) : RecyclerView.Adapter<MainAdapter.MainViewHolder>() {

    companion object {
        private val TAG = javaClass.simpleName
    }

    fun getSelectedPosition() = selectedPosition

    fun setSelectedPosition(position: Int) {
        selectedPosition = position
        notifyDataSetChanged()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        val binding =
            ConfigListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val holder = MainViewHolder(
            binding,
            binding.root.cardBackgroundColor.defaultColor,
            parent.context.getColor(R.color.colorPrimary)
        )

        binding.root.setOnClickListener {
            val position = holder.bindingAdapterPosition
            if (getSelectedPosition() != position) {
                setSelectedPosition(position)
            }
        }

        binding.share.setOnClickListener {
            val popupMenu = PopupMenu(parent.context, it)
            popupMenu.menuInflater.inflate(R.menu.menu_share, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.export_url -> {
                        toast(parent.context, "share ${holder.bindingAdapterPosition}")
                        val url = generateVmessUrlString(list[holder.bindingAdapterPosition])
                        Log.d(TAG, "onCreateViewHolder: $url")
                        val clip: ClipData = ClipData.newPlainText("", url)
                        copyToClipboard(parent.context, clip)
                        toast(parent.context, "导出到剪贴板成功")
                    }
                    R.id.qr_code -> {
                        val url = generateVmessUrlString(list[holder.bindingAdapterPosition])
                        val intent = Intent(parent.context, QRActivity::class.java)
                        intent.putExtra("vmess_url", url)
                        parent.context.startActivity(intent)
                    }
                    R.id.export_json -> {
                        toast(parent.context, "此功能未开发")
                    }
                }
                return@setOnMenuItemClickListener false
            }
            popupMenu.show()


        }
        binding.edit.setOnClickListener {
            toast(parent.context, "edit ${holder.bindingAdapterPosition}")
        }
        binding.delete.setOnClickListener {
            toast(parent.context, "delete ${holder.bindingAdapterPosition}")
        }
        return holder
    }

    private fun generateVmessUrlString(data: VmessURL) =
        "vmess://" + base64Encode(Gson().toJson(data))

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        val content = list[position]
        holder.binding.anotherName.text = content.ps
        holder.binding.serverAddress.text = content.address
        holder.binding.info.text = "200ms"

        if (position == selectedPosition) {
            holder.binding.root.apply {
                setCardBackgroundColor(holder.cardSelectedColor)
            }
            Log.d(TAG, "onBindViewHolder: 设置颜色成功")
        } else {
            holder.binding.root.apply {
                setCardBackgroundColor(holder.cardDefaultColor)
            }
        }

        Log.d(TAG, "onBindViewHolder: onBindViewHolder")
    }

    override fun getItemCount() = list.size

    class MainViewHolder(
        val binding: ConfigListItemBinding,
        val cardDefaultColor: Int,
        val cardSelectedColor: Int
    ) : RecyclerView.ViewHolder(
        binding.root
    )

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
