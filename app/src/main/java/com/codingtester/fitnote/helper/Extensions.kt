package com.codingtester.fitnote.helper

import kotlin.math.round

fun Float.formatDistanceInKilometer(): String {
    return String.format("%.1f", this/1000)
}

fun Float.calculateAvgSpeed(timeInMillis:Long): String {
    return (round((this / 1000f) / (timeInMillis / 1000f / 60 / 60) * 10) / 10f).toString()
}