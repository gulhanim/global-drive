package com.example.data.repository

import com.example.data.dao.FleetDao
import com.example.data.model.Vehicle
import com.example.data.model.Booking
import com.example.data.model.AccidentReport
import com.example.data.model.DriverDocument
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.UUID

class FleetRepository(private val fleetDao: FleetDao) {

    val allVehicles: Flow<List<Vehicle>> = fleetDao.getAllVehicles()
    val allBookings: Flow<List<Booking>> = fleetDao.getAllBookings()
    val allAccidents: Flow<List<AccidentReport>> = fleetDao.getAllAccidents()
    val allDocuments: Flow<List<DriverDocument>> = fleetDao.getAllDocuments()

    fun getVehiclesByRegion(region: String): Flow<List<Vehicle>> = fleetDao.getVehiclesByRegion(region)

    suspend fun insertVehicle(vehicle: Vehicle) = fleetDao.insertVehicle(vehicle)
    suspend fun updateVehicle(vehicle: Vehicle) = fleetDao.updateVehicle(vehicle)

    suspend fun insertBooking(booking: Booking) {
        fleetDao.insertBooking(booking)
        // Mark vehicle as unavailable
        val vehicles = fleetDao.getAllVehicles().first()
        val vehicle = vehicles.find { it.id == booking.vehicleId }
        if (vehicle != null) {
            fleetDao.updateVehicle(vehicle.copy(isAvailable = false))
        }
    }

    suspend fun updateBooking(booking: Booking) {
        fleetDao.updateBooking(booking)
        if (booking.status == "Completed" || booking.status == "Cancelled") {
            // Make vehicle available again
            val vehicles = fleetDao.getAllVehicles().first()
            val vehicle = vehicles.find { it.id == booking.vehicleId }
            if (vehicle != null) {
                fleetDao.updateVehicle(vehicle.copy(isAvailable = true))
            }
        }
    }

    suspend fun insertAccident(accident: AccidentReport) {
        fleetDao.insertAccident(accident)
    }

    suspend fun insertDocument(document: DriverDocument) = fleetDao.insertDocument(document)
    suspend fun updateDocument(document: DriverDocument) = fleetDao.updateDocument(document)

    suspend fun seedIfNeeded() {
        val currentVehicles = fleetDao.getAllVehicles().first()
        if (currentVehicles.isEmpty()) {
            val sampleVehicles = listOf(
                // North America
                Vehicle(
                    id = "NA-001",
                    name = "Tesla Model S Plaid",
                    type = "Tesla",
                    region = "North America",
                    basePricePerDay = 150.0,
                    currentSeasonalMultiplier = 1.3, // Summer multiplier
                    isAvailable = true,
                    latitude = 40.7128,  // NYC
                    longitude = -74.0060,
                    speedKmh = 0,
                    fuelLevel = 98
                ),
                Vehicle(
                    id = "NA-002",
                    name = "Ford Mustang Mach-E",
                    type = "SUV",
                    region = "North America",
                    basePricePerDay = 95.0,
                    currentSeasonalMultiplier = 1.1,
                    isAvailable = true,
                    latitude = 34.0522,  // LA
                    longitude = -118.2437,
                    speedKmh = 65, // Simulated in transit
                    fuelLevel = 84
                ),
                // Europe
                Vehicle(
                    id = "EU-001",
                    name = "Porsche Taycan Turbo",
                    type = "Luxury",
                    region = "Europe",
                    basePricePerDay = 210.0,
                    currentSeasonalMultiplier = 1.4,
                    isAvailable = true,
                    latitude = 51.5074,  // London
                    longitude = -0.1278,
                    speedKmh = 0,
                    fuelLevel = 100
                ),
                Vehicle(
                    id = "EU-002",
                    name = "Audi e-tron GT",
                    type = "Sports",
                    region = "Europe",
                    basePricePerDay = 160.0,
                    currentSeasonalMultiplier = 1.2,
                    isAvailable = true,
                    latitude = 48.8566,  // Paris
                    longitude = 2.3522,
                    speedKmh = 110, // Simulated cruising on highway
                    fuelLevel = 52
                ),
                // Asia Pacific
                Vehicle(
                    id = "AP-001",
                    name = "Nissan GT-R Nismo",
                    type = "Sports",
                    region = "Asia Pacific",
                    basePricePerDay = 190.0,
                    currentSeasonalMultiplier = 1.1,
                    isAvailable = true,
                    latitude = 35.6762,  // Tokyo
                    longitude = 139.6503,
                    speedKmh = 0,
                    fuelLevel = 90
                ),
                Vehicle(
                    id = "AP-002",
                    name = "Toyota RAV4 Prime",
                    type = "SUV",
                    region = "Asia Pacific",
                    basePricePerDay = 75.0,
                    currentSeasonalMultiplier = 1.0,
                    isAvailable = true,
                    latitude = 1.3521,  // Singapore
                    longitude = 103.8198,
                    speedKmh = 40,
                    fuelLevel = 76
                ),
                // Australia
                Vehicle(
                    id = "AU-001",
                    name = "BMW iX M60",
                    type = "SUV",
                    region = "Australia",
                    basePricePerDay = 145.0,
                    currentSeasonalMultiplier = 1.2,
                    isAvailable = true,
                    latitude = -33.8688,  // Sydney
                    longitude = 151.2093,
                    speedKmh = 0,
                    fuelLevel = 100
                ),
                Vehicle(
                    id = "AU-002",
                    name = "Polestar 2 BST Edition",
                    type = "Sedan",
                    region = "Australia",
                    basePricePerDay = 105.0,
                    currentSeasonalMultiplier = 1.1,
                    isAvailable = true,
                    latitude = -37.8136,  // Melbourne
                    longitude = 144.9631,
                    speedKmh = 80,
                    fuelLevel = 62
                ),
                // South America
                Vehicle(
                    id = "SA-001",
                    name = "Mercedes-Benz EQC",
                    type = "Luxury",
                    region = "South America",
                    basePricePerDay = 135.0,
                    currentSeasonalMultiplier = 1.0,
                    isAvailable = true,
                    latitude = -23.5505,  // Sao Paulo
                    longitude = -46.6333,
                    speedKmh = 0,
                    fuelLevel = 89
                ),
                Vehicle(
                    id = "SA-002",
                    name = "Hyundai Ioniq 5",
                    type = "Sedan",
                    region = "South America",
                    basePricePerDay = 85.0,
                    currentSeasonalMultiplier = 1.1,
                    isAvailable = true,
                    latitude = -34.6037,  // Buenos Aires
                    longitude = -58.3816,
                    speedKmh = 0,
                    fuelLevel = 94
                )
            )
            fleetDao.insertVehicles(sampleVehicles)
        }
    }
}
