package com.example.bleexampleapp

import java.util.*

object SampleGattAttribute {

    private val attributes : HashMap<String?, String?> = HashMap()
    val BATTERY_CLIENT_UUID = "00002a19-0000-1000-8000-00805f9b34fb"
    fun getValues() {
        //Sample Services
        attributes["0000180d-0000-1000-8000-00805f9b34fb"] = "Heart Rate Service"
        attributes["0000180a-0000-1000-8000-00805f9b34fb"] = "Device Information Service"
        attributes["0000180f-0000-1000-8000-00805f9b34fb"] = "Battery"
        // Sample Characteristics.
        attributes[BATTERY_CLIENT_UUID] = "Battery Level"
        attributes["00002a01-0000-1000-8000-00805f9b34fb"] = "Appearance"
        attributes["00002a50-0000-1000-8000-00805f9b34fb"] = "PnP ID"
        attributes["00002a04-0000-1000-8000-00805f9b34fb"] = "Peripheral Preferred Connection Parameters"
        attributes["00002a08-0000-1000-8000-00805f9b34fb"] = "Date Time"
        attributes["00002a2b-0000-1000-8000-00805f9b34fb"] = "Current Time"
        attributes["00002a25-0000-1000-8000-00805f9b34fb"] = "Serial Number String"
        attributes["00002a00-0000-1000-8000-00805f9b34fb"] = "Device Name"
        attributes["00002a29-0000-1000-8000-00805f9b34fb"] = "Manufacturer Name String"
    }

    fun lookup(uuid : String?, defaultName : String?) : String? {
        val name = attributes[uuid]
        return name ?: defaultName
    }
}