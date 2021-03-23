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
package com.github.guilhe.kairos.presentation.ui.detail

import android.annotation.SuppressLint
import androidx.compose.animation.Animatable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.github.guilhe.kairos.R
import com.github.guilhe.kairos.com.github.guilhe.kairos.utils.Utils.normalize
import com.github.guilhe.kairos.data.model.ColorSpectrum
import com.github.guilhe.kairos.data.model.LocationMetadata
import com.github.guilhe.kairos.data.model.Weather
import com.github.guilhe.kairos.presentation.ui.detail.model.MainHudAnimProps
import com.github.guilhe.kairos.presentation.ui.detail.model.TopHudAnimProps
import com.github.guilhe.kairos.presentation.ui.main.DetailViewModel
import dev.chrisbanes.accompanist.insets.systemBarsPadding
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import kotlin.math.round

private const val ZERO_HOURS = 0f
private const val ONE_HOUR = 60f
private const val HALF_DAY = 12f
private const val FULL_DAY = HALF_DAY * 2
private const val RESET_DEFAULT_DELAY = 500L
private const val SYSTEM_BAR_PADDING = 8f

@ExperimentalAnimationApi
@Composable
fun WeatherDetailScreen(modifier: Modifier = Modifier, viewModel: DetailViewModel, locationCallback: () -> Unit) {
    val weatherList by viewModel.weatherList.observeAsState(emptyList())
    val locationData by viewModel.locationData.observeAsState()

    if (weatherList.size >= 4 && weatherList.none { it.interval.isNullOrEmpty() }) {
        Dashboard(modifier, weatherList.take(4), locationData, locationCallback)
    } else {
        DataError()
    }
}

@Composable
private fun DataError(modifier: Modifier = Modifier) {
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colors.surface) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_light_snow),
                contentDescription = null, // decorative
                modifier = Modifier.size(100.dp),
                colorFilter = ColorFilter.tint(MaterialTheme.colors.primary)
            )
            Text(
                text = stringResource(R.string.error_data),
                color = MaterialTheme.colors.primary,
                fontSize = 26.sp,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.body1,
                modifier = Modifier.padding(20.dp),
            )
        }
    }
}

@SuppressLint("DefaultLocale")
@ExperimentalAnimationApi
@Composable
private fun Dashboard(modifier: Modifier = Modifier, list: List<Weather>, location: LocationMetadata? = null, locationCallback: () -> Unit) {
    val scope = rememberCoroutineScope()
    var next24hours by remember { mutableStateOf(build24HoursWeather(list.take(2))) }
    var dateTime by remember { mutableStateOf(DateTime()) }
    val tomorrowLabel = dayLabel(list[1])

    val topHudHeight = 120.dp
    val topHudInitY = -topHudHeight - SYSTEM_BAR_PADDING.dp // due to systemBarsPadding()
    var topHudY by remember { mutableStateOf(topHudInitY) }
    var topHudAnimProps by remember { mutableStateOf(TopHudAnimProps(topHudInitY)) }
    var topHudAnimJob: Job? = remember { null }

    val mainHudAlpha by animateFloatAsState(if (topHudY <= topHudInitY) 1f else 0f, tween(1000))
    var deltaDragValue by remember { mutableStateOf(0f) }
    var hudAnimProps by remember {
        with(next24hours) {
            mutableStateOf(
                MainHudAnimProps(
                    dayLabel = dayLabel(this),
                    degrees = interval[0].degrees,
                    typeNameRes = interval[0].typeNameRes
                ).also {
                    it.resetTimeToNow()
                }
            )
        }
    }
    var accessibilityJob: Job? = remember { null }

    val timeColor = remember { Animatable(next24hours.interval[0].colorSpectrum.sky) }
    val weatherColor = remember { Animatable(next24hours.interval[0].colorSpectrum.clouds) }
    val tempColor = remember { Animatable(next24hours.interval[0].colorSpectrum.temperature) }

    fun resetTopHud(delayIt: Boolean = true) {
        topHudAnimJob?.cancel()
        topHudAnimJob = scope.launch {
            if (delayIt) {
                delay(if (topHudY >= (-10).dp) 5000 else RESET_DEFAULT_DELAY)
            }
            topHudY = topHudInitY
            topHudAnimProps = TopHudAnimProps(topHudY, true)
        }
    }

    fun updateBackground(colorSpectrum: ColorSpectrum, delayIt: Boolean = false) {
        scope.launch {
            if (delayIt) {
                delay(RESET_DEFAULT_DELAY)
            }
            with(colorSpectrum) {
                timeColor.animateTo(sky)
                weatherColor.animateTo(clouds)
                tempColor.animateTo(temperature)
            }
        }
    }

    fun updateNext24Hours() {
        next24hours = build24HoursWeather(list.take(2))
    }

    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(timeColor.value, weatherColor.value, tempColor.value)))
        ) {
            MainHud(
                Modifier
                    .fillMaxSize()
                    .alpha(mainHudAlpha),
                location, hudAnimProps, locationCallback
            )
            Column(
                Modifier
                    .systemBarsPadding()
                    // so that touches can be catch when clicking for location refresh
                    .padding(bottom = if (location == null) 50.dp else 0.dp)
            ) {
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .pointerInput(Unit) {
                            detectVerticalDragGestures(
                                onDragStart = { topHudAnimJob?.cancel() },
                                onDragEnd = { resetTopHud() },
                                onVerticalDrag = { change, dragAmount ->
                                    change.consumeAllChanges()
                                    val amount: Dp = topHudY + (dragAmount.toDp() * 0.5f)
                                    if (amount in topHudInitY..0.dp) {
                                        topHudY = amount
                                        topHudAnimProps = TopHudAnimProps(topHudY)
                                    }
                                }
                            )
                        }
                ) {
                    TopHud(list, topHudHeight, topHudAnimProps)
                }
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .pointerInput(Unit) {
                            detectVerticalDragGestures(
                                onDragStart = {
                                    resetTopHud(false)
                                    deltaDragValue = 0f
                                },
                                onDragEnd = {
                                    accessibilityJob?.cancel()
                                    dateTime = DateTime()
                                    updateNext24Hours()
                                    with(next24hours.interval[0]) {
                                        updateBackground(colorSpectrum, true)
                                        hudAnimProps = MainHudAnimProps(
                                            dayLabel = dayLabel(next24hours),
                                            degrees = degrees,
                                            typeNameRes = typeNameRes,
                                            animate = true,
                                            isAccessibilityAvailable = true
                                        ).also { it.resetTimeToNow() }
                                    }
                                },
                                onVerticalDrag = { change, dragAmount ->
                                    change.consumeAllChanges()
                                    deltaDragValue += -dragAmount * 0.6f
                                    accessibilityJob?.cancel()

                                    // Clock logic
                                    val screenHours = normalize(deltaDragValue, 0f, size.height.toFloat(), ZERO_HOURS, FULL_DAY)
                                    val screenMinutes = normalize(screenHours - screenHours.toInt(), 0f, 1f, ZERO_HOURS, ONE_HOUR)
                                    val future = dateTime
                                        .plusHours(screenHours.toInt())
                                        .plusMinutes(screenMinutes.toInt())

                                    // Gradient logic + Hud data
                                    var dayLabel = hudAnimProps.dayLabel
                                    var degrees = hudAnimProps.degrees
                                    var typeNameRes = hudAnimProps.typeNameRes
                                    val index = normalize(round(screenHours), ZERO_HOURS, FULL_DAY, 0f, next24hours.interval.size.toFloat()).toInt()
                                    if (index < next24hours.interval.size) {
                                        with(next24hours.interval[index]) {
                                            updateBackground(colorSpectrum)
                                            degrees = this.degrees
                                            typeNameRes = this.typeNameRes
                                            dayLabel = if (future
                                                .dayOfWeek()
                                                .get() > dateTime
                                                    .dayOfWeek()
                                                    .get()
                                            ) {
                                                tomorrowLabel
                                            } else {
                                                dayLabel(next24hours)
                                            }
                                        }
                                    }
                                    hudAnimProps = MainHudAnimProps(
                                        hours = future.hourOfDay,
                                        minutes = future.minuteOfHour,
                                        dayLabel = dayLabel,
                                        degrees = degrees,
                                        typeNameRes = typeNameRes,
                                        isAccessibilityAvailable = false
                                    )

                                    accessibilityJob = scope.launch {
                                        delay(2000)
                                        hudAnimProps = hudAnimProps.copy(isAccessibilityAvailable = true)
                                    }
                                }
                            )
                        }
                )
            }
            if (isSystemInDarkTheme()) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                ) {}
            }
        }
    }
}

@Composable
private fun TopHud(items: List<Weather>, topBarHeight: Dp, props: TopHudAnimProps) {
    val anim: Dp by animateDpAsState(
        targetValue = props.translationY,
        animationSpec = tween(durationMillis = 500, easing = CubicBezierEasing(0.36f, 0f, 0.66f, -0.56f))
    )
    NextThreeDays(
        items = items.takeLast(3),
        modifier = Modifier
            .height(topBarHeight)
            .fillMaxWidth()
            .offset(y = if (props.animate) anim else props.translationY)
    )
}

@SuppressLint("DefaultLocale")
@Composable
private fun NextThreeDays(modifier: Modifier = Modifier, items: List<Weather>, color: Color = MaterialTheme.colors.primary) {
    Row(modifier, Arrangement.Center) {
        items.forEach {
            val accessibility = it.dayOfWeekLabel +
                stringResource(it.overallTypeNameRes) +
                stringResource(R.string.max) + "${it.overallMax}" +
                stringResource(R.string.min) + "${it.overallMin}" +
                stringResource(R.string.celsius)
            Column(
                Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .clearAndSetSemantics { contentDescription = accessibility },
                verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = it.dayOfWeekLabel.take(3).toUpperCase(),
                    color = color,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier.fillMaxWidth()
                )
                Image(
                    painter = painterResource(id = it.overallTypeImageRes),
                    contentDescription = null, // decorative
                    colorFilter = ColorFilter.tint(color),
                    modifier = Modifier.padding(5.dp)
                )
                Text(
                    buildAnnotatedString {
                        withStyle(SpanStyle(fontSize = 20.sp)) {
                            append("${it.overallMax}/")
                        }
                        withStyle(SpanStyle(fontSize = 16.sp, baselineShift = BaselineShift(.12f))) {
                            append("${it.overallMin}")
                        }
                    },
                    color = color,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@ExperimentalAnimationApi
@SuppressLint("DefaultLocale")
@Composable
private fun MainHud(
    modifier: Modifier = Modifier,
    locationData: LocationMetadata? = null,
    animProps: MainHudAnimProps,
    locationCallback: () -> Unit
) {
    val accessibility = animProps.accessibility() + stringResource(R.string.celsius) + stringResource(animProps.typeNameRes)
    ConstraintLayout(modifier.padding(top = 50.dp, bottom = 20.dp, start = 25.dp, end = 25.dp)) {
        val (clock, info, location) = createRefs()
        Next24HoursData(
            Modifier
                .constrainAs(info) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                }
                .clearAndSetSemantics { contentDescription = if (animProps.isAccessibilityAvailable) accessibility else "" },
            animProps
        )
        Clock(
            Modifier
                .size(70.dp)
                .constrainAs(clock) {
                    top.linkTo(parent.top)
                    end.linkTo(parent.end)
                },
            animProps
        )
        Location(
            Modifier
                .padding(10.dp)
                .constrainAs(location) {
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
            locationData,
            locationCallback = locationCallback
        )
    }
}

@SuppressLint("DefaultLocale")
@Composable
private fun Next24HoursData(modifier: Modifier = Modifier, animProps: MainHudAnimProps, color: Color = MaterialTheme.colors.primary) {
    val degreesA by animateIntAsState(animProps.degrees, tween(500))
    Column(modifier.clearAndSetSemantics { }) {
        Text(
            text = "${animProps.dayLabel.take(3)},${animProps.dayLabel.split(",")[1]}".toUpperCase(),
            color = color,
            fontSize = 20.sp,
            style = MaterialTheme.typography.body2
        )
        Text(
            text = "${animProps.hours}".padStart(2, '0') + ":" + "${animProps.minutes}".padStart(2, '0'),
            color = color,
            fontSize = 20.sp,
            style = MaterialTheme.typography.body2
        )
        Text(
            buildAnnotatedString {
                withStyle(SpanStyle(fontSize = 80.sp)) {
                    append(degreesA.toString())
                }
                withStyle(SpanStyle(fontSize = 30.sp, baselineShift = BaselineShift(3f))) {
                    append("º")
                }
            },
            color = color,
            style = MaterialTheme.typography.body1
        )
        Text(
            text = stringResource(animProps.typeNameRes).toUpperCase(),
            color = color,
            fontSize = 12.sp,
            style = MaterialTheme.typography.body2,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

@Composable
private fun Clock(modifier: Modifier = Modifier, animProps: MainHudAnimProps, color: Color = MaterialTheme.colors.primary) {
    val h = animProps.hours.toFloat()
    val m = animProps.minutes.toFloat()
    val hour = normalize(h, if (h < HALF_DAY) ZERO_HOURS else HALF_DAY, if (h < HALF_DAY) HALF_DAY else FULL_DAY, 0f, 360f)
    val minutes = normalize(m, 0f, 60f, 0f, 360f)
    val hourA by animateFloatAsState(hour)
    val minutesA by animateFloatAsState(minutes)
    val stroke = 4.dp

    Canvas(modifier = modifier) {
        drawCircle(
            color = color,
            center = size.center,
            radius = size.minDimension / 2,
            style = Stroke(stroke.toPx()),
        )
        withTransform(
            transformBlock = { rotate(if (animProps.animate) hourA else hour, size.center) },
            drawBlock = {
                drawLine(
                    color = color,
                    strokeWidth = stroke.toPx(),
                    cap = StrokeCap.Round,
                    start = size.center,
                    end = Offset(size.minDimension / 2, 20.dp.toPx())
                )
            }
        )
        withTransform(
            transformBlock = { rotate(if (animProps.animate) minutesA else minutes, size.center) },
            drawBlock = {
                drawLine(
                    color = color,
                    strokeWidth = stroke.toPx(),
                    cap = StrokeCap.Round,
                    start = size.center,
                    end = Offset(size.minDimension / 2, 10.dp.toPx())
                )
            }
        )
    }
}

@ExperimentalAnimationApi
@SuppressLint("DefaultLocale")
@Composable
private fun Location(
    modifier: Modifier = Modifier,
    locationData: LocationMetadata?,
    color: Color = MaterialTheme.colors.primary,
    locationCallback: () -> Unit
) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            imageVector = if (locationData != null) Icons.Filled.LocationOn else Icons.Filled.LocationOff,
            contentDescription = if (locationData == null) stringResource(R.string.btn_location) else null,
            colorFilter = ColorFilter.tint(color),
            modifier = Modifier
                .size(25.dp)
                .clip(CircleShape)
                .clickable(enabled = locationData == null) { locationCallback.invoke() }
        )
        AnimatedVisibility(visible = locationData != null) {
            if (locationData != null) {
                Text(
                    buildAnnotatedString {
                        withStyle(SpanStyle(fontSize = 20.sp)) {
                            append(locationData.city.toUpperCase())
                        }
                        withStyle(SpanStyle(fontSize = 12.sp)) {
                            append("\n${locationData.country.toUpperCase()}")
                        }
                    },
                    modifier = modifier.fillMaxWidth(),
                    color = color,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.body2
                )
            }
        }
    }
}

private fun build24HoursWeather(list: List<Weather>): Weather {
    val hour = DateTime.now().hourOfDay
    val index = list[0].interval.indexOfFirst { it.toHour > hour }
    val firstHalf = list[0].interval.subList(index, list[0].interval.size).toMutableList()
    val lastHalf = list[1].interval.subList(0, index)
    return list[0].copy(interval = firstHalf.apply { addAll(lastHalf) })
}

@SuppressLint("DefaultLocale")
private fun dayLabel(next24Hours: Weather): String = "${next24Hours.dayOfWeekLabel}, ${next24Hours.dayOfMonth}"
