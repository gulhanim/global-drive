package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Booking
import com.example.data.model.Vehicle
import com.example.ui.theme.*
import com.example.ui.viewmodel.FleetViewModel
import com.example.ui.viewmodel.RegionInfo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

enum class AppTab {
    Booking, Fleet, Emergency
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: FleetViewModel) {
    val selectedTab = remember { mutableStateOf(AppTab.Booking) }
    val latestAlert by viewModel.latestAlert.collectAsState()
    val currentLang by viewModel.currentLanguage.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DirectionsCar,
                            contentDescription = "App Logo",
                            tint = CyanPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = "GLOBALDRIVE",
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.5.sp,
                            color = WhiteText,
                            fontSize = 20.sp
                        )
                    }
                },
                actions = {
                    // Language selector
                    Box {
                        var expanded by remember { mutableStateOf(false) }
                        IconButton(onClick = { expanded = true }) {
                            Icon(Icons.Default.Language, contentDescription = "Language", tint = CyanPrimary)
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(SlateSurface)
                        ) {
                            DropdownMenuItem(
                                text = { Text("English (EN)", color = WhiteText) },
                                onClick = { viewModel.changeLanguage("en"); expanded = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Español (ES)", color = WhiteText) },
                                onClick = { viewModel.changeLanguage("es"); expanded = false }
                            )
                            DropdownMenuItem(
                                text = { Text("日本語 (JA)", color = WhiteText) },
                                onClick = { viewModel.changeLanguage("ja"); expanded = false }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SlateDark,
                    titleContentColor = WhiteText
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = SlateSurface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab.value == AppTab.Booking,
                    onClick = { selectedTab.value = AppTab.Booking },
                    label = { Text(if (currentLang == "es") "Reservar" else if (currentLang == "ja") "予約" else "Bookings") },
                    icon = { Icon(Icons.Rounded.CalendarToday, contentDescription = "Book") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SlateDark,
                        selectedTextColor = CyanPrimary,
                        unselectedIconColor = GrayText,
                        unselectedTextColor = GrayText,
                        indicatorColor = CyanPrimary
                    )
                )
                NavigationBarItem(
                    selected = selectedTab.value == AppTab.Fleet,
                    onClick = { selectedTab.value = AppTab.Fleet },
                    label = { Text(if (currentLang == "es") "Flota" else if (currentLang == "ja") "フリート" else "Global Fleet") },
                    icon = { Icon(Icons.Rounded.Analytics, contentDescription = "Fleet Analytics") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SlateDark,
                        selectedTextColor = CyanPrimary,
                        unselectedIconColor = GrayText,
                        unselectedTextColor = GrayText,
                        indicatorColor = CyanPrimary
                    )
                )
                NavigationBarItem(
                    selected = selectedTab.value == AppTab.Emergency,
                    onClick = { selectedTab.value = AppTab.Emergency },
                    label = { Text(if (currentLang == "es") "Emergencia" else if (currentLang == "ja") "緊急支援" else "Emergency") },
                    icon = { Icon(Icons.Rounded.ReportProblem, contentDescription = "Emergency") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SlateDark,
                        selectedTextColor = CyanPrimary,
                        unselectedIconColor = GrayText,
                        unselectedTextColor = GrayText,
                        indicatorColor = CyanPrimary
                    )
                )
            }
        },
        containerColor = SlateDark
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Live Critical System Alerts
            AnimatedVisibility(
                visible = latestAlert != null,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                latestAlert?.let { alert ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        colors = CardDefaults.cardColors(containerColor = RedAlert),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = "System Alert", tint = Color.White)
                                Text(
                                    text = alert,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            IconButton(onClick = { viewModel.clearAlert() }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                            }
                        }
                    }
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when (selectedTab.value) {
                    AppTab.Booking -> BookingTab(viewModel)
                    AppTab.Fleet -> FleetAnalyticsTab(viewModel)
                    AppTab.Emergency -> EmergencyTab(viewModel)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 1: ROBUST BOOKING SYSTEM
// -------------------------------------------------------------
@Composable
fun BookingTab(viewModel: FleetViewModel) {
    val vehicles by viewModel.vehicles.collectAsState()
    val selectedRegion by viewModel.selectedRegion.collectAsState()
    val demandLevel by viewModel.demandForecastingLevel.collectAsState()
    val currentLang by viewModel.currentLanguage.collectAsState()

    val regions = listOf("North America", "Europe", "Asia Pacific", "Australia", "South America")
    val regionInfo = viewModel.regionsMap[selectedRegion]!!

    var bookingVehicle by remember { mutableStateOf<Vehicle?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Header with Image Generation Asset
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_fleet_banner),
                    contentDescription = "Fleet banner",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Overlay Gradient
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, SlateDark.copy(alpha = 0.9f)),
                                startY = 100f
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(
                        text = if (currentLang == "es") "Reserva Global Inteligente" else if (currentLang == "ja") "スマートなグローバル予約" else "Smart Global Rental Engine",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.White
                    )
                    Text(
                        text = if (currentLang == "es") "Ajustes de precios basados en tendencias y previsión de demanda en tiempo real." else if (currentLang == "ja") "需要予測に基づいて価格を自動補正。" else "Automated dynamic seasonal adjustments and multi-region timezone calculations.",
                        fontSize = 12.sp,
                        color = CyanPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Region Selection chips
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = if (currentLang == "es") "Selecciona tu región operativa" else if (currentLang == "ja") "稼働エリア選択" else "Operational Region Selection",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = WhiteText
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    regions.forEach { region ->
                        val isSelected = region == selectedRegion
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.selectRegion(region) },
                            label = { Text(region, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanPrimary,
                                selectedLabelColor = SlateDark,
                                containerColor = SlateSurface,
                                labelColor = GrayText
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = BorderColor,
                                selectedBorderColor = CyanPrimary
                            )
                        )
                    }
                }
            }
        }

        // Timezone operational & support details card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                shape = RoundedCornerShape(12.dp),
                border = BoxBorder
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (currentLang == "es") "Horario de Oficina de la Región" else if (currentLang == "ja") "エリア営業時間情報" else "Region Timezone & Helplines",
                            fontWeight = FontWeight.Bold,
                            color = CyanPrimary,
                            fontSize = 14.sp
                        )
                        Box(
                            modifier = Modifier
                                .background(BorderColor, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = regionInfo.timezone,
                                fontSize = 10.sp,
                                color = WhiteText,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Render Working hours for timezone
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.AccessTime, contentDescription = "Hours", tint = GoldAccent, modifier = Modifier.size(16.dp))
                        Text(
                            text = "Working Hours: ${regionInfo.workingHours}",
                            fontSize = 12.sp,
                            color = WhiteText,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    HorizontalDivider(color = BorderColor)

                    // Different Daytime Support vs Night Support numbers
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = "Day Phone", tint = CyanPrimary, modifier = Modifier.size(16.dp))
                            Text(
                                text = "Daytime Helpline: ${regionInfo.supportLineDay}",
                                fontSize = 11.sp,
                                color = GrayText
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = "Emergency Night Phone", tint = RedAlert, modifier = Modifier.size(16.dp))
                            Text(
                                text = "Emergency/Night Support: ${regionInfo.supportLineNight}",
                                fontSize = 11.sp,
                                color = RedAlert,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Demand Forecasting Multiplier (interactive controller)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                shape = RoundedCornerShape(12.dp),
                border = BoxBorder
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (currentLang == "es") "Previsión de Demanda en Tiempo Real" else if (currentLang == "ja") "リアルタイム需要予測価格調整" else "Live Demand Forecasting Adjustment",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = WhiteText
                    )
                    Text(
                        text = if (currentLang == "es") "Ajusta dinámicamente la disponibilidad y la tarificación en función de las tendencias de la temporada actual (${regionInfo.currentSeason})." else if (currentLang == "ja") "現在のシーズン（${regionInfo.currentSeason}）の傾向を検知し自動調整します。" else "Dynamic forecasting is currently adjusting base rates based on the regional trend: ${regionInfo.currentSeason}.",
                        fontSize = 11.sp,
                        color = GrayText
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Historical Multiplier: x${regionInfo.seasonalMultiplier}",
                            fontSize = 12.sp,
                            color = GoldAccent,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Demand Factor: x${String.format("%.1f", demandLevel)}",
                            fontSize = 12.sp,
                            color = CyanPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Demand controller
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(1.0 to "Low", 1.2 to "Medium", 1.5 to "High Peak").forEach { (valMult, label) ->
                            val isSelected = demandLevel == valMult
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) CyanPrimary else SlateDark)
                                    .clickable { viewModel.setDemandLevel(valMult) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) SlateDark else WhiteText
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section Title: Available Fleet
        item {
            Text(
                text = if (currentLang == "es") "Flota disponible en la región" else if (currentLang == "ja") "エリア内配備車両" else "Available Region Fleet",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = WhiteText
            )
        }

        // Vehicles List
        val filteredVehicles = vehicles.filter { it.region == selectedRegion }
        if (filteredVehicles.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No vehicles found or loaded for this region.", color = GrayText)
                }
            }
        } else {
            items(filteredVehicles) { vehicle ->
                VehicleItemRow(
                    vehicle = vehicle,
                    viewModel = viewModel,
                    currentLang = currentLang,
                    onBookClicked = { bookingVehicle = vehicle }
                )
            }
        }
    }

    // Booking Dialog Modal
    if (bookingVehicle != null) {
        BookingModalDialog(
            vehicle = bookingVehicle!!,
            viewModel = viewModel,
            currentLang = currentLang,
            onDismiss = { bookingVehicle = null }
        )
    }
}

@Composable
fun VehicleItemRow(
    vehicle: Vehicle,
    viewModel: FleetViewModel,
    currentLang: String,
    onBookClicked: () -> Unit
) {
    val adjustedPrice = viewModel.getAdjustedPrice(vehicle.basePricePerDay)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("vehicle_item_card"),
        colors = CardDefaults.cardColors(containerColor = SlateSurface),
        shape = RoundedCornerShape(12.dp),
        border = BoxBorder
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(
                            modifier = Modifier
                                .background(CyanPrimary.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(vehicle.type, fontSize = 10.sp, color = CyanPrimary, fontWeight = FontWeight.Bold)
                        }
                        if (!vehicle.isAvailable) {
                            Box(
                                modifier = Modifier
                                    .background(RedAlert.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("RENTED / TRACKING", fontSize = 10.sp, color = RedAlert, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(vehicle.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = WhiteText)
                }

                // Price with original strikethrough vs adjusted price
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$${String.format("%.2f", adjustedPrice)}/day",
                        fontWeight = FontWeight.ExtraBold,
                        color = CyanPrimary,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Base rate: $${vehicle.basePricePerDay}",
                        fontSize = 11.sp,
                        color = GrayText
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Telemetry & Specs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.BatteryChargingFull, contentDescription = "Energy", tint = GoldAccent, modifier = Modifier.size(16.dp))
                        Text("${vehicle.fuelLevel}%", fontSize = 12.sp, color = WhiteText)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Speed, contentDescription = "Speed", tint = TealSecondary, modifier = Modifier.size(16.dp))
                        Text("${vehicle.speedKmh} Kmh", fontSize = 12.sp, color = WhiteText)
                    }
                }

                Button(
                    onClick = { onBookClicked() },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                    enabled = vehicle.isAvailable,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("book_button")
                ) {
                    Text(
                        text = if (!vehicle.isAvailable) "Rented" else "Reserve Now",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = SlateDark
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// BOOKING MODAL WITH SECURE WORKFLOW & USER MFA + UPLOAD
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingModalDialog(
    vehicle: Vehicle,
    viewModel: FleetViewModel,
    currentLang: String,
    onDismiss: () -> Unit
) {
    var step by remember { mutableStateOf(1) } // 1 = Details & Doc, 2 = MFA Check, 3 = Payment Gateway

    // Fields
    var driverName by remember { mutableStateOf("") }
    var licenseNumber by remember { mutableStateOf("") }
    var docType by remember { mutableStateOf("Driver's License") }
    var docContent by remember { mutableStateOf("") } // Simulated encrypted content

    // MFA inputs
    var phoneInput by remember { mutableStateOf("") }
    var codeInput by remember { mutableStateOf("") }
    val mfaSent by viewModel.mfaSent.collectAsState()
    val mfaVerified by viewModel.mfaVerified.collectAsState()
    val docStatus by viewModel.documentUploadStatus.collectAsState()

    var bookingDays by remember { mutableStateOf("3") }
    val calculatedPrice = viewModel.getAdjustedPrice(vehicle.basePricePerDay)
    val daysInt = bookingDays.toIntOrNull() ?: 1
    val totalCost = calculatedPrice * daysInt

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Text(
                text = "Secure Checkout - ${vehicle.name}",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = WhiteText
            )
        },
        containerColor = SlateSurface,
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("Cancel", color = GrayText)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Steps indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf("1. Regulatory", "2. Identity MFA", "3. Chase Gate").forEachIndexed { index, title ->
                        val isCurrent = step == index + 1
                        Text(
                            text = title,
                            color = if (isCurrent) CyanPrimary else GrayText,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.sp
                        )
                    }
                }

                HorizontalDivider(color = BorderColor)

                when (step) {
                    1 -> {
                        // STEP 1: Personal Details & Secure Doc Vault Upload
                        Text(
                            text = "Driver Information & Encrypted Vault",
                            fontWeight = FontWeight.Bold,
                            color = CyanPrimary,
                            fontSize = 14.sp
                        )

                        OutlinedTextField(
                            value = driverName,
                            onValueChange = { driverName = it },
                            label = { Text("Full Legal Name") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanPrimary,
                                unfocusedBorderColor = BorderColor,
                                focusedTextColor = WhiteText,
                                unfocusedTextColor = WhiteText
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = licenseNumber,
                            onValueChange = { licenseNumber = it },
                            label = { Text("License / Passport Number") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanPrimary,
                                unfocusedBorderColor = BorderColor,
                                focusedTextColor = WhiteText,
                                unfocusedTextColor = WhiteText
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = bookingDays,
                            onValueChange = { bookingDays = it },
                            label = { Text("Rental Duration (Days)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanPrimary,
                                unfocusedBorderColor = BorderColor,
                                focusedTextColor = WhiteText,
                                unfocusedTextColor = WhiteText
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Secure document storage simulation
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SlateCard),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.Lock, contentDescription = "Secure Vault", tint = GoldAccent, modifier = Modifier.size(16.dp))
                                    Text("Encrypted Document Vault (SOC2)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = WhiteText)
                                }
                                Text("Upload a photo or text representation of your documents. File is encrypted instantly before storing.", fontSize = 11.sp, color = GrayText)

                                OutlinedTextField(
                                    value = docContent,
                                    onValueChange = { docContent = it },
                                    label = { Text("Document Notes / Vault Text") },
                                    placeholder = { Text("e.g. License expiration, state issued...") },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = CyanPrimary,
                                        unfocusedBorderColor = BorderColor,
                                        focusedTextColor = WhiteText,
                                        unfocusedTextColor = WhiteText
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Button(
                                    onClick = { viewModel.uploadDocument(driverName, docType, docContent) },
                                    enabled = driverName.isNotBlank() && docContent.isNotBlank(),
                                    colors = ButtonDefaults.buttonColors(containerColor = TealSecondary),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Encrypt & Store Document", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SlateDark)
                                }

                                docStatus?.let { status ->
                                    Text(status, color = GoldAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Button(
                            onClick = { step = 2 },
                            enabled = driverName.isNotBlank() && licenseNumber.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Proceed to Identity Verification", fontWeight = FontWeight.Bold, color = SlateDark)
                        }
                    }
                    2 -> {
                        // STEP 2: Multi-Factor Authentication
                        Text(
                            text = "Multi-Factor Authentication",
                            fontWeight = FontWeight.Bold,
                            color = CyanPrimary,
                            fontSize = 14.sp
                        )
                        Text("To comply with international cross-border renting regulatory rules, verification via SMS MFA is required.", fontSize = 11.sp, color = GrayText)

                        OutlinedTextField(
                            value = phoneInput,
                            onValueChange = { phoneInput = it },
                            label = { Text("Mobile Phone Number") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanPrimary,
                                unfocusedBorderColor = BorderColor,
                                focusedTextColor = WhiteText,
                                unfocusedTextColor = WhiteText
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (!mfaSent) {
                            Button(
                                onClick = { viewModel.sendMfaCode(phoneInput) },
                                enabled = phoneInput.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Send Verification SMS", fontWeight = FontWeight.Bold, color = SlateDark)
                            }
                        } else {
                            OutlinedTextField(
                                value = codeInput,
                                onValueChange = { codeInput = it },
                                label = { Text("6-Digit SMS Verification Code") },
                                colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = CyanPrimary,
                                        unfocusedBorderColor = BorderColor,
                                        focusedTextColor = WhiteText,
                                        unfocusedTextColor = WhiteText
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Button(
                                onClick = { viewModel.verifyMfaCode(codeInput) },
                                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Verify Code", fontWeight = FontWeight.Bold, color = SlateDark)
                            }
                        }

                        if (mfaVerified) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Verified", tint = CyanPrimary)
                                Text("Verification Successful. Proceed to Checkout.", color = CyanPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            Button(
                                onClick = { step = 3 },
                                colors = ButtonDefaults.buttonColors(containerColor = TealSecondary),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Proceed to Payment Gateway", fontWeight = FontWeight.Bold, color = SlateDark)
                            }
                        }
                    }
                    3 -> {
                        // STEP 3: Secure cross-border gateway
                        Text(
                            text = "Cross-Border Settlement Gateway",
                            fontWeight = FontWeight.Bold,
                            color = CyanPrimary,
                            fontSize = 14.sp
                        )

                        Card(
                            colors = CardDefaults.cardColors(containerColor = SlateCard),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("TRANSACTION SUMMARY", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = GrayText)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Vehicle Selected:", fontSize = 12.sp, color = WhiteText)
                                    Text(vehicle.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = WhiteText)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Adjusted Daily Rate:", fontSize = 12.sp, color = WhiteText)
                                    Text("$${String.format("%.2f", calculatedPrice)}", fontSize = 12.sp, color = CyanPrimary, fontWeight = FontWeight.Bold)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Duration:", fontSize = 12.sp, color = WhiteText)
                                    Text("$daysInt Days", fontSize = 12.sp, color = WhiteText)
                                }
                                HorizontalDivider(color = BorderColor)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Total Price:", fontWeight = FontWeight.Bold, color = WhiteText, fontSize = 14.sp)
                                    Text("$${String.format("%.2f", totalCost)}", fontWeight = FontWeight.ExtraBold, color = CyanPrimary, fontSize = 18.sp)
                                }
                            }
                        }

                        Text("Using simulated AES-256 Chase SecurePay Gateway. Regulatory SOC2 standard is enforced.", fontSize = 11.sp, color = GrayText, textAlign = TextAlign.Center)

                        Button(
                            onClick = {
                                viewModel.createBooking(
                                    vehicle = vehicle,
                                    driverName = driverName,
                                    licenseNumber = licenseNumber,
                                    pickupDate = "2026-07-05",
                                    returnDate = "2026-07-08",
                                    days = daysInt,
                                    onSuccess = {
                                        viewModel.resetMfa()
                                        onDismiss()
                                    },
                                    onError = { /* Log error */ }
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Secure Settlement", fontWeight = FontWeight.Bold, color = SlateDark)
                        }
                    }
                }
            }
        }
    )
}

// -------------------------------------------------------------
// TAB 2: GLOBAL FLEET ANALYTICS & REAL-TIME GPS TRACKING
// -------------------------------------------------------------
@Composable
fun FleetAnalyticsTab(viewModel: FleetViewModel) {
    val vehicles by viewModel.vehicles.collectAsState()
    val bookings by viewModel.bookings.collectAsState()
    val currentLang by viewModel.currentLanguage.collectAsState()

    // Aggregate regional performance (Daily, Monthly, Yearly simulated data based on active database bookings)
    val regions = listOf("North America", "Europe", "Asia Pacific", "Australia", "South America")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = if (currentLang == "es") "Centro de Control de Flota Global" else if (currentLang == "ja") "グローバルフリート管理センター" else "Global Fleet Control Center",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = WhiteText
            )
            Text(
                text = if (currentLang == "es") "Supervisión analítica de ingresos regionales y seguimiento GPS en tiempo real." else if (currentLang == "ja") "エリア別の売上統計およびリアルタイムGPS追跡。" else "Analytics overviews and simulated real-time GPS telemetry from active fleet.",
                fontSize = 12.sp,
                color = GrayText
            )
        }

        // Live stats counter widgets
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = SlateSurface),
                    border = BoxBorder
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Active Fleet", fontSize = 11.sp, color = GrayText)
                        Text("${vehicles.size}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = CyanPrimary)
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = SlateSurface),
                    border = BoxBorder
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Active Rentings", fontSize = 11.sp, color = GrayText)
                        Text("${vehicles.count { !it.isAvailable }}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TealSecondary)
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = SlateSurface),
                    border = BoxBorder
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Total Bookings", fontSize = 11.sp, color = GrayText)
                        Text("${bookings.size}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = GoldAccent)
                    }
                }
            }
        }

        // regional revenue chart
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                shape = RoundedCornerShape(12.dp),
                border = BoxBorder
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Regional Revenue Performance (USD)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = WhiteText)

                    regions.forEach { reg ->
                        // Calculate metrics: Sum bookings total cost for this region
                        val regBookings = bookings.filter { it.region == reg }
                        val baseYearly = regBookings.sumOf { it.totalCost }
                        // Add default historical value so it is never empty and represents actual regional metrics
                        val historicalYearly = when (reg) {
                            "North America" -> 14500.0
                            "Europe" -> 18900.0
                            "Asia Pacific" -> 11200.0
                            "Australia" -> 9800.0
                            else -> 7400.0
                        }
                        val yearlyOutput = baseYearly + historicalYearly
                        val monthlyOutput = yearlyOutput / 12
                        val dailyOutput = monthlyOutput / 30

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(reg, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = WhiteText)
                                Text(
                                    text = "Day: $${String.format("%.1f", dailyOutput)} | Month: $${String.format("%.1f", monthlyOutput)} | Year: $${String.format("%.1f", yearlyOutput)}",
                                    fontSize = 10.sp,
                                    color = GrayText
                                )
                            }

                            // Progress Bar showing percentage of highest region (Europe)
                            val progress = (yearlyOutput / 30000.0).coerceIn(0.1, 1.0).toFloat()
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = if (reg == "Europe") CyanPrimary else TealSecondary,
                                trackColor = SlateDark
                            )
                        }
                    }
                }
            }
        }

        // Live GPS Tracking Feed
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Live Telemetry Feed (GPS Broadcast)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = WhiteText
                )
                Box(
                    modifier = Modifier
                        .background(CyanPrimary.copy(alpha = 0.15f), CircleShape)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(CyanPrimary, CircleShape)
                        )
                        Text("LIVE", fontSize = 9.sp, color = CyanPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        items(vehicles) { vehicle ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                shape = RoundedCornerShape(12.dp),
                border = BoxBorder
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.LocationOn,
                            contentDescription = "GPS Location",
                            tint = if (vehicle.speedKmh > 0) CyanPrimary else GrayText,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(vehicle.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = WhiteText)
                            Text(
                                text = "Lat: ${String.format("%.4f", vehicle.latitude)} | Lng: ${String.format("%.4f", vehicle.longitude)}",
                                fontSize = 11.sp,
                                color = GrayText
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${vehicle.speedKmh} Kmh",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (vehicle.speedKmh > 0) CyanPrimary else GrayText
                        )
                        Text(
                            text = "Fuel: ${vehicle.fuelLevel}%",
                            fontSize = 11.sp,
                            color = if (vehicle.fuelLevel > 50) TealSecondary else GoldAccent
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 3: EMERGENCY ACCIDENT RESPONSE CENTER & CHATBOT
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyTab(viewModel: FleetViewModel) {
    val bookings by viewModel.bookings.collectAsState()
    val chatHistory by viewModel.chatHistory.collectAsState()
    val chatLoading by viewModel.chatLoading.collectAsState()
    val currentLang by viewModel.currentLanguage.collectAsState()

    var selectedBookingForAccident by remember { mutableStateOf<Booking?>(null) }
    var accidentDescription by remember { mutableStateOf("") }
    var accidentSeverity by remember { mutableStateOf("Moderate") }
    var userMessage by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Emergency Crisis Dispatch Hub",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = WhiteText
            )
            Text(
                text = "Instant telemetry-based accident crash detectors. Triggers local hotlines based on live coordinates.",
                fontSize = 12.sp,
                color = GrayText
            )
        }

        // Active Booking Crash Detector Selector
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                shape = RoundedCornerShape(12.dp),
                border = BoxBorder
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Select Active Booking to Report Accident", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = WhiteText)

                    val activeBookings = bookings.filter { it.status == "Active" }
                    if (activeBookings.isEmpty()) {
                        Text("No active bookings found. Go to Bookings tab to make a rental booking first.", color = GrayText, fontSize = 12.sp)
                    } else {
                        var expanded by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { expanded = true },
                                colors = ButtonDefaults.buttonColors(containerColor = SlateDark),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    selectedBookingForAccident?.let { "${it.vehicleName} - ${it.driverName}" } ?: "Select Booking...",
                                    color = WhiteText
                                )
                            }

                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SlateSurface)
                            ) {
                                activeBookings.forEach { booking ->
                                    DropdownMenuItem(
                                        text = { Text("${booking.vehicleName} (${booking.driverName})", color = WhiteText) },
                                        onClick = {
                                            selectedBookingForAccident = booking
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }

                        if (selectedBookingForAccident != null) {
                            OutlinedTextField(
                                value = accidentDescription,
                                onValueChange = { accidentDescription = it },
                                label = { Text("Accident Description") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CyanPrimary,
                                    unfocusedBorderColor = BorderColor,
                                    focusedTextColor = WhiteText,
                                    unfocusedTextColor = WhiteText
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Severity Chips
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("Low", "Moderate", "Critical").forEach { sev ->
                                    val isSelected = accidentSeverity == sev
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) RedAlert else SlateDark)
                                            .clickable { accidentSeverity = sev }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = sev,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = WhiteText
                                        )
                                    }
                                }
                            }

                            Button(
                                onClick = {
                                    viewModel.triggerAccident(
                                        selectedBookingForAccident!!,
                                        accidentDescription,
                                        accidentSeverity
                                    )
                                    accidentDescription = ""
                                },
                                enabled = accidentDescription.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(containerColor = RedAlert),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Trigger Incident Reporting Protocol", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        // AI Chatbot responder interface
        if (chatHistory.isNotEmpty()) {
            item {
                Text(
                    text = "AI Emergency Crisis Chatbot",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = WhiteText
                )
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    colors = CardDefaults.cardColors(containerColor = SlateSurface),
                    border = BoxBorder
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        // Chats Display
                        Box(modifier = Modifier.weight(1f)) {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(chatHistory) { msg ->
                                    val isAi = msg.sender == "AI"
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = if (isAi) Arrangement.Start else Arrangement.End
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(if (isAi) SlateCard else CyanPrimary)
                                                .padding(12.dp)
                                                .widthIn(max = 240.dp)
                                        ) {
                                            Text(
                                                text = msg.text,
                                                fontSize = 12.sp,
                                                color = if (isAi) WhiteText else SlateDark,
                                                fontWeight = if (isAi) FontWeight.Normal else FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                if (chatLoading) {
                                    item {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.Start
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(SlateCard)
                                                    .padding(12.dp)
                                            ) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(16.dp),
                                                    color = CyanPrimary,
                                                    strokeWidth = 2.dp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Text input row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = userMessage,
                                onValueChange = { userMessage = it },
                                placeholder = { Text("Ask the Emergency Assist Bot...") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CyanPrimary,
                                    unfocusedBorderColor = BorderColor,
                                    focusedTextColor = WhiteText,
                                    unfocusedTextColor = WhiteText
                                ),
                                modifier = Modifier.weight(1f)
                            )

                            IconButton(
                                onClick = {
                                    val regionName = selectedBookingForAccident?.region ?: "North America"
                                    viewModel.sendEmergencyChatMessage(userMessage, regionName)
                                    userMessage = ""
                                },
                                enabled = userMessage.isNotBlank() && !chatLoading,
                                modifier = Modifier.background(CyanPrimary, CircleShape)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Send", tint = SlateDark)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Global visual styling helpers
val BoxBorder = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
