package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MedicalImagingScan
import com.example.ui.theme.*

@Composable
fun ResearchScreen(
    currentScan: MedicalImagingScan?,
    onTriggerMedicalScan: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedModality by remember { mutableStateOf("DICOM_XRAY") }
    var selectedArea by remember { mutableStateOf("Poumons / Thorax") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkNavy)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Science, contentDescription = null, tint = NeonPurple)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = "RECHERCHE SCIENTIFIQUE & IMAGERIE MÉDICALE", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = "Stack local: BizTalk Data Hub -> Chronos -> Qwen/Gemma4 BitNet -> Turbocompte", color = TextSecondary, fontSize = 11.sp)
                }
            }
        }

        // Scanner Trigger Panel
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DeepObsidian),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(NeonPurple)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("SIMULATEUR D'IMAGERIE MÉDICALE & INDUSTRIELLE", color = NeonPurple, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Inférence clinique hors-ligne avec rapport automatisé structuré", color = TextSecondary, fontSize = 11.sp)

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { selectedModality = "DICOM_XRAY"; selectedArea = "Poumons / Thorax" },
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (selectedModality == "DICOM_XRAY") NeonPurple.copy(alpha = 0.2f) else CardBackground,
                                contentColor = if (selectedModality == "DICOM_XRAY") ElectricCyan else TextSecondary
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (selectedModality == "DICOM_XRAY") ElectricCyan else CardBorder),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Radio Thorax", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = { selectedModality = "CT_SCAN"; selectedArea = "Cérébral / Encephale" },
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (selectedModality == "CT_SCAN") NeonPurple.copy(alpha = 0.2f) else CardBackground,
                                contentColor = if (selectedModality == "CT_SCAN") ElectricCyan else TextSecondary
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (selectedModality == "CT_SCAN") ElectricCyan else CardBorder),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Scanner CT", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { onTriggerMedicalScan(selectedModality, selectedArea) },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_trigger_medical_scan")
                    ) {
                        Icon(imageVector = Icons.Default.Biotech, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("LANCER L'ANALYSE RAPIDE BITNET (QWEN / GEMMA 4)", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Generated Report Result View
        if (currentScan != null) {
            item {
                Text("COMPTE RENDU CLINIQUE GÉNÉRÉ (AIR-GAPPED SOUVERAIN)", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    shape = RoundedCornerShape(14.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(if (currentScan.anomalyDetected) CyberRed else CyberGreen)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Description, contentDescription = null, tint = ElectricCyan)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = currentScan.scanId, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (currentScan.anomalyDetected) CyberRed.copy(alpha = 0.2f) else CyberGreen.copy(alpha = 0.2f),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (currentScan.anomalyDetected) "ANOMALIE DÉTECTÉE" else "EXAMEN NORMAL",
                                    color = if (currentScan.anomalyDetected) CyberRed else CyberGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        ReportDetailLine("Sujet / Patient ID", currentScan.subjectId)
                        ReportDetailLine("Modalité / Zone", "${currentScan.modality} (${currentScan.targetArea})")
                        ReportDetailLine("Modèle IA Local", currentScan.llmModelUsed)
                        ReportDetailLine("Quantification", currentScan.bitnetQuantLevel)
                        ReportDetailLine("Accélération CPU", currentScan.turbocompteAccelFactor)
                        ReportDetailLine("Indice de Confiance", "${(currentScan.confidenceScore * 100).toInt()}%")

                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(DarkNavy, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text("CONSTATATIONS CLINIQUE:", color = ElectricCyan, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(currentScan.clinicalFindings, color = TextPrimary, fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(currentScan.recommendation, color = CyberAmber, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = currentScan.biztalkRouteStatus,
                            color = TextMuted,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportDetailLine(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = TextSecondary, fontSize = 11.sp)
        Text(text = value, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}
