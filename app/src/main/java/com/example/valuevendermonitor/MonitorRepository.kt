package com.example.valuevendmonitor

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

data class MachineStatus(
    val machineId: String?,
    val connectivity: String?,
    val operationalState: String?,
    val paymentService: String?,
    val lastTransactionStatus: String?,
    val lastTransactionAt: String?,
    val errorCode: String?,
    val checkedAt: String?
)

object MonitorRepository {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .callTimeout(20, TimeUnit.SECONDS)
        .build()

    suspend fun fetch(baseUrl: String, machineId: String, token: String): MachineStatus =
        withContext(Dispatchers.IO) {
            require(baseUrl.startsWith("https://", ignoreCase = true)) {
                "Use the HTTPS API URL supplied by your operator."
            }
            val cleanBase = baseUrl.trimEnd('/')
            val encodedId = URLEncoder.encode(machineId, Charsets.UTF_8.name())
            val request = Request.Builder()
                .url("$cleanBase/api/v1/machines/$encodedId/status")
                .header("Authorization", "Bearer $token")
                .header("Accept", "application/json")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IllegalStateException("HTTP ${response.code}. Check API access and machine ID.")
                }
                val body = response.body?.string()
                    ?: throw IllegalStateException("Empty response from monitoring API.")
                val json = JSONObject(body)
                MachineStatus(
                    machineId = json.optString("machineId").takeUnless { it == "null" },
                    connectivity = json.optString("connectivity").takeUnless { it == "null" },
                    operationalState = json.optString("operationalState").takeUnless { it == "null" },
                    paymentService = json.optString("paymentService").takeUnless { it == "null" },
                    lastTransactionStatus = json.optString("lastTransactionStatus").takeUnless { it == "null" },
                    lastTransactionAt = json.optString("lastTransactionAt").takeUnless { it == "null" },
                    errorCode = json.optString("errorCode").takeUnless { it == "null" },
                    checkedAt = json.optString("checkedAt").takeUnless { it == "null" }
                )
            }
        }
}
