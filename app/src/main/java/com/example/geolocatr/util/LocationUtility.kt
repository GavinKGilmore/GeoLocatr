package com.example.geolocatr.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStates
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.IOException

class LocationUtility(context: Context) {
    private val mCurrentLocationStateFlow: MutableStateFlow<Location?>
        = MutableStateFlow(null)
    val currentLocationStateFlow: StateFlow<Location?>
        get() = mCurrentLocationStateFlow.asStateFlow()

    private val mCurrentAddressStateFlow: MutableStateFlow<String?>
        = MutableStateFlow(null)
    val currentAddressStateFlow: StateFlow<String?>
        get() = mCurrentAddressStateFlow.asStateFlow()

    private val mLocationAvailability: MutableStateFlow<Boolean?>
        = MutableStateFlow(null)
    val locationAvailability: StateFlow<Boolean?>
        get() = mLocationAvailability.asStateFlow()


    // Location services components

    private val locationRequest = LocationRequest
        .Builder(Priority.PRIORITY_HIGH_ACCURACY, 0L)
        .setMaxUpdates(1)
        .build()

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            locationResult.lastLocation?.let { location ->
                mCurrentLocationStateFlow.value = location
                Log.d("LocationUtility", "Location received: $location")
            }
        }
    }

    private val fusedLocationProviderClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val geocoder = Geocoder(context)


    fun checkPermissionAndGetLocation(activity: Activity,
                                      permissionLauncher: ActivityResultLauncher<Array<String>>) {
        // Check if permissions are granted
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            // Permission has been granted
            Log.d("LocationPermission", "Location permission already granted")
            fusedLocationProviderClient
                .requestLocationUpdates(locationRequest,
                    locationCallback,
                    Looper.getMainLooper())
        } else {
            // Permission is currently not granted
            // check if we need to ask for permission
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION) ||
                ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_COARSE_LOCATION)
            ) {
                // User previously denied permission
                Log.d("LocationPermission", "Permission was denied previously")
                Toast.makeText(
                    activity,
                    "We must access your location to plot where you are",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                // Ask for permission for the first time
                Log.d("LocationPermission", "Requesting location permission")
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

    fun removeLocationRequest() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    suspend fun getAddress(location: Location?) {
        val addressTextBuilder = StringBuilder()
        if (location != null) {
            try {
                val addresses = geocoder.getFromLocation(location.latitude,
                    location.longitude,
                    1)
                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    for (i in 0..address.maxAddressLineIndex) {
                        if (i > 0) {
                            addressTextBuilder.append("\n")
                        }
                        addressTextBuilder.append( address.getAddressLine(i) )
                    }
                }
            } catch (e: IOException) {
                Log.e("Location Utility", "Error getting address", e)
            }
        }
        mCurrentAddressStateFlow.update { addressTextBuilder.toString() }
    }

    fun verifyLocationSettingsStates(states: LocationSettingsStates?) {
        mLocationAvailability.update { states?.isLocationUsable ?: false }
    }

    fun checkIfLocationCanBeRetrieved(
        activity: Activity,
        locationLauncher: ActivityResultLauncher<IntentSenderRequest>
    ) {
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        val client = LocationServices.getSettingsClient(activity)
        client.checkLocationSettings(builder.build()).apply {
            addOnSuccessListener { response ->
                verifyLocationSettingsStates(response.locationSettingsStates)
            }
            addOnFailureListener { exc ->
                mLocationAvailability.update { false }
                if (exc is ResolvableApiException) {
                    locationLauncher
                        .launch(IntentSenderRequest.Builder(exc.resolution).build())
                }
            }
        }
    }

    fun setStartingLocation(location: Location?) {
        if (location == null) {
            // Handle null case (maybe log or keep current location)
            return
        }

        // Create a new Location object with the updated coordinates
        val newLocation = Location(location.provider ?: "").apply {
            latitude = location.latitude
            longitude = location.longitude
            // Copy other relevant fields if needed
            time = location.time
            accuracy = location.accuracy
        }

        // Update the StateFlow with the new Location object
        mCurrentLocationStateFlow.value = newLocation

    }
}
