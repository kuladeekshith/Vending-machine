package com.example.valuevendmonitor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { MonitorScreen() } }
    }
}

@Composable
private fun MonitorScreen() {
    var baseUrl by remember { mutableStateOf("https://your-authorized-server.example") }
    var machineId by remember { mutableStateOf("VV-001") }
    var token by remember { mutableStateOf("") }
    var status by remember { mutableStateOf<MachineStatus?>(null) }
    var message by remember { mutableStateOf("Enter your operator-provided API details.") }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("ValueVend Monitor", style = MaterialTheme.typography.headlineMedium)
            Text("Read-only machine and transaction status")
            OutlinedTextField(
                value = baseUrl, onValueChange = { baseUrl = it },
                label = { Text("Operator API base URL (HTTPS)") },
                modifier = Modifier.fillMaxWidth(), singleLine = true
            )
            OutlinedTextField(
                value = machineId, onValueChange = { machineId = it },
                label = { Text("Machine ID") },
                modifier = Modifier.fillMaxWidth(), singleLine = true
            )
            OutlinedTextField(
                value = token, onValueChange = { token = it },
                label = { Text("Bearer token") },
                modifier = Modifier.fillMaxWidth(), singleLine = true
            )
            Button(
                enabled = !loading && token.isNotBlank() && machineId.isNotBlank(),
                onClick = {
                    scope.launch {
                        loading = true
                        message = "Checking authorized endpoint…"
                        val result = runCatching {
                            MonitorRepository.fetch(baseUrl.trim(), machineId.trim(), token)
                        }
                        result.onSuccess {
                            status = it
                            message = "Last checked: ${it.checkedAt ?: "not provided"}"
                        }.onFailure {
                            message = "Unable to retrieve status: ${it.message ?: "Unknown error"}"
                        }
                        loading = false
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (loading) "Checking…" else "Refresh status") }

            HorizontalDivider()
            Text("Machine status", style = MaterialTheme.typography.titleLarge)
            StatusLine("Machine", status?.machineId ?: "—")
            StatusLine("Connectivity", status?.connectivity ?: "Unknown")
            StatusLine("Operational state", status?.operationalState ?: "Unknown")
            StatusLine("Payment service", status?.paymentService ?: "Unknown")
            StatusLine("Last transaction", status?.lastTransactionStatus ?: "—")
            StatusLine("Last transaction time", status?.lastTransactionAt ?: "—")
            StatusLine("Error code", status?.errorCode ?: "—")
            Text(message, style = MaterialTheme.typography.bodySmall)
            Text(
                "This app only sends an authenticated HTTPS GET request to the endpoint configured by the machine operator. It does not initiate payments, change machine settings, unlock service menus, or issue dispense commands.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun StatusLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}
