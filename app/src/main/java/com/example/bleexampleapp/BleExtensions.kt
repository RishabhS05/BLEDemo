package com.example.bleexampleapp

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import com.example.bleexampleapp.utils.DebugLog
import java.util.*

const val CCC_DESCRIPTOR_UUID = "00002902-0000-1000-8000-00805F9B34FB"
fun BluetoothGattCharacteristic.isReadable() : Boolean =
        containsProperty(BluetoothGattCharacteristic.PROPERTY_READ)

fun BluetoothGattCharacteristic.isWritable() : Boolean =
        containsProperty(BluetoothGattCharacteristic.PROPERTY_WRITE)

fun BluetoothGattCharacteristic.isWritableWithoutResponse() : Boolean =
        containsProperty(BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)

fun BluetoothGattDescriptor.isReadable() : Boolean =
        containsPermission(BluetoothGattDescriptor.PERMISSION_READ) ||
                containsPermission(BluetoothGattDescriptor.PERMISSION_READ_ENCRYPTED) ||
                containsPermission(BluetoothGattDescriptor.PERMISSION_READ_ENCRYPTED_MITM)

fun BluetoothGattDescriptor.isWritable() : Boolean =
        containsPermission(BluetoothGattDescriptor.PERMISSION_WRITE) ||
                containsPermission(BluetoothGattDescriptor.PERMISSION_WRITE_ENCRYPTED) ||
                containsPermission(BluetoothGattDescriptor.PERMISSION_WRITE_ENCRYPTED_MITM) ||
                containsPermission(BluetoothGattDescriptor.PERMISSION_WRITE_SIGNED) ||
                containsPermission(BluetoothGattDescriptor.PERMISSION_WRITE_SIGNED_MITM)

fun BluetoothGattCharacteristic.isIndicatable() : Boolean =
        containsProperty(BluetoothGattCharacteristic.PROPERTY_INDICATE)

fun BluetoothGattCharacteristic.isNotifiable() : Boolean =
        containsProperty(BluetoothGattCharacteristic.PROPERTY_NOTIFY)

fun BluetoothGattDescriptor.containsPermission(permission : Int) : Boolean =
        permissions and permission != 0

fun BluetoothGattCharacteristic.containsProperty(property : Int) : Boolean {
    return properties and property != 0
}

fun BluetoothGatt.printGattTable() {
    if(services.isEmpty()) {
        DebugLog.i("No service and characteristic available, call discoverServices() first?")
        return
    }
    services.forEach { service ->
        val characteristicsTable = service.characteristics.joinToString(
            separator = "\n|--",
            prefix = "|--"
        ) { char ->
            var description = "${char.uuid}: ${char.printProperties()}"
            if(char.descriptors.isNotEmpty()) {
                description += "\n" + char.descriptors.joinToString(
                    separator = "\n|------",
                    prefix = "|------"
                ) { descriptor ->
                    "${descriptor.uuid}: ${descriptor.printProperties()}"
                }
            }
            description
        }
        DebugLog.i("Service ${service.uuid}\nCharacteristics:\n$characteristicsTable")
    }
}

fun BluetoothGattCharacteristic.printProperties() : String = mutableListOf<String>().apply {
    if(isReadable()) add("READABLE")
    if(isWritable()) add("WRITABLE")
    if(isWritableWithoutResponse()) add("WRITABLE WITHOUT RESPONSE")
    if(isIndicatable()) add("INDICATABLE")
    if(isNotifiable()) add("NOTIFIABLE")
    if(isEmpty()) add("EMPTY")
}.joinToString()

fun BluetoothGatt.findCharacteristic(uuid : UUID) : BluetoothGattCharacteristic? {
    services?.forEach { service ->
        service.characteristics?.firstOrNull { characteristic ->
            characteristic.uuid == uuid
        }?.let { matchingCharacteristic ->
            return matchingCharacteristic
        }
    }
    return null
}

fun BluetoothGatt.findDescriptor(uuid : UUID) : BluetoothGattDescriptor? {
    services?.forEach { service ->
        service.characteristics.forEach { characteristic ->
            characteristic.descriptors?.firstOrNull { descriptor ->
                descriptor.uuid == uuid
            }?.let { matchingDescriptor ->
                return matchingDescriptor
            }
        }
    }
    return null
}

fun BluetoothGattDescriptor.printProperties() : String = mutableListOf<String>().apply {
    if(isReadable()) add("READABLE")
    if(isWritable()) add("WRITABLE")
    if(isEmpty()) add("EMPTY")
}.joinToString()

fun BluetoothGattDescriptor.isCccd() =
        uuid.toString().toUpperCase(Locale.US) == CCC_DESCRIPTOR_UUID.toUpperCase(Locale.US)

// ByteArray
fun ByteArray.toHexString() : String =
        joinToString(separator = " ", prefix = "0x") { String.format("%02X", it) }