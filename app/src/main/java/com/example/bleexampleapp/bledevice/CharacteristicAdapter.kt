

package com.example.bleexampleapp.bledevice

import android.bluetooth.BluetoothGattCharacteristic
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bleexampleapp.databinding.RowCharacteristicBinding
import com.example.bleexampleapp.printProperties

class CharacteristicAdapter(
        private val items : List<BluetoothGattCharacteristic>,
        private val onClickListener : ((characteristic : BluetoothGattCharacteristic) -> Unit)
) : RecyclerView.Adapter<CharacteristicAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent : ViewGroup, viewType : Int) : ViewHolder {
        val view = RowCharacteristicBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view, onClickListener)
    }

    override fun getItemCount() = items.size
    override fun onBindViewHolder(holder : ViewHolder, position : Int) {
        val item = items[position]
        holder.bind(item)
    }

    class ViewHolder(
            private val view : RowCharacteristicBinding,
            private val onClickListener : ((characteristic : BluetoothGattCharacteristic) -> Unit)
    ) : RecyclerView.ViewHolder(view.root) {

        fun bind(characteristic : BluetoothGattCharacteristic) {
            view.characteristicUuid.text = characteristic.uuid.toString()
            view.characteristicProperties.text = characteristic.printProperties()
            view.root.setOnClickListener { onClickListener.invoke(characteristic) }
        }
    }
}
