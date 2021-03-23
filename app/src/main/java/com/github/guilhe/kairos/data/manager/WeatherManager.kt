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
package com.github.guilhe.kairos.data.manager

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import com.github.guilhe.kairos.R
import com.github.guilhe.kairos.com.github.guilhe.kairos.utils.Utils.normalize
import com.github.guilhe.kairos.data.model.ColorSpectrum
import com.github.guilhe.kairos.data.model.Interval
import com.github.guilhe.kairos.data.model.Weather
import com.github.guilhe.kairos.presentation.theme.cloud_0
import com.github.guilhe.kairos.presentation.theme.cloud_100
import com.github.guilhe.kairos.presentation.theme.cloud_25
import com.github.guilhe.kairos.presentation.theme.cloud_50
import com.github.guilhe.kairos.presentation.theme.cloud_75
import com.github.guilhe.kairos.presentation.theme.sky_0
import com.github.guilhe.kairos.presentation.theme.sky_100
import com.github.guilhe.kairos.presentation.theme.sky_25
import com.github.guilhe.kairos.presentation.theme.sky_50
import com.github.guilhe.kairos.presentation.theme.sky_75
import com.github.guilhe.kairos.presentation.theme.temp_10
import com.github.guilhe.kairos.presentation.theme.temp_20
import com.github.guilhe.kairos.presentation.theme.temp_30
import com.github.guilhe.kairos.presentation.theme.temp_40
import org.joda.time.DateTime

@Suppress("unused")
class WeatherManager {

    fun forecastData(): List<Weather> = mockGoodForecast()

    private fun mockGoodForecast(): List<Weather> {
        val dataA = mutableListOf<Interval>()
        for (i in 0..5) {
            dataA.add(
                Interval(
                    i, i + 1, 18,
                    ColorSpectrum(
                        mapColor(temp_10, temp_20, 10, 20, 18),
                        cloud_0,
                        mapColor(sky_100, sky_75, 0, 5, i),
                    ),
                    R.string.clear
                )
            )
        }
        for (i in 6..11) {
            dataA.add(
                Interval(
                    i, i + 1, 26,
                    ColorSpectrum(
                        mapColor(temp_20, temp_30, 20, 30, 26),
                        cloud_0,
                        mapColor(sky_50, sky_25, 6, 11, i),
                    ),
                    R.string.clear
                )
            )
        }
        for (i in 12..17) {
            dataA.add(
                Interval(
                    i, i + 1, 30,
                    ColorSpectrum(
                        mapColor(temp_30, temp_40, 30, 40, 30),
                        cloud_0,
                        mapColor(sky_0, sky_50, 12, 17, i),
                    ),
                    R.string.clear
                )
            )
        }
        for (i in 18..23) {
            dataA.add(
                Interval(
                    i, i + 1, 28,
                    ColorSpectrum(
                        mapColor(temp_20, temp_30, 20, 30, 28),
                        cloud_0,
                        mapColor(sky_75, sky_100, 18, 23, i),
                    ),
                    R.string.clear
                )
            )
        }

        val dataB = mutableListOf<Interval>()
        for (i in 0..5) {
            dataB.add(
                Interval(
                    i, i + 1, 25,
                    ColorSpectrum(
                        mapColor(temp_20, temp_30, 20, 30, 25),
                        cloud_0,
                        mapColor(sky_100, sky_75, 0, 5, i),
                    ),
                    R.string.clear
                )
            )
        }
        for (i in 6..11) {
            dataB.add(
                Interval(
                    i, i + 1, 30,
                    ColorSpectrum(
                        mapColor(temp_20, temp_30, 20, 30, 30),
                        cloud_0,
                        mapColor(sky_50, sky_25, 6, 11, i),
                    ),
                    R.string.clear
                )
            )
        }
        for (i in 12..17) {
            dataB.add(
                Interval(
                    i, i + 1, 36,
                    ColorSpectrum(
                        mapColor(temp_30, temp_40, 30, 40, 36),
                        cloud_0,
                        mapColor(sky_0, sky_50, 12, 17, i),
                    ),
                    R.string.clear
                )
            )
        }
        for (i in 18..23) {
            dataB.add(
                Interval(
                    i, i + 1, 28,
                    ColorSpectrum(
                        mapColor(temp_20, temp_30, 20, 30, 28),
                        cloud_25,
                        mapColor(sky_75, sky_100, 18, 23, i),
                    ),
                    R.string.partly_cloud
                )
            )
        }

        val dataC = mutableListOf<Interval>()
        for (i in 0..5) {
            dataC.add(
                Interval(
                    i, i + 1, 12,
                    ColorSpectrum(
                        mapColor(temp_10, temp_20, 10, 20, 12),
                        cloud_100,
                        mapColor(sky_100, sky_75, 0, 5, i),
                    ),
                    R.string.overcast
                )
            )
        }
        for (i in 6..11) {
            dataC.add(
                Interval(
                    i, i + 1, 16,
                    ColorSpectrum(
                        mapColor(temp_10, temp_20, 10, 20, 16),
                        cloud_50,
                        mapColor(sky_50, sky_25, 6, 11, i),
                    ),
                    R.string.partly_cloud
                )
            )
        }
        for (i in 12..17) {
            dataC.add(
                Interval(
                    i, i + 1, 21,
                    ColorSpectrum(
                        mapColor(temp_20, temp_30, 20, 30, 21),
                        cloud_25,
                        mapColor(sky_0, sky_50, 12, 17, i),
                    ),
                    R.string.light_rain
                )
            )
        }
        for (i in 18..23) {
            dataC.add(
                Interval(
                    i, i + 1, 19,
                    ColorSpectrum(
                        mapColor(temp_10, temp_20, 10, 20, 19),
                        cloud_25,
                        mapColor(sky_75, sky_100, 18, 23, i),
                    ),
                    R.string.light_rain
                )
            )
        }

        return with(DateTime.now()) {
            listOf(
                Weather(this, dataA, R.string.clear, R.drawable.ic_sunny),
                Weather(this.plusDays(1), dataB, R.string.clear, R.drawable.ic_sunny),
                Weather(this.plusDays(2), dataB, R.string.partly_cloud, R.drawable.ic_partly_cloud),
                Weather(this.plusDays(3), dataC, R.string.light_rain, R.drawable.ic_moderate_rain)
            )
        }
    }

    private fun mockBadForecast(): List<Weather> {
        val dataA = mutableListOf<Interval>()
        for (i in 0..5) {
            dataA.add(
                Interval(
                    i, i + 1, 18,
                    ColorSpectrum(
                        mapColor(temp_10, temp_20, 10, 20, 18),
                        cloud_25,
                        mapColor(sky_100, sky_75, 0, 5, i),
                    ),
                    R.string.partly_cloud
                )
            )
        }
        for (i in 6..11) {
            dataA.add(
                Interval(
                    i, i + 1, 21,
                    ColorSpectrum(
                        mapColor(temp_20, temp_30, 20, 30, 21),
                        cloud_0,
                        mapColor(sky_50, sky_25, 6, 11, i),
                    ),
                    R.string.clear
                )
            )
        }
        for (i in 12..17) {
            dataA.add(
                Interval(
                    i, i + 1, 23,
                    ColorSpectrum(
                        mapColor(temp_20, temp_30, 20, 30, 23),
                        cloud_75,
                        mapColor(sky_0, sky_50, 12, 17, i),
                    ),
                    R.string.foggy
                )
            )
        }
        for (i in 18..23) {
            dataA.add(
                Interval(
                    i, i + 1, 20,
                    ColorSpectrum(
                        mapColor(temp_10, temp_20, 10, 20, 20),
                        cloud_100,
                        mapColor(sky_75, sky_100, 18, 23, i),
                    ),
                    R.string.overcast
                )
            )
        }

        val dataB = mutableListOf<Interval>()
        for (i in 0..5) {
            dataB.add(
                Interval(
                    i, i + 1, 15,
                    ColorSpectrum(
                        mapColor(temp_10, temp_20, 10, 20, 15),
                        cloud_0,
                        mapColor(sky_100, sky_75, 0, 5, i),
                    ),
                    R.string.light_rain
                )
            )
        }
        for (i in 6..11) {
            dataB.add(
                Interval(
                    i, i + 1, 16,
                    ColorSpectrum(
                        mapColor(temp_10, temp_20, 10, 20, 16),
                        cloud_0,
                        mapColor(sky_50, sky_25, 6, 11, i),
                    ),
                    R.string.light_rain
                )
            )
        }
        for (i in 12..17) {
            dataB.add(
                Interval(
                    i, i + 1, 20,
                    ColorSpectrum(
                        mapColor(temp_10, temp_20, 10, 20, 20),
                        cloud_25,
                        mapColor(sky_0, sky_50, 12, 17, i),
                    ),
                    R.string.moderate_rain
                )
            )
        }
        for (i in 18..23) {
            dataB.add(
                Interval(
                    i, i + 1, 19,
                    ColorSpectrum(
                        mapColor(temp_10, temp_20, 10, 20, 19),
                        cloud_25,
                        mapColor(sky_75, sky_100, 18, 23, i),
                    ),
                    R.string.heavy_rain
                )
            )
        }

        val dataC = mutableListOf<Interval>()
        for (i in 0..5) {
            dataC.add(
                Interval(
                    i, i + 1, 12,
                    ColorSpectrum(
                        mapColor(temp_10, temp_20, 10, 20, 12),
                        cloud_100,
                        mapColor(sky_100, sky_75, 0, 5, i),
                    ),
                    R.string.overcast
                )
            )
        }
        for (i in 6..11) {
            dataC.add(
                Interval(
                    i, i + 1, 16,
                    ColorSpectrum(
                        mapColor(temp_10, temp_20, 10, 20, 16),
                        cloud_50,
                        mapColor(sky_50, sky_25, 6, 11, i),
                    ),
                    R.string.partly_cloud
                )
            )
        }
        for (i in 12..17) {
            dataC.add(
                Interval(
                    i, i + 1, 21,
                    ColorSpectrum(
                        mapColor(temp_20, temp_30, 20, 30, 21),
                        cloud_75,
                        mapColor(sky_0, sky_50, 12, 17, i),
                    ),
                    R.string.foggy
                )
            )
        }
        for (i in 18..23) {
            dataC.add(
                Interval(
                    i, i + 1, 19,
                    ColorSpectrum(
                        mapColor(temp_10, temp_20, 10, 20, 19),
                        cloud_100,
                        mapColor(sky_75, sky_100, 18, 23, i),
                    ),
                    R.string.overcast
                )
            )
        }

        return with(DateTime.now()) {
            listOf(
                Weather(this, dataB, R.string.moderate_rain, R.drawable.ic_moderate_rain),
                Weather(this.plusDays(1), dataC, R.string.foggy, R.drawable.ic_foggy),
                Weather(this.plusDays(2), dataA, R.string.partly_cloud, R.drawable.ic_partly_cloud),
                Weather(this.plusDays(3), dataA, R.string.partly_cloud, R.drawable.ic_partly_cloud)
            )
        }
    }

    private fun mapColor(minColor: Color, maxColor: Color, minValue: Int, maxValue: Int, value: Int): Color {
        return lerp(minColor, maxColor, normalize(value.toFloat(), minValue.toFloat(), maxValue.toFloat(), 0f, 1f))
    }
}
