package com.amazon.androidquickstartapp.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.amazon.androidquickstartapp.R
import com.amazon.androidquickstartapp.utils.Constants.ACCURACY
import com.amazon.androidquickstartapp.utils.Constants.FREQUENCY
import com.amazon.androidquickstartapp.utils.Constants.LATENCY
import com.amazon.androidquickstartapp.utils.Constants.MIN_UPDATE_INTERVAL_MILLIS
import com.amazon.androidquickstartapp.utils.Constants.WAIT_FOR_ACCURATE_LOCATION
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.location.LocationComponent
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.location.LocationComponentOptions
import org.maplibre.android.location.modes.CameraMode
import org.maplibre.android.location.modes.RenderMode
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.LineString
import org.maplibre.geojson.Point
import software.amazon.location.tracking.config.SdkConfig.MIN_DISTANCE
import software.amazon.location.tracking.util.Helper

class MapHelper {
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var locationManager: LocationManager? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private val helper = Helper()

    fun enableLocationComponent(
        it: Style,
        context: Context,
        locationComponent: LocationComponent?
    ) {
        val locationComponentOptions =
            LocationComponentOptions.builder(context)
                .pulseEnabled(true)
                .build()
        val locationComponentActivationOptions =
            buildLocationComponentActivationOptions(it, locationComponentOptions, context)
        locationComponent?.activateLocationComponent(locationComponentActivationOptions)
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        locationComponent?.isLocationComponentEnabled = true
        locationComponent?.cameraMode = CameraMode.TRACKING
        locationComponent?.renderMode = RenderMode.NORMAL

        if (helper.isGooglePlayServicesAvailable(context)) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        } else {
            locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        }
        initLocationEngine(object : LocationCallback() {})
    }

    private fun buildLocationComponentActivationOptions(
        style: Style,
        locationComponentOptions: LocationComponentOptions, context: Context
    ): LocationComponentActivationOptions {
        return LocationComponentActivationOptions
            .builder(context, style)
            .locationComponentOptions(locationComponentOptions)
            .build()
    }

    fun addMarker(
        mapboxMap: MapLibreMap,
        position: LatLng,
        image: Int,
        layerId: String,
        sourceId: String,
        imageName: String,
        context: Context
    ) {
        mapboxMap.style?.removeLayer(layerId)
        mapboxMap.style?.removeSource(sourceId)
        val geoJsonSource = GeoJsonSource(
            sourceId,
            Feature.fromGeometry(
                Point.fromLngLat(position.longitude, position.latitude)
            )
        )
        mapboxMap.style?.addSource(geoJsonSource)

        val symbolLayer = SymbolLayer(layerId, sourceId)
        symbolLayer.withProperties(
            PropertyFactory.iconImage(imageName),
            PropertyFactory.iconAllowOverlap(true),
            PropertyFactory.iconIgnorePlacement(true)
        )
        ContextCompat.getDrawable(context, image)
            ?.let { mapboxMap.style?.addImage(imageName, it) }
        mapboxMap.style?.addLayer(symbolLayer)
    }

    fun addTrackerLine(
        coordinates: List<Point>,
        mLayerId: String,
        mSourceId: String,
        context: Context,
        mapLibreMap: MapLibreMap,
        layerSize: Int
    ) {
        mapLibreMap.getStyle { style ->
            style.removeLayer(mLayerId)
            style.removeSource(mSourceId)

            val points = mutableListOf<Point>()
            coordinates.forEach { latLng ->
                points.add(Point.fromLngLat(latLng.longitude(), latLng.latitude()))
            }

            val lineString = LineString.fromLngLats(points)

            val feature = Feature.fromGeometry(lineString)
            val featureCollection = FeatureCollection.fromFeature(feature)
            style.addSource(GeoJsonSource(mSourceId, featureCollection))
            val lineLayer = LineLayer(mLayerId, mSourceId).withProperties(
                PropertyFactory.lineDasharray(arrayOf(0.01f, 2f)),
                PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
                PropertyFactory.lineWidth(5f),
                PropertyFactory.lineColor(ContextCompat.getColor(context, R.color.black))
            )
            if (layerSize <= 0) {
                style.addLayer(lineLayer)
            } else {
                style.addLayerAt(lineLayer, layerSize)
            }
        }
    }

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun initLocationEngine(locationCallback: LocationCallback) {
        if (fusedLocationClient != null) {
            initLocationEngineWithFusedLocationClient(locationCallback)
        } else {
            initLocationEngineWithLocationManager(locationCallback)
        }
    }

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun initLocationEngineWithFusedLocationClient(locationCallback: LocationCallback) {
        coroutineScope.launch {
            fusedLocationClient?.locationAvailability?.addOnSuccessListener {
                if (!it.isLocationAvailable) {
                    return@addOnSuccessListener
                }

                fusedLocationClient?.requestLocationUpdates(
                    LocationRequest.Builder(ACCURACY, FREQUENCY)
                        .setWaitForAccurateLocation(WAIT_FOR_ACCURATE_LOCATION)
                        .setMinUpdateIntervalMillis(MIN_UPDATE_INTERVAL_MILLIS)
                        .setMaxUpdateDelayMillis(LATENCY)
                        .build(),
                    locationCallback,
                    Looper.getMainLooper(),
                )
            }
        }
    }

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun initLocationEngineWithLocationManager(locationCallback: LocationCallback) {
        coroutineScope.launch {
            val locationListener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    val locationResult = LocationResult.create(arrayListOf(location))
                    locationCallback.onLocationResult(locationResult)
                }

                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
            }
            locationManager?.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                FREQUENCY,
                MIN_DISTANCE,
                locationListener,
                Looper.getMainLooper()
            )
        }
    }
}
