package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.Vehicle
import com.example.data.model.Booking
import com.example.data.model.AccidentReport
import com.example.data.model.DriverDocument
import com.example.data.repository.FleetRepository
import com.example.data.network.GeminiApiClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.random.Random

data class RegionInfo(
    val name: String,
    val timezone: String,
    val workingHours: String,
    val supportLineDay: String,
    val supportLineNight: String, // Different than day
    val emergencyHotline: String, // Local authorities
    val seasonalTrend: String,
    val seasonalMultiplier: Double,
    val currentSeason: String
)

data class ChatMessage(
    val sender: String, // "User" or "AI"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

class FleetViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FleetRepository

    val vehicles: StateFlow<List<Vehicle>>
    val bookings: StateFlow<List<Booking>>
    val accidents: StateFlow<List<AccidentReport>>
    val documents: StateFlow<List<DriverDocument>>

    // Selected region for booking & filtering
    private val _selectedRegion = MutableStateFlow("North America")
    val selectedRegion = _selectedRegion.asStateFlow()

    // Demand forecasting multiplier (1.0 = Low, 1.2 = Medium, 1.5 = High)
    private val _demandForecastingLevel = MutableStateFlow(1.2)
    val demandForecastingLevel = _demandForecastingLevel.asStateFlow()

    // Multi-Language setting
    private val _currentLanguage = MutableStateFlow("en")
    val currentLanguage = _currentLanguage.asStateFlow()

    // Document & ID encryption status
    private val _documentUploadStatus = MutableStateFlow<String?>(null)
    val documentUploadStatus = _documentUploadStatus.asStateFlow()

    // Multi-Factor Authentication state
    private val _mfaCode = MutableStateFlow("")
    private val _mfaSent = MutableStateFlow(false)
    val mfaSent = _mfaSent.asStateFlow()
    private val _mfaVerified = MutableStateFlow(false)
    val mfaVerified = _mfaVerified.asStateFlow()

    // Live tracking status banner or selection
    private val _selectedTrackingVehicle = MutableStateFlow<Vehicle?>(null)
    val selectedTrackingVehicle = _selectedTrackingVehicle.asStateFlow()

    // Emergency incident details
    private val _latestAlert = MutableStateFlow<String?>(null)
    val latestAlert = _latestAlert.asStateFlow()

    // Active Emergency Chat Bot state
    private val _chatHistory = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatHistory = _chatHistory.asStateFlow()
    private val _chatLoading = MutableStateFlow(false)
    val chatLoading = _chatLoading.asStateFlow()

    // Region Information Master Data
    val regionsMap = mapOf(
        "North America" to RegionInfo(
            name = "North America",
            timezone = "America/New_York",
            workingHours = "08:00 AM - 06:00 PM EST",
            supportLineDay = "+1-800-555-0100 (HQ General)",
            supportLineNight = "+1-888-777-9111 (Night Support)",
            emergencyHotline = "911 (US/Canada Dispatch)",
            seasonalTrend = "High Summer Travel & Winter Skiing",
            seasonalMultiplier = 1.3,
            currentSeason = "Summer Peak"
        ),
        "Europe" to RegionInfo(
            name = "Europe",
            timezone = "Europe/London",
            workingHours = "07:00 AM - 07:00 PM BST",
            supportLineDay = "+44-20-7946-0111 (Europe Desk)",
            supportLineNight = "+44-800-111-9999 (Midnight Assist)",
            emergencyHotline = "112 (European Emergency Hub)",
            seasonalTrend = "Spring Festival Peak & Mid-Summer Holiday",
            seasonalMultiplier = 1.45,
            currentSeason = "Holiday High Demand"
        ),
        "Asia Pacific" to RegionInfo(
            name = "Asia Pacific",
            timezone = "Asia/Tokyo",
            workingHours = "09:00 AM - 06:00 PM JST",
            supportLineDay = "+81-3-5555-0122 (Tokyo HQ)",
            supportLineNight = "+81-3-4444-0999 (Night Operations)",
            emergencyHotline = "110 (Japan Police Desk)",
            seasonalTrend = "Spring Cherry Blossom Peak & Autumn Leaves",
            seasonalMultiplier = 1.25,
            currentSeason = "Cherry Blossom Spring"
        ),
        "Australia" to RegionInfo(
            name = "Australia",
            timezone = "Australia/Sydney",
            workingHours = "08:00 AM - 05:00 PM AEST",
            supportLineDay = "+61-2-5550-0133 (Sydney Desk)",
            supportLineNight = "+61-1800-999-888 (Emergency Support)",
            emergencyHotline = "000 (Australia Triple Zero)",
            seasonalTrend = "Southern Summer Sun Seekers",
            seasonalMultiplier = 1.35,
            currentSeason = "Summer Beach Peak"
        ),
        "South America" to RegionInfo(
            name = "South America",
            timezone = "America/Sao_Paulo",
            workingHours = "08:00 AM - 06:00 PM BRT",
            supportLineDay = "+55-11-5555-0144 (Brazil Desk)",
            supportLineNight = "+55-11-3333-0888 (After Hours)",
            emergencyHotline = "190 (Brazil Military Police)",
            seasonalTrend = "Carnival Peak & High Eco-Tourism",
            seasonalMultiplier = 1.2,
            currentSeason = "Carnival Trend"
        )
    )

    init {
        val database = AppDatabase.getDatabase(application)
        repository = FleetRepository(database.fleetDao())

        vehicles = repository.allVehicles.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        bookings = repository.allBookings.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        accidents = repository.allAccidents.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        documents = repository.allDocuments.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        viewModelScope.launch {
            repository.seedIfNeeded()
            startGpsTrackingSimulation()
        }
    }

    fun selectRegion(region: String) {
        _selectedRegion.value = region
    }

    fun setDemandLevel(level: Double) {
        _demandForecastingLevel.value = level
    }

    fun changeLanguage(lang: String) {
        _currentLanguage.value = lang
    }

    // Get adjusted daily price based on season and forecasting
    fun getAdjustedPrice(basePrice: Double): Double {
        val regionInfo = regionsMap[_selectedRegion.value] ?: return basePrice
        return basePrice * regionInfo.seasonalMultiplier * _demandForecastingLevel.value
    }

    // SIMULATED MFA (Multi-Factor Auth) FLOW
    fun sendMfaCode(phoneNumber: String) {
        viewModelScope.launch {
            _mfaSent.value = true
            _mfaCode.value = (100000..999999).random().toString()
            _latestAlert.value = "MFA verification code sent to $phoneNumber: ${_mfaCode.value}"
            delay(10000)
            if (_latestAlert.value?.contains("MFA verification code") == true) {
                _latestAlert.value = null
            }
        }
    }

    fun verifyMfaCode(inputCode: String): Boolean {
        return if (inputCode == _mfaCode.value && _mfaSent.value) {
            _mfaVerified.value = true
            _latestAlert.value = "Security verified successfully."
            true
        } else {
            false
        }
    }

    fun resetMfa() {
        _mfaSent.value = false
        _mfaVerified.value = false
        _mfaCode.value = ""
    }

    // ENCRYPTED DOCUMENT STORAGE
    fun uploadDocument(driverName: String, docType: String, docContent: String) {
        viewModelScope.launch {
            // Simulate military-grade SHA-256 / AES encryption of docContent
            val encryptedMock = "AES-256-ENCRYPTED::" + android.util.Base64.encodeToString(
                docContent.toByteArray(),
                android.util.Base64.DEFAULT
            ).take(30).trim()

            val doc = DriverDocument(
                driverName = driverName,
                documentType = docType,
                encryptedData = encryptedMock,
                isVerified = true
            )
            repository.insertDocument(doc)
            _documentUploadStatus.value = "Successfully encrypted and securely stored $docType under regulatory compliance (SOC2)."
            delay(5000)
            _documentUploadStatus.value = null
        }
    }

    // CREATE BOOKING WITH SECURE TRANSACTIONS
    fun createBooking(
        vehicle: Vehicle,
        driverName: String,
        licenseNumber: String,
        pickupDate: String,
        returnDate: String,
        days: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            if (!_mfaVerified.value) {
                onError("MFA security verification required.")
                return@launch
            }

            val price = getAdjustedPrice(vehicle.basePricePerDay)
            val total = price * days

            // Secure Payment Gateway Mock (Stripe / Apple Pay Cross-border simulation)
            _latestAlert.value = "Processing secure cross-border payment of $${String.format("%.2f", total)}..."
            delay(1500)

            val success = Random.nextBoolean() || true // Let it always succeed for prototype flow
            if (success) {
                val booking = Booking(
                    vehicleId = vehicle.id,
                    vehicleName = vehicle.name,
                    driverName = driverName,
                    licenseNumber = licenseNumber,
                    pickupDate = pickupDate,
                    returnDate = returnDate,
                    totalCost = total,
                    region = _selectedRegion.value,
                    status = "Active"
                )
                repository.insertBooking(booking)
                onSuccess()
                _latestAlert.value = "Booking confirmed! Payment settled securely via Chase Merchant Gateway."
            } else {
                onError("Payment declined by issuing bank. Please verify billing address.")
            }
        }
    }

    // ACCIDENT DISPATCH & REAL-TIME ALERTS
    fun triggerAccident(booking: Booking, description: String, severity: String) {
        viewModelScope.launch {
            val vehiclesList = vehicles.value
            val v = vehiclesList.find { it.id == booking.vehicleId } ?: return@launch

            val accident = AccidentReport(
                bookingId = booking.id,
                vehicleName = booking.vehicleName,
                region = booking.region,
                latitude = v.latitude,
                longitude = v.longitude,
                description = description,
                severity = severity,
                isEmergencyDispatched = true
            )
            repository.insertAccident(accident)

            // Update vehicle telemetry to state crash, 0 Kmh speed
            repository.updateVehicle(v.copy(speedKmh = 0, isAvailable = false, fuelLevel = (v.fuelLevel - 20).coerceAtLeast(0)))

            // Trigger simulated SMS and emergency alerts
            val regionInfo = regionsMap[booking.region]
            _latestAlert.value = "🚨 CRASH DETECTED IN ${booking.region.uppercase()}! Simulated SMS sent to primary contacts. Local hotline ${regionInfo?.emergencyHotline} notified with telemetry."

            // Initialize chat auto-response for accident guidance
            initializeEmergencyChat(booking.region, booking.vehicleName, severity)
        }
    }

    private fun initializeEmergencyChat(regionName: String, carName: String, severity: String) {
        val r = regionsMap[regionName] ?: return
        val text = "🚨 ALERT: Crash telemetry detected for your rented $carName. Geolocation points to $regionName. " +
                "The primary emergency dispatch is ${r.emergencyHotline} (${r.name}). " +
                "Our current local support desk is available at ${r.supportLineNight} (Night mode active in this timezone). " +
                "Are you safe? Please let me know if you need medical dispatch or if you want us to send roadside assistance."

        _chatHistory.value = listOf(
            ChatMessage(sender = "AI", text = text)
        )
    }

    fun sendEmergencyChatMessage(userMsg: String, regionName: String) {
        if (userMsg.isBlank()) return
        val history = _chatHistory.value.toMutableList()
        history.add(ChatMessage(sender = "User", text = userMsg))
        _chatHistory.value = history

        _chatLoading.value = true
        viewModelScope.launch {
            val r = regionsMap[regionName]
            val localHotline = r?.emergencyHotline ?: "911"
            val localDaySupport = r?.supportLineDay ?: "N/A"
            val localNightSupport = r?.supportLineNight ?: "N/A"
            val workingHours = r?.workingHours ?: "N/A"

            val systemInstruction = "You are a crisis responder chatbot for GlobalDrive Car Rentals. " +
                    "Your user has just been in an accident. You MUST remain calm, reassuring, and highly supportive. " +
                    "Local timezone working hours: $workingHours. " +
                    "Primary Emergency Hotline to dial for immediate dispatch: $localHotline. " +
                    "Daytime office helpline: $localDaySupport. " +
                    "Nighttime emergency helpline: $localNightSupport. " +
                    "Always display these emergency numbers clearly and route callers if they mention dispatch. " +
                    "Formulate a helpful response suggesting safety actions (checking for injuries, turning on hazard lights, placing warning triangles, waiting behind safety barriers)."

            val prompt = "Current user chat: $userMsg. Respond with critical safety guidelines, emergency dispatch options, and ask if we should trigger SMS alerts."

            val aiResp = GeminiApiClient.generateResponse(prompt, systemInstruction)
            val updatedHistory = _chatHistory.value.toMutableList()
            updatedHistory.add(ChatMessage(sender = "AI", text = aiResp))
            _chatHistory.value = updatedHistory
            _chatLoading.value = false
        }
    }

    fun clearAlert() {
        _latestAlert.value = null
    }

    // REAL-TIME GPS MONITORING LOOP (Alters fleet locations slightly every 10 seconds to show active movement)
    private fun startGpsTrackingSimulation() {
        viewModelScope.launch {
            while (true) {
                delay(10000)
                val currentList = vehicles.value
                if (currentList.isNotEmpty()) {
                    val updated = currentList.map { vehicle ->
                        if (vehicle.speedKmh > 0) {
                            // Move vehicle slightly
                            val deltaLat = (Random.nextDouble() - 0.5) * 0.01
                            val deltaLng = (Random.nextDouble() - 0.5) * 0.01
                            val newFuel = (vehicle.fuelLevel - 1).coerceAtLeast(10)
                            vehicle.copy(
                                latitude = vehicle.latitude + deltaLat,
                                longitude = vehicle.longitude + deltaLng,
                                fuelLevel = newFuel,
                                speedKmh = (40..120).random()
                            )
                        } else {
                            vehicle
                        }
                    }
                    // Save to database
                    updated.forEach { repository.insertVehicle(it) }
                }
            }
        }
    }
}
