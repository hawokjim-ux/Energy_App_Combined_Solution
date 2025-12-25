package com.energyapp.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Multi-Station Data Models
 * Supports 50+ gas stations with centralized management
 */

@Serializable
data class Station(
    @SerialName("station_id")
    val stationId: Int,
    
    @SerialName("station_code")
    val stationCode: String,
    
    @SerialName("station_name")
    val stationName: String,
    
    @SerialName("station_type")
    val stationType: String? = "petrol_station",
    
    // Location
    @SerialName("physical_address")
    val physicalAddress: String? = null,
    
    @SerialName("city")
    val city: String? = null,
    
    @SerialName("county")
    val county: String? = null,
    
    @SerialName("region")
    val region: String? = null,
    
    @SerialName("gps_latitude")
    val gpsLatitude: Double? = null,
    
    @SerialName("gps_longitude")
    val gpsLongitude: Double? = null,
    
    // M-Pesa Config (may be null for security)
    @SerialName("mpesa_till_number")
    val mpesaTillNumber: String? = null,
    
    // Contact
    @SerialName("station_phone")
    val stationPhone: String? = null,
    
    @SerialName("station_email")
    val stationEmail: String? = null,
    
    @SerialName("manager_name")
    val managerName: String? = null,
    
    @SerialName("manager_phone")
    val managerPhone: String? = null,
    
    // Operations
    @SerialName("operating_hours_start")
    val operatingHoursStart: String? = "06:00:00",
    
    @SerialName("operating_hours_end")
    val operatingHoursEnd: String? = "22:00:00",
    
    @SerialName("is_24_hours")
    val is24Hours: Boolean = false,
    
    // Status
    @SerialName("is_active")
    val isActive: Boolean = true,
    
    @SerialName("is_online")
    val isOnline: Boolean = true,
    
    @SerialName("last_sync_at")
    val lastSyncAt: String? = null,
    
    // Stats (from API with summaries)
    @SerialName("pump_count")
    val pumpCount: Int? = null,
    
    @SerialName("user_count")
    val userCount: Int? = null,
    
    @SerialName("active_shifts")
    val activeShifts: Int? = null,
    
    @SerialName("today_sales")
    val todaySales: Double? = null,
    
    @SerialName("today_transactions")
    val todayTransactions: Int? = null,
    
    // User-specific (when queried by user)
    @SerialName("station_role")
    val stationRole: String? = null,
    
    @SerialName("is_primary_station")
    val isPrimaryStation: Boolean? = null,
    
    @SerialName("can_view_reports")
    val canViewReports: Boolean? = null,
    
    @SerialName("can_manage_pumps")
    val canManagePumps: Boolean? = null,
    
    @SerialName("created_at")
    val createdAt: String? = null,
    
    @SerialName("updated_at")
    val updatedAt: String? = null
) {
    /**
     * Get display name with location
     */
    fun getDisplayName(): String {
        return if (city != null) "$stationName ($city)" else stationName
    }
    
    /**
     * Get short display for dropdowns
     */
    fun getShortName(): String {
        return "$stationCode - $stationName"
    }
    
    /**
     * Check if user has admin access
     */
    fun hasAdminAccess(): Boolean {
        return stationRole in listOf("super_admin", "station_admin", "manager")
    }
    
    /**
     * Get formatted today's sales
     */
    fun getFormattedTodaySales(): String {
        val sales = todaySales ?: 0.0
        return "KES ${String.format("%,.2f", sales)}"
    }
}

@Serializable
data class StationsListResponse(
    @SerialName("success")
    val success: Boolean,
    
    @SerialName("message")
    val message: String,
    
    @SerialName("data")
    val data: List<Station>,
    
    @SerialName("count")
    val count: Int? = null,
    
    @SerialName("summary")
    val summary: StationsSummary? = null
)

@Serializable
data class StationsSummary(
    @SerialName("total_stations")
    val totalStations: Int,
    
    @SerialName("total_today_sales")
    val totalTodaySales: Double,
    
    @SerialName("total_today_transactions")
    val totalTodayTransactions: Int
)

@Serializable
data class StationResponse(
    @SerialName("success")
    val success: Boolean,
    
    @SerialName("message")
    val message: String,
    
    @SerialName("data")
    val data: Station? = null,
    
    @SerialName("station_id")
    val stationId: Int? = null
)

@Serializable
data class CreateStationRequest(
    @SerialName("station_code")
    val stationCode: String,
    
    @SerialName("station_name")
    val stationName: String,
    
    @SerialName("station_type")
    val stationType: String = "petrol_station",
    
    @SerialName("physical_address")
    val physicalAddress: String? = null,
    
    @SerialName("city")
    val city: String? = null,
    
    @SerialName("county")
    val county: String? = null,
    
    @SerialName("region")
    val region: String? = null,
    
    @SerialName("gps_latitude")
    val gpsLatitude: Double? = null,
    
    @SerialName("gps_longitude")
    val gpsLongitude: Double? = null,
    
    @SerialName("mpesa_till_number")
    val mpesaTillNumber: String? = null,
    
    @SerialName("mpesa_shortcode")
    val mpesaShortcode: String? = null,
    
    @SerialName("station_phone")
    val stationPhone: String? = null,
    
    @SerialName("station_email")
    val stationEmail: String? = null,
    
    @SerialName("manager_name")
    val managerName: String? = null,
    
    @SerialName("manager_phone")
    val managerPhone: String? = null,
    
    @SerialName("operating_hours_start")
    val operatingHoursStart: String = "06:00:00",
    
    @SerialName("operating_hours_end")
    val operatingHoursEnd: String = "22:00:00",
    
    @SerialName("is_24_hours")
    val is24Hours: Boolean = false,
    
    @SerialName("fuel_types")
    val fuelTypes: List<String>? = null,
    
    @SerialName("created_by")
    val createdBy: String? = null
)

/**
 * User-Station Assignment for multi-station access
 */
@Serializable
data class UserStationAssignment(
    @SerialName("id")
    val id: Int,
    
    @SerialName("user_id")
    val userId: String,
    
    @SerialName("station_id")
    val stationId: Int,
    
    @SerialName("station_role")
    val stationRole: String = "attendant",
    
    @SerialName("is_primary_station")
    val isPrimaryStation: Boolean = false,
    
    @SerialName("can_view_reports")
    val canViewReports: Boolean = false,
    
    @SerialName("can_manage_pumps")
    val canManagePumps: Boolean = false,
    
    @SerialName("can_manage_users")
    val canManageUsers: Boolean = false,
    
    @SerialName("can_manage_shifts")
    val canManageShifts: Boolean = false,
    
    @SerialName("is_active")
    val isActive: Boolean = true,
    
    @SerialName("assigned_at")
    val assignedAt: String? = null
)

/**
 * Multi-Station Sales Summary for Admin Dashboard
 */
@Serializable
data class MultiStationSalesSummary(
    @SerialName("station_id")
    val stationId: Int,
    
    @SerialName("station_name")
    val stationName: String,
    
    @SerialName("total_sales")
    val totalSales: Double,
    
    @SerialName("transaction_count")
    val transactionCount: Int,
    
    @SerialName("mpesa_sales")
    val mpesaSales: Double,
    
    @SerialName("mpesa_count")
    val mpesaCount: Int
) {
    val cashSales: Double
        get() = totalSales - mpesaSales
    
    val mpesaPercentage: Double
        get() = if (totalSales > 0) (mpesaSales / totalSales) * 100 else 0.0
}
