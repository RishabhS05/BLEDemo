package com.example.bleexampleapp

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.bleexampleapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var mainActivityBinding : ActivityMainBinding
    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)
        mainActivityBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainActivityBinding.root)
        mainActivityBinding.scanNow.setOnClickListener {
            startBleScan()
        }
        if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private val bluetoothAdapter : BluetoothAdapter by lazy {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    override fun onResume() {
        super.onResume()
        if(!isLocationPermissionGranted)
            askLocationPermission.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
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

        }
    }
}