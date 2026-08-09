package com.dolo.patient.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dolo.patient.data.HostedPrototypePayment

@Composable
internal fun PrototypePaymentSimulationCard(payment: HostedPrototypePayment?, loading: Boolean, onRun: (String) -> Unit) {
    val scenarios = listOf(
        "CAPTURE_SUCCESS" to "Captured",
        "ZERO_CHARGE" to "Zero charge",
        "PAYMENT_FAILED" to "Failed",
        "PAYMENT_EXPIRED" to "Expired",
        "REFUND_AFTER_CAPTURE" to "Refunded"
    )
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Synthetic payment test lab", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Fixed test outcomes only. No card, UPI, bank, Patient or appointment data is sent. No real money moves.", style = MaterialTheme.typography.bodySmall)
            scenarios.chunked(2).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { (scenario, label) ->
                        OutlinedButton({ onRun(scenario) }, enabled = !loading, modifier = Modifier.weight(1f)) { Text(label) }
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }
            payment?.let {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(it.syntheticPaymentId, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                        Text(it.status.replace('_', ' '), fontWeight = FontWeight.Bold)
                        Text("Scenario: ${it.scenario.replace('_', ' ')}")
                        Text("Test amount: ${it.currency} ${formatPrototypeAmount(it.amountMinor)}")
                        Text("Booking eligible: ${if (it.bookingEligible) "Yes" else "No"} | Refund: ${it.refundStatus.replace('_', ' ')}")
                        Text("Synthetic only - no real payment, appointment or billing-ledger change.", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

private fun formatPrototypeAmount(amountMinor:Int):String="${amountMinor/100}.${(amountMinor%100).toString().padStart(2,'0')}"
