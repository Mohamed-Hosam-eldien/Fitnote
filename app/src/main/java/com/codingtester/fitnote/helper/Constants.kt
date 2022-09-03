package com.codingtester.fitnote.helper

import com.codingtester.fitnote.R

object Constants {

    const val DATABASE_NAME = "RunningDb"

    const val REQUEST_LOCATION_PERMISSION_CODE = 1001

    const val ACTION_START_OR_RESUME_RUNNING = "START_OR_RESUME_RUNNING"
    const val ACTION_PAUSE_RUNNING = "PAUSE_RUNNING"
    const val ACTION_STOP_RUNNING = "STOP_RUNNING"
    const val ACTION_SHOW_TRACKING_FRAGMENT_FROM_NOTIFICATION = "SHOW_TRACKING_FRAGMENT"

    const val LOCATION_UPDATE_INTERVAL = 5000L
    const val FASTEST_LOCATION_INTERVAL = 2000L

    const val TIMER_UPDATE_INTERVAL = 50L

    const val POLYLINE_COLOR = R.color.teal_200
    const val POLYLINE_WIDTH = 14f
    const val CAMERA_ZOOM = 16f

    const val NOTIFICATION_CHANNEL_ID = "Fitnote"
    const val NOTIFICATION_CHANNEL_NAME = "Tracking Channel"
    const val NOTIFICATION_ID = 11

}