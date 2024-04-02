package com.example.rssiwifi

import android.annotation.SuppressLint
import android.net.wifi.ScanResult
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.rssiwifi.databinding.ItemWifiBinding

class RssiAdapter : RecyclerView.Adapter<RssiAdapter.RssiViewHolder>() {

    private var listWifi: ArrayList<WifiModel> = arrayListOf()

    inner class RssiViewHolder(private val binding: ItemWifiBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindData(item: WifiModel) {
            binding.apply {
                tvWifiName.text = item.wifiName
                tvWifiMac.text = item.wifiMac
                tvWifiRssi.text = item.wifiRssi?.let { convertToDbm(it) }
            }
            binding.itemSelected.visibility = if (item.selected) View.VISIBLE else View.GONE
            itemView.setOnClickListener {
                item.selected = !item.selected
                if (item.selected) {
                    binding.itemSelected.visibility = View.VISIBLE
                } else {
                    binding.itemSelected.visibility = View.GONE
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RssiViewHolder {
        return RssiViewHolder(
            ItemWifiBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return listWifi.size
    }

    override fun onBindViewHolder(holder: RssiViewHolder, position: Int) {
        holder.bindData(listWifi[position])
    }

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(listScannedWifi: List<WifiModel>) {
        listWifi.clear()
        if (listScannedWifi.isNotEmpty()) {
            listWifi.addAll(listScannedWifi)
        }
        notifyDataSetChanged()
    }

    private fun convertToDbm(level: Int): String {
        val stringBuilder = StringBuilder()
        stringBuilder.append(level)
        stringBuilder.append(" ")
        stringBuilder.append("dbm")
        return stringBuilder.toString()
    }

    fun getItemSelected(): List<WifiModel> {
        val listWifiModel = arrayListOf<WifiModel>()
        for (wifi in listWifi) {
           if (wifi.selected) {
               listWifiModel.add(wifi)
           }
        }
        return listWifiModel
    }
}