package com.example.bleexampleapp

import android.bluetooth.*
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.bleexampleapp.utils.DebugLog
import java.util.*
import java.util.concurrent.ConcurrentHashMap

private const val GATT_MIN_MTU_SIZE = 23
private const val GATT_MAX_MTU_SIZE = 517

object ConnectionManager {

    private fun BluetoothGatt.printGattTable() {
        if(services.isEmpty()) {
            Log.i("printGattTable", "No service and characteristic available, call discoverServices() first?")
            return
        }
        services.forEach { service ->
            val characteristicsTable = service.characteristics.joinToString(
                separator = "\n|--",
                prefix = "|--"
            ) { it.uuid.toString() }
            Log.i("printGattTable", "\nService ${service.uuid}\nCharacteristics:\n$characteristicsTable"
            )
        }
    }

    private val deviceGattMap = ConcurrentHashMap<BluetoothDevice, BluetoothGatt>()
    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt : BluetoothGatt, status : Int, newState : Int) {
            val deviceAddress = gatt.device.address

            if(status == BluetoothGatt.GATT_SUCCESS) {
                if(newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.w("BluetoothGattCallback", "Successfully connected to $deviceAddress")
                    deviceGattMap[gatt.device] = gatt
                    Handler(Looper.getMainLooper()).post {
                        gatt.discoverServices()
                    }
                } else if(newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.w("BluetoothGattCallback", "Successfully disconnected from $deviceAddress")
                    gatt.close()
                }
            } else {
                Log.w("BluetoothGattCallback", "Error $status encountered for $deviceAddress! Disconnecting...")
                gatt.close()
            }
        }

        override fun onServicesDiscovered(gatt : BluetoothGatt, status : Int) {
            with(gatt) {
                Log.w("BluetoothGattCallback", "Discovered ${services.size} services for ${device.address}")
                printGattTable()
                gatt.requestMtu(GATT_MAX_MTU_SIZE)
                readBatteryLevel(gatt)
            }
        }

        override fun onMtuChanged(gatt : BluetoothGatt, mtu : Int, status : Int) {
            DebugLog.w("ATT MTU changed to $mtu, success: ${status == BluetoothGatt.GATT_SUCCESS}")
        }

        override fun onCharacteristicRead(
                gatt : BluetoothGatt,
                characteristic : BluetoothGattCharacteristic,
                status : Int
        ) {
            with(characteristic) {
                when(status) {
                    BluetoothGatt.GATT_SUCCESS -> {
                        DebugLog.i("BluetoothGattCallback Read characteristic $uuid:\n${value.toHexString()}")
                        val readBytes = value.toHexString()
                        val batteryLevel = readBytes.first().toInt()
                        DebugLog.d("BluetoothGattCallback Read characteristic $uuid:\n Battery Level $batteryLevel")
                    }
                    BluetoothGatt.GATT_READ_NOT_PERMITTED -> {
                        DebugLog.e("BluetoothGattCallback Read not permitted for $uuid!")
                    }
                    else -> {
                        DebugLog.e("BluetoothGattCallback Characteristic read failed for $uuid, error: $status")
                    }
                }
            }
        }

        override fun onCharacteristicWrite(
                gatt : BluetoothGatt,
                characteristic : BluetoothGattCharacteristic,
                status : Int
        ) {
            with(characteristic) {
                when(status) {
                    BluetoothGatt.GATT_SUCCESS -> {
                        Log.i("BluetoothGattCallback", "Wrote to characteristic $uuid | value: ${value.toHexString()}")
                    }
                    BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH -> {
                        Log.e("BluetoothGattCallback", "Write exceeded connection ATT MTU!")
                    }
                    BluetoothGatt.GATT_WRITE_NOT_PERMITTED -> {
                        Log.e("BluetoothGattCallback", "Write not permitted for $uuid!")
                    }
                    else -> {
                        Log.e("BluetoothGattCallback", "Characteristic write failed for $uuid, error: $status")
                    }
                }
            }
        }
    }

    private fun readBatteryLevel(gatt : BluetoothGatt) {
        DebugLog.d("readBatteryLevel")
        val batteryServiceUuid = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb")
        val batteryLevelCharUuid = UUID.fromString("00002A19-0000-1000-8000-00805f9b34fb")
        val batteryLevelChar = gatt.getService(batteryServiceUuid)?.getCharacteristic(batteryLevelCharUuid)
        if(batteryLevelChar?.isReadable() == true) {
            gatt.readCharacteristic(batteryLevelChar)
        }
    }

    fun connect(bluetoothDevice : BluetoothDevice, context : Context) {
        bluetoothDevice.connectGatt(context, false, gattCallback)
    }

    fun writeCharacteristic(gatt : BluetoothGatt, characteristic : BluetoothGattCharacteristic, payload : ByteArray) {
        val writeType = when {
            characteristic.isWritable() -> BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            characteristic.isWritableWithoutResponse() -> {
                BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
            }
            else -> error("Characteristic ${characteristic.uuid} cannot be written to")
        }

        gatt.let { gatt ->
            characteristic.writeType = writeType
            characteristic.value = payload
            gatt.writeCharacteristic(characteristic)
        } ?: error("Not connected to a BLE device!")
    }
}