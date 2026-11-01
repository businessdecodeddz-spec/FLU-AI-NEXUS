package com.example.data.model

/**
 * Model representing a Medical / Industrial Scan object processed through the NEXUS local stack:
 * BizTalk Data Hub -> Chronos Scheduler -> Qwen / Gemma 4 (BitNet Quantized) -> Turbocompte Engine.
 */
data class MedicalImagingScan(
    val scanId: String,
    val subjectId: String,
    val modality: String, // DICOM_XRAY, CT_SCAN, MRI_BRAIN, INDUSTRIAL_ULTRASOUND
    val targetArea: String,
    val status: String, // RECEIVED, ROUTED, INFERRED, REPORTED
    val anomalyDetected: Boolean,
    val confidenceScore: Float, // 0.0 to 1.0
    val biztalkRouteStatus: String,
    val chronosSyncMs: Long,
    val bitnetQuantLevel: String, // i2_s (2-bit) / i1_s (1-bit)
    val turbocompteAccelFactor: String, // "4.8x CPU/RAM Boost"
    val llmModelUsed: String, // "Qwen-2.5-VL-Local (BitNet)" or "Gemma-4-Clinical (BitNet)"
    val clinicalFindings: String,
    val recommendation: String,
    val timestamp: Long = System.currentTimeMillis()
)
