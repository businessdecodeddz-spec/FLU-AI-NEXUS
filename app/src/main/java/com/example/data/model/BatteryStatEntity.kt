package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "battery_stats")
data class BatteryStatEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val batteryLevel: Int, // 0 - 100 %
    val batteryTemperature: Float, // °C
    val voltageMv: Int, // e.g. 3850 mV
    val chargingStatus: String, // "CHARGING", "DISCHARGING", "FULL"
    val drainRatePercentPerHour: Float, // e.g. 14.5 %/hr
    val topDrainPackageName: String,
    val topDrainAppName: String,
    val topDrainPowerConsumptionMah: Float,
    val isHighDrainAlert: Boolean = false,
    val alertReason: String = ""
)
