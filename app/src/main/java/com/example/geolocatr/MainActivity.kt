package com.example.geolocatr

import android.location.Location
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.registerForActivityResult
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.geolocatr.ui.LocationScreen
import com.example.geolocatr.ui.theme.GeoLocatrTheme
import com.example.geolocatr.util.LocationAlarmReceiver
import com.example.geolocatr.util.LocationUtility
import com.google.android.gms.location.LocationSettingsStates

class MainActivity : ComponentActivity() {

    private lateinit var locationUtility: LocationUtility
    private lateinit var locationPermissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var notificationPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var locationLauncher: ActivityResultLauncher<IntentSenderRequest>
    private val locationAlarmReceiver = LocationAlarmReceiver()

    override fun onCreate(savedInstanceState: Bundle?) {
        locationUtility = LocationUtility(this)

        locationPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
                // Step 5
                // process if permissions were granted
                locationUtility.checkPermissionAndGetLocation(this@MainActivity, locationPermissionLauncher)
            }

        notificationPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                locationAlarmReceiver.checkPermissionAndScheduleAlarm(this@MainActivity, notificationPermissionLauncher)
            }

        locationLauncher = registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.let { data ->
                    val states = LocationSettingsStates.fromIntent(data)
                    locationUtility.verifyLocationSettingsStates(states)
                }
            }
        }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val locationState = locationUtility
                .currentLocationStateFlow
                .collectAsStateWithLifecycle(lifecycle = this@MainActivity.lifecycle)
            val addressState = locationUtility
                .currentAddressStateFlow
                .collectAsStateWithLifecycle(lifecycle = this@MainActivity.lifecycle)



            GeoLocatrTheme {
                LaunchedEffect(locationState.value) {
                    locationUtility.getAddress(locationState.value)
                }
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LocationScreen(modifier = Modifier.padding(innerPadding),
                        location = locationState.value,
                        locationAvailable = true,
                        onGetLocation = {
                            locationUtility.checkPermissionAndGetLocation(this@MainActivity, locationPermissionLauncher)
                        },
                        address = addressState.value.toString(),
                        onNotify = { lastLocation ->
                            locationAlarmReceiver.lastLocation = lastLocation
                            locationAlarmReceiver.checkPermissionAndScheduleAlarm(
                                activity = this@MainActivity,
                                permissionLauncher = notificationPermissionLauncher
                            ) // TODO: implement everything here
                        }
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        locationUtility.removeLocationRequest()
        super.onDestroy()
    }

    override fun onStart() {
        locationUtility
            .checkIfLocationCanBeRetrieved(this, locationLauncher)
        super.onStart()
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    GeoLocatrTheme {
        Greeting("Android")
    }
}