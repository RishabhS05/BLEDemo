package com.example.bleexampleapp

import android.bluetooth.BluetoothDevice
import java.util.*

/** Abstract sealed class representing a type of BLE operation */
sealed class BleOperationType {
    abstract val device: BluetoothDevice
}

data class CharacteristicRead(
        override val device: BluetoothDevice,
        val characteristicUuid: UUID
) : BleOperationType()