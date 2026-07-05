package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "vehicles")
data class Vehicle(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: String, // Sedan, SUV, Tesla, Sports, Luxury
    val region: String, // North America, Europe, Asia Pacific, Australia, South America
    val basePricePerDay: Double,
    val currentSeasonalMultiplier: Double = 1.0,
    val isAvailable: Boolean = true,
    val latitude: Double,
    val longitude: Double,
    val speedKmh: Int = 0,
    val fuelLevel: Int = 100 // Battery / Fuel percentage
)

@Entity(tableName = "bookings")
data class Booking(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val vehicleId: String,
    val vehicleName: String,
    val driverName: String,
    val licenseNumber: String,
    val pickupDate: String,
    val returnDate: String,
    val totalCost: Double,
    val region: String,
    val status: String, // "Active", "Completed", "Cancelled"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "accidents")
data class AccidentReport(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val bookingId: String,
    val vehicleName: String,
    val region: String,
    val latitude: Double,
    val longitude: Double,
    val description: String,
    val severity: String, // "Low", "Moderate", "Critical"
    val isEmergencyDispatched: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "driver_documents")
data class DriverDocument(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val driverName: String,
    val documentType: String, // "Driver's License", "Passport", "Corporate ID"
    val encryptedData: String, // Base64 simulated encrypted string
    val isVerified: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
