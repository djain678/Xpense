package com.xpenseatlas.logic

import android.content.Context
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import com.xpenseatlas.data.Transaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

object PartnerSync {
    private const val SERVICE_ID = "com.xpenseatlas.SYNC"
    private val STRATEGY = Strategy.P2P_POINT_TO_POINT

    fun startAdvertising(context: Context, onResult: (String) -> Unit) {
        val options = AdvertisingOptions.Builder().setStrategy(STRATEGY).build()
        Nearby.getConnectionsClient(context).startAdvertising(
            "Partner", SERVICE_ID, object : ConnectionLifecycleCallback() {
                override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
                    Nearby.getConnectionsClient(context).acceptConnection(endpointId, object : PayloadCallback() {
                        override fun onPayloadReceived(id: String, payload: Payload) {
                            payload.asBytes()?.let { onResult(String(it)) }
                        }
                        override fun onPayloadTransferUpdate(id: String, update: PayloadTransferUpdate) {}
                    })
                }
                override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {}
                override fun onDisconnected(endpointId: String) {}
            }, options
        )
    }

    fun startDiscovery(context: Context, onFound: (String) -> Unit) {
        val options = DiscoveryOptions.Builder().setStrategy(STRATEGY).build()
        Nearby.getConnectionsClient(context).startDiscovery(
            SERVICE_ID, object : EndpointDiscoveryCallback() {
                override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
                    onFound(endpointId)
                }
                override fun onEndpointLost(endpointId: String) {}
            }, options
        )
    }

    fun sendData(context: Context, endpointId: String, transactions: List<Transaction>) {
        val jsonArray = JSONArray()
        transactions.forEach {
            val obj = JSONObject()
            obj.put("amount", it.amount)
            obj.put("vendor", it.vendor)
            obj.put("timestamp", it.timestamp)
            jsonArray.put(obj)
        }
        val payload = Payload.fromBytes(jsonArray.toString().toByteArray())
        Nearby.getConnectionsClient(context).sendPayload(endpointId, payload)
    }
}
