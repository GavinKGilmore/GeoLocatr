package com.example.geolocatr.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.geolocatr.data.DataStoreManager

@Composable
fun UiSettings(
    modifier: Modifier = Modifier,
    buildingEnabled: Boolean,
    toolbarEnabled: Boolean,
    onBuildingChanged: (Boolean) -> Unit,
    onToolbarChanged: (Boolean) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Buildings toggle section
        Column {
            Text("Show Buildings:")
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = buildingEnabled,
                    onClick = { onBuildingChanged(true) }
                )
                Text(
                    text = "Enabled",
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .selectable(
                            selected = buildingEnabled,
                            onClick = { onBuildingChanged(true) }
                        )
                )
                RadioButton(
                    selected = !buildingEnabled,
                    onClick = { onBuildingChanged(false) }
                )
                Text(
                    text = "Disabled",
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .selectable(
                            selected = !buildingEnabled,
                            onClick = { onBuildingChanged(false) }
                        )
                )
            }
        }

        // Map toolbar toggle section
        Column {
            Text("Enable Zoom Controls:")
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = toolbarEnabled,
                    onClick = { onToolbarChanged(true) }
                )
                Text(
                    text = "Enabled",
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .selectable(
                            selected = toolbarEnabled,
                            onClick = { onToolbarChanged(true) }
                        )
                )
                RadioButton(
                    selected = !toolbarEnabled,
                    onClick = { onToolbarChanged(false) }
                )
                Text(
                    text = "Disabled",
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .selectable(
                            selected = !toolbarEnabled,
                            onClick = { onToolbarChanged(false) }
                        )
                )
            }
        }
    }
}