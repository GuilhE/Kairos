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

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.guilhe.kairos.data.manager.WeatherManager
import com.github.guilhe.kairos.data.model.LocationMetadata
import com.github.guilhe.kairos.data.model.Weather
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(manager: WeatherManager) : ViewModel() {

    private val _locationData: MutableLiveData<LocationMetadata?> = MutableLiveData(null)
    private val _weatherList: MutableLiveData<List<Weather>> = MutableLiveData(manager.forecastData())

    val locationData: LiveData<LocationMetadata?> = _locationData
    val weatherList: LiveData<List<Weather>> = _weatherList

    fun updateLocation(location: LocationMetadata) {
        _locationData.value = location
    }
}
