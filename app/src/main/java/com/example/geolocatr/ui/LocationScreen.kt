package com.example.geolocatr.ui

import android.location.Location
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.geolocatr.R

// TODO: Step 2

@Composable
fun LocationScreen(modifier: Modifier = Modifier,
                   location: Location?,
                   locationAvailable: Boolean,
                   onGetLocation: () -> Unit,
                   address: String) {
    Column {
        // TODO: Part 1.I
        // TODO: integrate null checks
        if(location != null) {
            Text(stringResource(R.string.lat_long))
            Text(stringResource(R.string.lat_long_display, location.latitude, location.longitude))
            Text(stringResource(R.string.address))
            Text(address)
            Button(enabled = locationAvailable,
                onClick = onGetLocation,
                ) {
                Text(stringResource(R.string.get_current_location))
            }
        } else {
            Text(stringResource(R.string.lat_long))
            Text(stringResource(R.string.lat_long_display, 0.0, 0.0))
            Text(stringResource(R.string.address))
            Text(address)
            Button(enabled = locationAvailable,
                onClick = onGetLocation,
            ) {
                Text(stringResource(R.string.get_current_location))
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