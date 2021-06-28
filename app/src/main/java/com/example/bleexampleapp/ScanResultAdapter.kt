/*
 * Copyright 2019 Punch Through Design LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.bleexampleapp

import android.bluetooth.le.ScanResult
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bleexampleapp.databinding.RowScanResultBinding

class ScanResultAdapter(
        private val items : List<ScanResult>,
        private val onClickListener : ((device : ScanResult) -> Unit)
) : RecyclerView.Adapter<ScanResultAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent : ViewGroup, viewType : Int) : ViewHolder {
        val rowScanResultBinding = RowScanResultBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ViewHolder(rowScanResultBinding, onClickListener)
    }

    override fun getItemCount() = items.size
    override fun onBindViewHolder(holder : ViewHolder, position : Int) {
        val item = items[position]
        holder.bind(item)
    }

    class ViewHolder(
            private val rowScanResultBinding : RowScanResultBinding,
            private val onClickListener : ((device : ScanResult) -> Unit)
    ) : RecyclerView.ViewHolder(rowScanResultBinding.root) {
        fun bind(result : ScanResult) {
            rowScanResultBinding.deviceName.text = result.device.name ?: "Unnamed"
            rowScanResultBinding.macAddress.text = result.device.address
            rowScanResultBinding.signalStrength.text = "${result.rssi} dBm"
            rowScanResultBinding.root.setOnClickListener { onClickListener.invoke(result) }
        }
    }
}
