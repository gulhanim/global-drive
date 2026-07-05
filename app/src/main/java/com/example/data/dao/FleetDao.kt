package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Vehicle
import com.example.data.model.Booking
import com.example.data.model.AccidentReport
import com.example.data.model.DriverDocument
import kotlinx.coroutines.flow.Flow

@Dao
interface FleetDao {
    // Vehicles
    @Query("SELECT * FROM vehicles")
    fun getAllVehicles(): Flow<List<Vehicle>>

    @Query("SELECT * FROM vehicles WHERE region = :region")
    fun getVehiclesByRegion(region: String): Flow<List<Vehicle>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicle(vehicle: Vehicle)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicles(vehicles: List<Vehicle>)

    @Update
    suspend fun updateVehicle(vehicle: Vehicle)

    // Bookings
    @Query("SELECT * FROM bookings ORDER BY timestamp DESC")
    fun getAllBookings(): Flow<List<Booking>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: Booking)

    @Update
    suspend fun updateBooking(booking: Booking)

    // Accidents
    @Query("SELECT * FROM accidents ORDER BY timestamp DESC")
    fun getAllAccidents(): Flow<List<AccidentReport>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccident(accident: AccidentReport)

    // Driver Documents
    @Query("SELECT * FROM driver_documents ORDER BY timestamp DESC")
    fun getAllDocuments(): Flow<List<DriverDocument>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: DriverDocument)

    @Update
    suspend fun updateDocument(document: DriverDocument)
}
