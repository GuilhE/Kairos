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
package com.github.guilhe.kairos.data.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import org.joda.time.DateTime
import java.util.Calendar
import java.util.Locale

data class Weather(
    val date: DateTime,
    val interval: List<Interval>,
    @StringRes val overallTypeNameRes: Int,
    @DrawableRes val overallTypeImageRes: Int
) {
    private val locale = Locale.getDefault()

    val overallMin: Int = interval.sortedBy { it.degrees }.take(interval.size / 2).map { it.degrees }.average().toInt()
    val overallMax: Int = interval.sortedBy { it.degrees }.takeLast(interval.size / 2).map { it.degrees }.average().toInt()

    val dayOfMonth: Int = date.dayOfMonth
    val dayOfWeek: Int = date.dayOfWeek

    val dayOfWeekLabel: String =
        (Calendar.getInstance().apply { set(Calendar.DAY_OF_WEEK, dayOfWeek + 1) }.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, locale) ?: "")
    val monthLabel: String =
        (Calendar.getInstance().apply { set(Calendar.MONTH, date.monthOfYear - 1) }.getDisplayName(Calendar.MONTH, Calendar.LONG, locale) ?: "")
}
