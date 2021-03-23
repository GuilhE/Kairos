/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.guilhe.kairos.presentation.ui.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.IntentSender
import android.location.Geocoder
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.fondesa.kpermissions.allGranted
import com.fondesa.kpermissions.coroutines.sendSuspend
import com.fondesa.kpermissions.extension.permissionsBuilder
import com.github.guilhe.kairos.data.model.LocationMetadata
import com.github.guilhe.kairos.presentation.theme.KairosTheme
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import dagger.hilt.android.AndroidEntryPoint
import dev.chrisbanes.accompanist.insets.ProvideWindowInsets

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: DetailViewModel by viewModels()
    private lateinit var locationCancelToken: CancellationTokenSource
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    @OptIn(ExperimentalAnimationApi::class)
    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.lastLocation.addOnCompleteListener { last ->
            if (last.isSuccessful) {
                if (last.result != null) {
                    with(Geocoder(this@MainActivity).getFromLocation(last.result.latitude, last.result.longitude, 1)[0]) {
                        viewModel.updateLocation(LocationMetadata(countryName, locality, postalCode))
                    }
                } else {
                    askForLocationRefresh()
                }
            }
        }

        setContent {
            KairosTheme {
                ProvideWindowInsets {
                    Main(
                        viewModel,
                        askForLocation = {
                            askForLocationSettings()
                        }
                    )
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun askForLocationRefresh() {
        lifecycleScope.launchWhenResumed {
            val result = permissionsBuilder(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
                .build()
                .sendSuspend()

            if (result.allGranted()) {
                locationCancelToken = CancellationTokenSource()
                fusedLocationClient.getCurrentLocation(PRIORITY_HIGH_ACCURACY, locationCancelToken.token).addOnCompleteListener { current ->
                    if (current.isSuccessful && current.result != null) {
                        with(
                            Geocoder(this@MainActivity).getFromLocation(
                                current.result.latitude, current.result.longitude, 1
                            )[0]
                        ) { viewModel.updateLocation(LocationMetadata(countryName, locality, postalCode)) }
                    }
                }
            }
        }
    }

    private fun askForLocationSettings() {
        val task: Task<LocationSettingsResponse> =
            LocationServices.getSettingsClient(this)
                .checkLocationSettings(
                    LocationSettingsRequest.Builder().addLocationRequest(
                        LocationRequest.create().apply {
                            interval = 10000
                            fastestInterval = 5000
                            priority = PRIORITY_HIGH_ACCURACY
                        }
                    ).build()
                )
        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    exception.startResolutionForResult(this@MainActivity, 123)
                } catch (ignored: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
        task.addOnSuccessListener { askForLocationRefresh() }
    }

    override fun onDestroy() {
        if (::locationCancelToken.isInitialized) {
            locationCancelToken.cancel()
        }
        super.onDestroy()
    }
}
