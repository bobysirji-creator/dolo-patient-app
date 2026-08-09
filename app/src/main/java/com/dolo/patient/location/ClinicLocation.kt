package com.dolo.patient.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import androidx.core.content.ContextCompat
import java.util.Locale

sealed interface ApproximateLocationResult {
    data class Available(val latitude: Double, val longitude: Double) : ApproximateLocationResult
    data class Unavailable(val message: String) : ApproximateLocationResult
}

object ApproximateLocationProvider {
    fun hasPermission(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    fun request(context: Context, onResult: (ApproximateLocationResult) -> Unit) {
        if (!hasPermission(context)) {
            onResult(ApproximateLocationResult.Unavailable("Approximate location permission is required."))
            return
        }
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        if (manager == null) {
            onResult(ApproximateLocationResult.Unavailable("Location is unavailable on this device."))
            return
        }

        val providers = listOf(
            LocationManager.NETWORK_PROVIDER,
            LocationManager.PASSIVE_PROVIDER,
            LocationManager.GPS_PROVIDER
        ).filter { provider -> runCatching { manager.isProviderEnabled(provider) }.getOrDefault(false) }

        val cached = providers.mapNotNull { provider ->
            runCatching { manager.getLastKnownLocation(provider) }.getOrNull()
        }.maxByOrNull { it.time }
        if (cached != null) {
            onResult(ApproximateLocationResult.Available(cached.latitude, cached.longitude))
            return
        }

        val provider = providers.firstOrNull()
        if (provider == null) {
            onResult(ApproximateLocationResult.Unavailable("Turn on device location and try again."))
            return
        }

        var delivered = false
        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                if (delivered) return
                delivered = true
                manager.removeUpdates(this)
                onResult(ApproximateLocationResult.Available(location.latitude, location.longitude))
            }

            @Deprecated("Deprecated in Android")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit
            override fun onProviderEnabled(provider: String) = Unit
            override fun onProviderDisabled(provider: String) = Unit
        }
        runCatching {
            manager.requestSingleUpdate(provider, listener, Looper.getMainLooper())
        }.onFailure {
            onResult(ApproximateLocationResult.Unavailable("Approximate location could not be read."))
        }
    }
}

object ClinicNavigation {
    fun directionsUrl(latitude: Double, longitude: Double): String {
        require(latitude in -90.0..90.0 && longitude in -180.0..180.0)
        val destination = String.format(Locale.US, "%.6f,%.6f", latitude, longitude)
        return "https://www.google.com/maps/dir/?api=1&destination=$destination&travelmode=driving"
    }

    fun open(context: Context, latitude: Double, longitude: Double): Boolean {
        val uri = Uri.parse(directionsUrl(latitude, longitude))
        val generalIntent = Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val googleMapsIntent = Intent(generalIntent).setPackage("com.google.android.apps.maps")
        val selected = if (googleMapsIntent.resolveActivity(context.packageManager) != null) {
            googleMapsIntent
        } else {
            generalIntent
        }
        return runCatching {
            context.startActivity(selected)
            true
        }.getOrDefault(false)
    }
}
