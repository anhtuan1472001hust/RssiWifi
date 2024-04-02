package com.example.rssiwifi

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rssiwifi.databinding.ActivityMainBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var mainAdapter: RssiAdapter
    private lateinit var wifiManager: WifiManager
    private lateinit var database: FirebaseDatabase
    private lateinit var locationsRef: DatabaseReference

    companion object {
        const val REQUEST_CODE_PERMISSION_LOCATION = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requestPermission()
        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        registerBroadcastReceiver()
        setMainAdapter()
        addListSpinner()
        database = FirebaseDatabase.getInstance()
        locationsRef = database.getReference("locations")
        setButtonClick()
    }

    private fun setButtonClick() {
        binding.buttonScan.setOnClickListener {
            scanWifi()
        }
        binding.buttonSave.setOnClickListener {
            val latitude = binding.edtLatitude.text.toString()
            val longitude = binding.edtLongitude.text.toString()
            uploadLocationData(
                WifiScan(
                    mainAdapter.getItemSelected(),
                    longitude,
                    latitude
                )
            )
        }
    }

    private fun setMainAdapter() {
        val linearLayoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        mainAdapter = RssiAdapter()
        binding.rcvWifi.adapter = mainAdapter
        binding.rcvWifi.layoutManager = linearLayoutManager
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_CODE_PERMISSION_LOCATION
        )
    }

    private fun registerBroadcastReceiver() {
        val wifiScanReceiver = object : BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                val success = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
                } else {
                    false
                }
                if (success) {
                    Log.e("Bello","receive update scan result")
                    scanResult()
                }
            }

        }

        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        applicationContext.registerReceiver(wifiScanReceiver, intentFilter)
    }

    private fun scanWifi() {
        wifiManager.startScan()
        scanResult()
    }

    private fun scanResult() {
        val results = if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            null
        } else {
            wifiManager.scanResults
        }
        val filterResult = results?.filter { !it.SSID.isNullOrEmpty() }
        val listWifiModel = arrayListOf<WifiModel>()
        for (result in filterResult!!) {
            listWifiModel.add(WifiModel(wifiName = result.SSID, wifiMac = result.BSSID, wifiRssi = result.level))
        }
        mainAdapter.submitList(listWifiModel)
    }

    private fun addListSpinner() {
        val directions = arrayOf<String?>("Đông", "Tây", "Nam", "Bắc")
        val arrayAdapter: ArrayAdapter<*> = ArrayAdapter<Any?> (
            this,
            android.R.layout.simple_spinner_item,
            directions
        )
        binding.spinnerDirection.adapter = arrayAdapter
//        binding.spinnerDirection.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
//                TODO("Not yet implemented")
//            }
//
//            override fun onNothingSelected(p0: AdapterView<*>?) {
//                TODO("Not yet implemented")
//            }
//
//        }
    }

    private fun uploadLocationData(wifiScan: WifiScan) {
        val listRssi = mutableListOf<Int>()
        for (wifi in wifiScan.listWifi) {
            wifi.wifiRssi?.let { listRssi.add(it) }
        }
        locationsRef.child("${wifiScan.latitude}:${wifiScan.longitude}").setValue(listRssi)
            .addOnSuccessListener {
                // Xử lý khi ghi dữ liệu thành công
                Toast.makeText(this, "Dữ liệu đã được lưu trữ thành công", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                // Xử lý khi ghi dữ liệu thất bại
                Toast.makeText(this, "Đã xảy ra lỗi khi lưu dữ liệu", Toast.LENGTH_SHORT).show()
            }
    }
}