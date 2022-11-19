package com.codingtester.fitnote.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import com.codingtester.fitnote.R
import com.codingtester.fitnote.helper.Constants
import com.codingtester.fitnote.helper.Constants.ACTION_PAUSE_RUNNING
import com.codingtester.fitnote.helper.Constants.ACTION_START_OR_RESUME_RUNNING
import com.codingtester.fitnote.helper.Constants.ACTION_STOP_RUNNING
import com.codingtester.fitnote.helper.Constants.FASTEST_LOCATION_INTERVAL
import com.codingtester.fitnote.helper.Constants.LOCATION_UPDATE_INTERVAL
import com.codingtester.fitnote.helper.Constants.NOTIFICATION_CHANNEL_ID
import com.codingtester.fitnote.helper.Constants.NOTIFICATION_CHANNEL_NAME
import com.codingtester.fitnote.helper.Constants.NOTIFICATION_ID
import com.codingtester.fitnote.helper.TrackingStateEnum
import com.codingtester.fitnote.helper.TrackingUtility
import com.google.android.gms.location.*
import com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

typealias Polyline = MutableList<LatLng>
typealias Polylines = MutableList<Polyline>

@AndroidEntryPoint
class TrackingService : LifecycleService() {

    private var isFirstRunning = true
    private var isServiceDestroyed = false

    @set:Inject
    internal lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    @set:Inject
    internal lateinit var baseNotificationBuilder: NotificationCompat.Builder

    @set:Inject
    internal lateinit var notificationManager: NotificationManager

    private lateinit var currentNotificationBuilder: NotificationCompat.Builder

    private val timeRunInSeconds = MutableLiveData<Long>()

    companion object {
        val timeRunInMillis = MutableLiveData<Long>()
        val trackingState = MutableLiveData<TrackingStateEnum>()
        val pathPoints = MutableLiveData<Polylines>()
    }

    private fun setInitialValue() {
        trackingState.postValue(TrackingStateEnum.HOLD)
        pathPoints.postValue(mutableListOf())
        timeRunInMillis.postValue(0L)
        timeRunInSeconds.postValue(0L)
    }

    override fun onCreate() {
        super.onCreate()
        setInitialValue()
        currentNotificationBuilder = baseNotificationBuilder
        trackingState.observe(this) {
            updateLocationTracking(it)
            updateNotificationTrackingState(it)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_START_OR_RESUME_RUNNING -> {
                    if (isFirstRunning) {
                        startRunningForegroundService()
                        isFirstRunning = false
                    } else {
                        startTimer()
                    }
                }
                ACTION_PAUSE_RUNNING -> {
                    pauseTracking()
                }
                ACTION_STOP_RUNNING -> {
                    destroyService()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun destroyService() {
        isFirstRunning = false
        isServiceDestroyed = true
        isTimerEnabled = false
        trackingState.postValue(TrackingStateEnum.CANCEL)
        setInitialValue()
        stopForeground(true)
        stopSelf()
    }

    // timer initialize
    private var isTimerEnabled = false
    private var lapTime = 0L
    private var timeRun = 0L
    private var timeStarted = 0L
    private var lastSecondTimeStamp = 0L

    private fun startTimer() {
        addEmptyPolyline()

        trackingState.postValue(TrackingStateEnum.RUNNING)
        timeStarted = System.currentTimeMillis()
        isTimerEnabled = true

        CoroutineScope(Dispatchers.Main).launch {
            while (trackingState.value == TrackingStateEnum.RUNNING) {
                // difference between now and time started
                lapTime = System.currentTimeMillis() - timeStarted

                timeRunInMillis.postValue(timeRun + lapTime)
                if (timeRunInMillis.value!! >= lastSecondTimeStamp + 1000L) {
                    timeRunInSeconds.postValue(timeRunInSeconds.value!! + 1)
                    lastSecondTimeStamp += 1000L
                }
                delay(Constants.TIMER_UPDATE_INTERVAL)
            }
            timeRun += lapTime
        }
    }

    private fun pauseTracking() {
        trackingState.postValue(TrackingStateEnum.PAUSE)
        isTimerEnabled = false
    }

    @SuppressLint("MissingPermission")
    private fun updateLocationTracking(trackingState: TrackingStateEnum) {
        if (trackingState == TrackingStateEnum.RUNNING) {
            if (TrackingUtility.hasLocationPermission(this)) {
                val request = LocationRequest.create().apply {
                    interval = LOCATION_UPDATE_INTERVAL
                    fastestInterval = FASTEST_LOCATION_INTERVAL
                    priority = PRIORITY_HIGH_ACCURACY
                }
                fusedLocationProviderClient.requestLocationUpdates(
                    request,
                    locationCallback,
                    Looper.getMainLooper()
                )
            }
        } else {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            if (trackingState.value == TrackingStateEnum.RUNNING) {
                locationResult.locations.let { locations ->
                    locations.forEach { location ->
                        addPathPoint(location)
                    }
                }
            }
        }
    }

    private fun addPathPoint(location: Location?) {
        location?.let {
            val point = LatLng(location.latitude, location.longitude)
            pathPoints.value?.apply {
                last().add(point)
                pathPoints.postValue(this)
            }
        }
    }

    private fun addEmptyPolyline() = pathPoints.value?.apply {
        add(mutableListOf())
        pathPoints.postValue(this)
    } ?: pathPoints.postValue(mutableListOf(mutableListOf()))

    private fun startRunningForegroundService() {
        startTimer()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }
        startForeground(NOTIFICATION_ID, baseNotificationBuilder.build())

        timeRunInSeconds.observe(this) {
            if(!isServiceDestroyed) {
                val trackingNotification = currentNotificationBuilder
                    .setContentText(TrackingUtility.formatTrackingTime(it * 1000L))
                notificationManager.notify(NOTIFICATION_ID, trackingNotification.build())
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }

    private fun updateNotificationTrackingState(trackingState: TrackingStateEnum) {
        val isTracking = (trackingState == TrackingStateEnum.RUNNING)

        if(isTracking) {
            currentNotificationBuilder.setContentTitle(
                HtmlCompat.fromHtml(
                    "<font color=\"${
                        ContextCompat.getColor(
                            this,
                            R.color.teal_200
                        )
                    }\">keep going ${String(Character.toChars(0x1F4AA))}</font>",
                    HtmlCompat.FROM_HTML_MODE_LEGACY
                )
            )
        } else {
            currentNotificationBuilder.setContentTitle("taking a break...")
        }

        val notificationActionText =
            if (isTracking) getString(R.string.pause) else getString(R.string.resume)
        val pendingIntent = PendingIntent.getService(
            this,
            if (isTracking) 1 else 2,
            Intent(this, TrackingService::class.java).apply {
                action = if (isTracking) ACTION_PAUSE_RUNNING else ACTION_START_OR_RESUME_RUNNING
            },
            FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE
        )

        currentNotificationBuilder.javaClass.getDeclaredField("mActions").apply {
            isAccessible = true
            set(currentNotificationBuilder, ArrayList<NotificationCompat.Action>())
        }

        if(!isServiceDestroyed) {
            currentNotificationBuilder = baseNotificationBuilder
                .addAction(R.drawable.ic_pause_24, notificationActionText, pendingIntent)
            notificationManager.notify(NOTIFICATION_ID, currentNotificationBuilder.build())
        }
    }

}