package com.example.geolocatr.ui

import android.location.Location
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.geolocatr.R
import com.example.geolocatr.data.DataStoreManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// TODO: Step 2

@Composable
fun LocationScreen(modifier: Modifier = Modifier,
                   location: Location?,
                   locationAvailable: Boolean,
                   onGetLocation: () -> Unit,
                   address: String,
                   onNotify: (Location) -> Unit) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 0f)
    }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val dataStoreManager = remember { DataStoreManager(context) }

    // collect:
    val buildingState = dataStoreManager
        .buildingFlow
        .collectAsStateWithLifecycle(
            initialValue = true,
            lifecycle = lifecycleOwner.lifecycle
        )

    val toolbarState = dataStoreManager
        .toolbarFlow
        .collectAsStateWithLifecycle(
            initialValue = true,
            lifecycle = lifecycleOwner.lifecycle
        )

    val mapUiSettings = MapUiSettings(
        zoomControlsEnabled = toolbarState.value
    )

    val mapProperties = MapProperties(
        isBuildingEnabled = buildingState.value
    )

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
            Row(modifier = modifier, horizontalArrangement = Arrangement.SpaceBetween) {
                Button(enabled = locationAvailable,
                    onClick = onGetLocation,
                ) {
                    Text(stringResource(R.string.get_current_location))
                }
                Button(enabled = true, onClick = { onNotify(location) }) {
                    Text("Remind Me Later")
                }
            }

        } else {
            Text(stringResource(R.string.lat_long))
            Text(stringResource(R.string.lat_long_display, 0.0, 0.0))
            Text(stringResource(R.string.address))
            Text("No Current Address")
            Row(modifier = modifier, horizontalArrangement = Arrangement.SpaceBetween) {
                Button(enabled = locationAvailable,
                    onClick = onGetLocation,
                ) {
                    Text(stringResource(R.string.get_current_location))
                }
                Button(enabled = false, onClick = {}) {
                    Text("Remind Me Later")
                }


            }

        }


        UiSettings(
            modifier = modifier,
            buildingEnabled = buildingState.value,
            toolbarEnabled = toolbarState.value,
            onBuildingChanged = { newValue ->
                CoroutineScope(Dispatchers.IO).launch {
                    dataStoreManager.setBuildings(newValue)
                }
            },
            onToolbarChanged = {newValue ->
                CoroutineScope(Dispatchers.IO).launch {
                    dataStoreManager.setToolbar(newValue)
                }}
        )
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = mapUiSettings,
            properties = mapProperties
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
        address = addressState.value,
        onNotify = {}
    )
}