package com.codingtester.fitnote.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
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
import com.codingtester.fitnote.helper.Constants.ACTION_SHOW_TRACKING_FRAGMENT_FROM_NOTIFICATION
import com.codingtester.fitnote.helper.Constants.ACTION_START_OR_RESUME_RUNNING
import com.codingtester.fitnote.helper.Constants.ACTION_STOP_RUNNING
import com.codingtester.fitnote.helper.Constants.FASTEST_LOCATION_INTERVAL
import com.codingtester.fitnote.helper.Constants.LOCATION_UPDATE_INTERVAL
import com.codingtester.fitnote.helper.Constants.NOTIFICATION_CHANNEL_ID
import com.codingtester.fitnote.helper.Constants.NOTIFICATION_CHANNEL_NAME
import com.codingtester.fitnote.helper.Constants.NOTIFICATION_ID
import com.codingtester.fitnote.helper.TrackingStateEnum
import com.codingtester.fitnote.helper.TrackingUtility
import com.codingtester.fitnote.ui.MainActivity
import com.google.android.gms.location.*
import com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

typealias Polyline = MutableList<LatLng>
typealias Polylines = MutableList<Polyline>

class TrackingService: LifecycleService() {

    private var isFirstRunning = true
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private val timeRunInSeconds = MutableLiveData<Long>()

    companion object{
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
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        trackingState.observe(this) {
            updateLocationTracking(it)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when(it.action) {
                ACTION_START_OR_RESUME_RUNNING -> {
                    if(isFirstRunning) {
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
                    Timber.d("stop")
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
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
                if(timeRunInMillis.value!! >= lastSecondTimeStamp + 1000L) {
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
        if(trackingState == TrackingStateEnum.RUNNING) {
            if(TrackingUtility.hasLocationPermission(this)) {
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
            if(trackingState.value == TrackingStateEnum.RUNNING) {
                locationResult.locations.let {locations ->
                    locations.forEach { location->
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

    private fun startRunningForegroundService(){
        startTimer()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
            as NotificationManager

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        val color = ContextCompat.getColor(this, R.color.teal_200)

        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(R.drawable.runner_small)
            .setColor(color)
            .setContentTitle(HtmlCompat.fromHtml("<font color=\"$color\">Running, keep going ${String(Character.toChars(0x1F4AA))}</font>", HtmlCompat.FROM_HTML_MODE_LEGACY))
            .setContentText("00:00:00")
            .setContentIntent(getPendingIntent())

        startForeground(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun getPendingIntent() = PendingIntent.getActivity(
        this,
        0,
        Intent(this, MainActivity::class.java).also {
            it.action = ACTION_SHOW_TRACKING_FRAGMENT_FROM_NOTIFICATION
        },
        FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }

}