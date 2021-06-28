package com.example.bleexampleapp

import android.Manifest
import android.app.Activity
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.example.bleexampleapp.bledevice.BleOperationsActivity
import com.example.bleexampleapp.databinding.ActivityMainBinding
import com.example.bleexampleapp.utils.DebugLog
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import java.util.*

class MainActivity : AppCompatActivity() {

    val REQUEST_CHECK_SETTING = 1001
    lateinit var mainActivityBinding : ActivityMainBinding
    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)
        mainActivityBinding = ActivityMainBinding.inflate(layoutInflater)
        SampleGattAttribute.getValues()
        setContentView(mainActivityBinding.root)
        mainActivityBinding.scanNow.setOnClickListener {
            if(isScanning) stopBleScan() else startBleScan()
        }
        if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish()
        }
        setupRecyclerView()
    }

    private val bluetoothAdapter : BluetoothAdapter by lazy {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    override fun onResume() {
        super.onResume()
        ConnectionManager.registerListener(connectionEventListener)
        if(!isLocationPermissionGranted)
            askLocationPermission.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        createLocationRequest()
        if(!bluetoothAdapter.isEnabled) {
            promptEnableBluetooth()
        }
    }

    val isLocationPermissionGranted
        get() = hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)

    fun Context.hasPermission(permissionType : String) : Boolean {
        return ContextCompat.checkSelfPermission(this, permissionType) ==
                PackageManager.PERMISSION_GRANTED
    }

    private fun promptEnableBluetooth() {
        if(!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            resultLauncher.launch(enableBtIntent)
        }
    }

    val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if(it.resultCode == Activity.RESULT_OK) {
            askMultiplePermissions.launch(
                arrayOf(
                    android.Manifest.permission.BLUETOOTH,
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                )
            )
            if(!isLocationPermissionGranted)
                askLocationPermission.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
    private val askLocationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
        if(result) {
        } else {
        }
    }
    private val askMultiplePermissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { map ->
        for(entry in map.entries) {
            Toast.makeText(this, "${entry.key} = ${entry.value}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startBleScan() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !isLocationPermissionGranted) {
            askLocationPermission.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            bleScanner.startScan(null, scanSettings, scanCallback)
            isScanning = true
        }
    }

    private val bleScanner by lazy {
        bluetoothAdapter.bluetoothLeScanner
    }
    private val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()
    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType : Int, result : ScanResult) {
            val indexQuery = scanResults.indexOfFirst { it.device.address == result.device.address }
            if(indexQuery != -1) { // A scan result already exists with the same address
                scanResults[indexQuery] = result
                scanResultAdapter.notifyItemChanged(indexQuery)
            } else {
                with(result.device) {
                    DebugLog.i("ScanCallback Found BLE device! Name: ${name ?: "Unnamed"}, address: $address")
                }
                scanResults.add(result)
                scanResultAdapter.notifyItemInserted(scanResults.size - 1)
            }
        }

        override fun onScanFailed(errorCode : Int) {
            DebugLog.e("ScanCallback onScanFailed: code $errorCode")
        }
    }

    private fun stopBleScan() {
        bleScanner.stopScan(scanCallback)
        isScanning = false
    }

    private var isScanning = false
        set(value) {
            field = value
            runOnUiThread { mainActivityBinding.scanNow.text = if(value) "Stop Scan" else "Scan Bluetooth Devices" }
        }

    private fun setupRecyclerView() {
        mainActivityBinding.scanRecyclerView.apply {
            adapter = scanResultAdapter
            layoutManager = LinearLayoutManager(
                this@MainActivity,
                RecyclerView.VERTICAL,
                false
            )
            isNestedScrollingEnabled = false
        }
        val animator = mainActivityBinding.scanRecyclerView.itemAnimator
        if(animator is SimpleItemAnimator) {
            animator.supportsChangeAnimations = false
        }
    }

    fun createLocationRequest() {
        val locationRequest = LocationRequest.create()?.apply {
            interval = 200000
            fastestInterval = 100000
            priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        builder.setAlwaysShow(true)
        builder.setNeedBle(true)
        val client : SettingsClient = LocationServices.getSettingsClient(this)
        val task : Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())
        task.addOnSuccessListener { locationSettingsResponse ->
        }.addOnFailureListener { exception ->
            if(exception is ResolvableApiException) {
                exception.startResolutionForResult(this@MainActivity, REQUEST_CHECK_SETTING)
            }
        }
    }

    override fun onActivityResult(requestCode : Int, resultCode : Int, data : Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQUEST_CHECK_SETTING) {
            when(resultCode) {
                Activity.RESULT_OK ->
                    Toast.makeText(this@MainActivity, "GPS is turned on ", Toast.LENGTH_SHORT).show()
                Activity.RESULT_CANCELED -> Toast.makeText(this@MainActivity, "GPS is required to be turned on!!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private val connectionEventListener by lazy {
        ConnectionEventListener().apply {
            onConnectionSetupComplete = { gatt ->
                Intent(this@MainActivity, BleOperationsActivity::class.java).also {
                    it.putExtra(BluetoothDevice.EXTRA_DEVICE, gatt.device)

                    startActivity(it)
                }
                ConnectionManager.unregisterListener(this)
            }
            onDisconnect = {
                runOnUiThread {
//                    alert {
//                        title = "Disconnected"
//                        message = "Disconnected or unable to connect to device."
//                        positiveButton("OK") {}
//                    }.show()
                }
            }
        }
    }
    private val scanResults = mutableListOf<ScanResult>()
    private val scanResultAdapter : ScanResultAdapter by lazy {
        ScanResultAdapter(scanResults) { result ->
            if(isScanning) {
                stopBleScan()
            }
            with(result.device) {
                Log.w("ScanResultAdapter", "Connecting to $address")
//                Timber.w("Connecting to $address")
                ConnectionManager.connect(this, this@MainActivity)
            }
        }
    }
}