package com.example.bleexampleapp

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor

fun BluetoothGattCharacteristic.isReadable() : Boolean =
        containsProperty(BluetoothGattCharacteristic.PROPERTY_READ)

fun BluetoothGattCharacteristic.isWritable() : Boolean =
        containsProperty(BluetoothGattCharacteristic.PROPERTY_WRITE)

fun BluetoothGattCharacteristic.isWritableWithoutResponse() : Boolean =
        containsProperty(BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)

fun BluetoothGattCharacteristic.containsProperty(property : Int) : Boolean {
    return properties and property != 0
}

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

fun BluetoothGattDescriptor.containsPermission(permission : Int) : Boolean =
        permissions and permission != 0

// ByteArray
fun ByteArray.toHexString() : String =
        joinToString(separator = " ", prefix = "0x") { String.format("%02X", it) }