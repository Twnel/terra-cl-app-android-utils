package com.twnel.android_components.input

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.twnel.android_components.utils.PermissionContent


@Composable
fun LocationInput(
    location: Location? = null,
    setLocation: (Location) -> Unit,
    sendLocation: () -> Unit,
    cancel: () -> Unit,
    editLocation: Boolean = false,
    isFullScreen: Boolean = false,
    onToggleFullScreen: () -> Unit = {},
    sendText: String,
    cancelText: String,
    allowLocationText: String,
    enableLocationText: String,
    settingsButtonText: String,
    contentFullScreenText: String,
    enableLocationPermissionText: String,
    checkPermission: Boolean = true
) {
    val context = LocalContext.current

    var permissionGranted by remember { mutableStateOf(false) }
    var locationEnabled by remember { mutableStateOf(false) }

    val arrayPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
    )
    var isGettingLocation by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        checkPermissionAndLocationStatus(context) { hasPermission, isLocationEnabled ->
            permissionGranted = hasPermission
            locationEnabled = isLocationEnabled
        }
    }

    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissionGranted = permissions.any { it.value }
            if (permissionGranted) {
                locationEnabled = isLocationEnabled(context)
            }
        }

    val launcherLocationEnabled =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            locationEnabled = isLocationEnabled(context)
        }

    when {
        !permissionGranted && checkPermission -> {
            PermissionRequest(
                onRequestPermission = { launcher.launch(arrayPermissions) },
                onCancel = cancel,
                enableLocationPermissionText = enableLocationPermissionText,
                allowLocationText = allowLocationText,
                cancelText = cancelText
            )
        }

        !locationEnabled && checkPermission -> {
            LocationEnableRequest(
                onEnableLocation = { launcherLocationEnabled.launch(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) },
                onCancel = cancel,
                enableLocationText = enableLocationText,
                settingsButtonText = settingsButtonText,
                cancelText = cancelText
            )
        }

        isGettingLocation -> {
            LoadingIndicator()
            getLastLocation(context) { loc ->
                setLocation(loc)
                isGettingLocation = false
            }
        }

        location != null -> {
            val positionMap = LatLng(location.latitude, location.longitude)
            val cameraPositionState = remember { CameraPositionState() }
            LaunchedEffect(positionMap) {
                cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(positionMap, 15f))
            }
            if (isFullScreen) {
                BackHandler {
                    cancel()
                }
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .background(Color.Transparent)
                        .padding(top = 8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .padding(horizontal = 8.dp)
                    ) {
                        ReusableGoogleMap(
                            cameraPositionState = cameraPositionState,
                            positionMap = positionMap,
                            setLocation = setLocation,
                            editLocation = editLocation,
                            isRounded = true,
                            uiSettings = MapUiSettings()
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(70.dp)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = { cancel() }, modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.secondaryContainer.copy(
                                        alpha = 0.8f
                                    ), CircleShape
                                )
                                .size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = cancelText,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        IconButton(
                            onClick = { sendLocation() }, modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.secondaryContainer.copy(
                                        alpha = 0.8f
                                    ), CircleShape
                                )
                                .size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = sendText,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(16.dp))

                    ) {
                        ReusableGoogleMap(
                            cameraPositionState = cameraPositionState,
                            positionMap = positionMap,
                            setLocation = setLocation,
                            editLocation = editLocation
                        )
                    }
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        IconButton(
                            onClick = { sendLocation() },
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f),
                                    CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = sendText
                            )
                        }
                        IconButton(
                            onClick = onToggleFullScreen,
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f),
                                    CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fullscreen,
                                contentDescription = contentFullScreenText
                            )
                        }
                        IconButton(
                            onClick = {
                                cancel()
                            }, modifier = Modifier
                                .size(48.dp)
                                .background(
                                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f),
                                    CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = cancelText
                            )
                        }
                    }
                }
            }
        }

        else -> {
            isGettingLocation = true
        }
    }
}

@Composable
private fun ReusableGoogleMap(
    cameraPositionState: CameraPositionState,
    positionMap: LatLng,
    setLocation: (Location) -> Unit,
    editLocation: Boolean,
    isRounded: Boolean = false,
    uiSettings: MapUiSettings = MapUiSettings(
        tiltGesturesEnabled = false, zoomControlsEnabled = false
    )
) {
    val modifier = Modifier.fillMaxSize()
    if (isRounded) {
        modifier.clip(RoundedCornerShape(12.dp))
    }

    val markerState = remember {
        MarkerState(position = positionMap)
    }

    LaunchedEffect(positionMap) {
        markerState.position = positionMap
    }

    GoogleMap(
        modifier = modifier, cameraPositionState = cameraPositionState, onMapClick = { latLng ->
            if (editLocation) {
                setLocation(Location("").apply {
                    latitude = latLng.latitude
                    longitude = latLng.longitude
                })
            }
        }, uiSettings = uiSettings, properties = MapProperties(isMyLocationEnabled = true)
    ) {
        Marker(state = markerState, draggable = true)
    }
}

fun isLocationEnabled(context: Context): Boolean {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
        LocationManager.NETWORK_PROVIDER
    )
}

@SuppressLint("MissingPermission")
fun getLastLocation(context: Context, setLocation: (Location) -> Unit) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
        if (location != null) {
            setLocation(location)
        } else {
            requestCurrentLocation(fusedLocationClient, setLocation)
        }
    }.addOnFailureListener { _ ->
        requestCurrentLocation(fusedLocationClient, setLocation)
    }
}

@SuppressLint("MissingPermission")
private fun requestCurrentLocation(
    fusedLocationClient: FusedLocationProviderClient, setLocation: (Location) -> Unit
) {
    val locationRequest =
        LocationRequest.Builder(10000).setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setMinUpdateIntervalMillis(5000).build()

    fusedLocationClient.requestLocationUpdates(locationRequest, object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let { location ->
                setLocation(location)
                fusedLocationClient.removeLocationUpdates(this)
            }
        }
    }, Looper.getMainLooper())
}

fun checkPermissionAndLocationStatus(context: Context, callback: (Boolean, Boolean) -> Unit) {
    val hasPermission = ActivityCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    val isLocationEnabled = isLocationEnabled(context)

    callback(hasPermission, isLocationEnabled)
}

@Composable
fun PermissionRequest(
    onRequestPermission: () -> Unit,
    onCancel: () -> Unit,
    cancelText: String,
    enableLocationPermissionText: String,
    allowLocationText: String
) {
    PermissionContent(onCancel = onCancel, cancelText = cancelText) {
        Text(
            text = enableLocationPermissionText,
            modifier = Modifier.width(250.dp),
            textAlign = TextAlign.Center
        )
        Button(onClick = onRequestPermission) {
            Text(text = allowLocationText)
        }
    }
}

@Composable
fun LocationEnableRequest(
    onEnableLocation: () -> Unit,
    onCancel: () -> Unit,
    cancelText: String,
    enableLocationText: String,
    settingsButtonText: String
) {
    PermissionContent(onCancel = onCancel, cancelText) {
        Text(
            text = enableLocationText,
            modifier = Modifier.width(250.dp),
            textAlign = TextAlign.Center
        )
        Button(onClick = onEnableLocation) {
            Text(text = settingsButtonText)
        }
    }
}

@Composable
fun LoadingIndicator() {
    Box(
        Modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.Center)
    ) {
        CircularProgressIndicator()
    }
}
