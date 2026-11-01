package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ActionRecordEntity
import com.example.ui.theme.*

@Composable
fun ActionDialog(
    action: ActionRecordEntity,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "AUTORISATION REQUISE",
                    color = CyberAmber,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "L'action suivante comporte des modifications système sensibles :",
                    color = TextPrimary,
                    fontSize = 14.sp
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CardBackground, RoundedCornerShape(8.dp))
                        .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = action.title,
                            color = ElectricCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Département: ${action.departmentId}",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "Détails: ${action.details}",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                }
                Text(
                    text = "Voulez-vous accorder la permission d'exécution au NEXUS POLICY ENGINE ?",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = CyberAmber),
                modifier = Modifier.testTag("action_dialog_confirm")
            ) {
                Text("AUTORISER", color = DeepObsidian, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                modifier = Modifier.testTag("action_dialog_cancel")
            ) {
                Text("REFUSER")
            }
        },
        containerColor = DeepObsidian,
        shape = RoundedCornerShape(16.dp)
    )
}
