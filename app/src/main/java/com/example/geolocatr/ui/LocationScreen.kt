package com.example.geolocatr.ui

import android.location.Location
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun LocationScreen(modifier: Modifier = Modifier,
                   location: Location?,
                   locationAvailable: Boolean,
                   onGetLocation: () -> Unit,
                   address: String) {
    Column {
        // TODO: Part 1.I 
    }
}