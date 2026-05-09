package com.xpenseatlas.logic

data class CountryBounds(
    val name: String,
    val currency: String,
    val minLat: Double, val maxLat: Double,
    val minLng: Double, val maxLng: Double
)

object TravelManager {
    // Basic offline bounds for common travel destinations from India
    private val COUNTRIES = listOf(
        CountryBounds("United Arab Emirates", "AED", 22.6, 26.1, 51.5, 56.4),
        CountryBounds("Thailand", "THB", 5.6, 20.5, 97.3, 105.6),
        CountryBounds("Singapore", "SGD", 1.1, 1.5, 103.6, 104.1),
        CountryBounds("United Kingdom", "GBP", 49.9, 59.5, -8.6, 1.8),
        CountryBounds("United States", "USD", 24.4, 49.4, -124.8, -66.9)
    )

    fun getCountryFromLocation(lat: Double, lng: Double): CountryBounds? {
        return COUNTRIES.find { 
            lat in it.minLat..it.maxLat && lng in it.minLng..it.maxLng 
        }
    }
}
