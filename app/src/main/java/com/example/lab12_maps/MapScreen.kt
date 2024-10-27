package com.example.lab12_maps

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*


@Composable
fun MapScreen() {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val arequipaLocation = LatLng(-16.4040102, -71.559611) // Coordenadas de Arequipa, Perú
    val cameraPositionState = rememberCameraPositionState {
        position = com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(arequipaLocation, 12f)
    }

    // Estado para el tipo de mapa
    var mapType by remember { mutableStateOf(MapType.NORMAL) }
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                getCurrentLocation(context, fusedLocationClient) { location ->
                    currentLocation = location
                    cameraPositionState.position = com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(location, 15f)
                }
            }
        }
    )

    LaunchedEffect(Unit) {
        // para verificar el permiso de ubicación
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            getCurrentLocation(context, fusedLocationClient) { location ->
                currentLocation = location
                cameraPositionState.position = com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(location, 15f)
            }
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        Column {
            var expanded by remember { mutableStateOf(false) }
            TextButton(onClick = { expanded = true }) {
                Text(text = "Seleccionar Tipo de Mapa")
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Normal") },
                    onClick = { mapType = MapType.NORMAL; expanded = false }
                )
                DropdownMenuItem(
                    text = { Text("Híbrido") },
                    onClick = { mapType = MapType.HYBRID; expanded = false }
                )
                DropdownMenuItem(
                    text = { Text("Terreno") },
                    onClick = { mapType = MapType.TERRAIN; expanded = false }
                )
                DropdownMenuItem(
                    text = { Text("Satélite") },
                    onClick = { mapType = MapType.SATELLITE; expanded = false }
                )
            }

            // Añadir GoogleMap al layout
            GoogleMap(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    mapType = mapType,
                    isMyLocationEnabled = ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) // Aquí se configura el tipo de mapa
            ) {
                // Añadir marcador en Arequipa, Perú con un ícono personalizado
                Marker(
                    state = rememberMarkerState(position = arequipaLocation),
                    icon = bitmapFromVector(context, R.drawable.monta_icon),
                    title = "Arequipa, Perú"
                )

                // Lista de ubicaciones para marcadores adicionales
                val locations = listOf(
                    Pair(LatLng(-16.433415, -71.5442652), "JLByR"),
                    Pair(LatLng(-16.4205151, -71.4945209), "Paucarpata"),
                    Pair(LatLng(-16.3524187, -71.5675994), "Zamacola")
                )
                // Añadir marcadores para las ubicaciones
                locations.forEach { (location, title) ->
                    Marker(
                        state = rememberMarkerState(position = location),
                        title = title,
                        snippet = "Punto de interés"
                    )
                }

                currentLocation?.let { location ->
                    Marker(
                        state = rememberMarkerState(position = location),
                        title = "Ubicación Actual",
                        snippet = "Estás aquí"
                    )
                }

                // Definir polígonos
                val mallAventuraPolygon = listOf(
                    LatLng(-16.432292, -71.509145),
                    LatLng(-16.432757, -71.509626),
                    LatLng(-16.433013, -71.509310),
                    LatLng(-16.432566, -71.508853)
                )

                val parqueLambramaniPolygon = listOf(
                    LatLng(-16.422704, -71.530830),
                    LatLng(-16.422920, -71.531340),
                    LatLng(-16.423264, -71.531110),
                    LatLng(-16.423050, -71.530600)
                )

                val plazaDeArmasPolygon = listOf(
                    LatLng(-16.398866, -71.536961),
                    LatLng(-16.398744, -71.536529),
                    LatLng(-16.399178, -71.536289),
                    LatLng(-16.399299, -71.536721)
                )

                // Agregar polígonos al mapa
                Polygon(
                    points = plazaDeArmasPolygon,
                    strokeColor = Color.Red,
                    fillColor = Color.Blue.copy(alpha = 0.5f),
                    strokeWidth = 5f
                )
                Polygon(
                    points = parqueLambramaniPolygon,
                    strokeColor = Color.Green,
                    fillColor = Color.Green.copy(alpha = 0.5f),
                    strokeWidth = 5f
                )
                Polygon(
                    points = mallAventuraPolygon,
                    strokeColor = Color.Yellow,
                    fillColor = Color.Yellow.copy(alpha = 0.5f),
                    strokeWidth = 5f
                )

                // Definir polilíneas
                val touristRoute = listOf(
                    LatLng(-16.398866, -71.536961), // Plaza de Armas
                    LatLng(-16.4040102, -71.559611), // Arequipa
                    LatLng(-16.4205151, -71.4945209) // Paucarpata
                )

                Polyline(
                    points = touristRoute,
                    color = Color.Blue,
                    width = 6f
                )

                // Recorrido
                val bikeTrail = listOf(
                    LatLng(-16.430999, -71.537649), // Inicio
                    LatLng(-16.431130, -71.541150), // Intermedio
                    LatLng(-16.432120, -71.543500), // Fin
                    LatLng(-16.433000, -71.546000)  // Punto de llegada
                )

                Polyline(
                    points = bikeTrail,
                    color = Color.Red,
                    width = 8f
                )
            }

            // Manejo del movimiento de la cámara
            LaunchedEffect(Unit) {
                cameraPositionState.animate(
                    update = CameraUpdateFactory.newLatLngZoom(LatLng(-16.2520984, -71.6836503), 12f), // Mover a Yura
                    durationMs = 3000
                )
            }
        }
    }
}
// Función para obtener la ubicación actual
fun getCurrentLocation(
    context: Context,
    fusedLocationClient: FusedLocationProviderClient,
    onLocationReceived: (LatLng) -> Unit
) {
    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                val latLng = LatLng(it.latitude, it.longitude)
                onLocationReceived(latLng)
            }
        }
    }
}

// Función para el escalado del ícono del marcador
fun bitmapFromVector(context: Context, resId: Int): BitmapDescriptor {
    val bitmap = BitmapFactory.decodeResource(context.resources, resId)
    val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 150, 150, false)
    return BitmapDescriptorFactory.fromBitmap(scaledBitmap)
}
