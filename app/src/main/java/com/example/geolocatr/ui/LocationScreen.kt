package com.example.geolocatr.ui

import android.location.Location
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.geolocatr.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

// TODO: Step 2

@Composable
fun LocationScreen(modifier: Modifier = Modifier,
                   location: Location?,
                   locationAvailable: Boolean,
                   onGetLocation: () -> Unit,
                   address: String) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 0f)
    }
    val context = LocalContext.current
    LaunchedEffect(location) {
        if(location != null) {
            // include all points that should be within the bounds of the zoom
            // convex hull
            val bounds = LatLngBounds.Builder()
                .include(LatLng(location.latitude, location.longitude))
                .build()
            // add padding
            val padding = context.resources
                .getDimensionPixelSize(R.dimen.map_inset_padding)
            // create a camera update to smoothly move the map view
            val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding)
            // move our camera!
            cameraPositionState.animate(cameraUpdate)
        }
    }
    Column(modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly) {
        if(location != null) {
            Text(stringResource(R.string.lat_long))
            Text(stringResource(R.string.lat_long_display, location.latitude, location.longitude))
            Text(stringResource(R.string.address))
            Text(address.toString())
            Button(enabled = locationAvailable,
                onClick = onGetLocation,
                ) {
                Text(stringResource(R.string.get_current_location))
            }
        } else {
            Text(stringResource(R.string.lat_long))
            Text(stringResource(R.string.lat_long_display, 0.0, 0.0))
            Text(stringResource(R.string.address))
            Text("No Current Address")
            Button(enabled = locationAvailable,
                onClick = onGetLocation,
            ) {
                Text(stringResource(R.string.get_current_location))
            }
        }
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState
        ) {
            if(location != null) {
                val markerState = MarkerState().apply {
                    position = LatLng(location.latitude, location.longitude)
                }
                Marker(
                    state = markerState,
                    title = address,
                    snippet = "${location.latitude} / ${location.longitude}"
                )
            }
        }
    }

}


@Preview(showBackground = true)
@Composable
private fun PreviewLocationScreen() {
    val locationState = remember { mutableStateOf<Location?>(null) }
    val addressState = remember { mutableStateOf("") }
    LocationScreen(
        location = locationState.value,
        locationAvailable = true,
        onGetLocation = {
            locationState.value = Location("").apply {
                latitude = 1.35
                longitude = 103.87
            }
            addressState.value = "Singapore"
        },
        address = addressState.value
    )
}