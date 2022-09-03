package com.codingtester.fitnote.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.codingtester.fitnote.R
import com.codingtester.fitnote.databinding.FragmentTrackingBinding
import com.codingtester.fitnote.helper.Constants
import com.codingtester.fitnote.helper.TrackingStateEnum
import com.codingtester.fitnote.helper.TrackingUtility
import com.codingtester.fitnote.services.Polyline
import com.codingtester.fitnote.services.TrackingService
import com.codingtester.fitnote.ui.viewmodels.MainViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.PolylineOptions

class TrackingFragment : Fragment() {

    private val viewModel: MainViewModel by viewModels()

    private lateinit var binding: FragmentTrackingBinding

    private var map: GoogleMap? = null
    private var trackingState = TrackingStateEnum.HOLD
    private var pathPoints = mutableListOf<Polyline>()
    private var currentTimeInMillis = 0L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTrackingBinding.bind(
            inflater.inflate(
                R.layout.fragment_tracking,
                container,
                false
            )
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.mapView.onCreate(savedInstanceState)

        binding.btnStart.setOnClickListener {
            sendResponseToService(Constants.ACTION_START_OR_RESUME_RUNNING)
        }

        binding.btnResumeAndPause.setOnClickListener {
            changeRunningState()
        }

        binding.mapView.getMapAsync {
            map = it
            map?.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(),
                    R.raw.map_style
                )
            )
            addAllPolyline()
        }

        subscribeToObservers()
    }

    private fun subscribeToObservers() {
        TrackingService.trackingState.observe(viewLifecycleOwner) {
            updateTrackingViews(it)
        }

        TrackingService.pathPoints.observe(viewLifecycleOwner) {
            pathPoints = it
            addLatestPolyline()
            moveCameraToPosition()
        }

        TrackingService.timeRunInMillis.observe(viewLifecycleOwner) {
            currentTimeInMillis = it
            binding.txtTimer.text = TrackingUtility.formatTrackingTime(currentTimeInMillis, true)
        }
    }

    private fun changeRunningState() {
        sendResponseToService(
            if (trackingState == TrackingStateEnum.RUNNING) Constants.ACTION_PAUSE_RUNNING else Constants.ACTION_START_OR_RESUME_RUNNING
        )
    }

    private fun updateTrackingViews(trackingState: TrackingStateEnum) {
        this.trackingState = trackingState
        when (trackingState) {
            TrackingStateEnum.RUNNING -> {
                binding.btnStart.visibility = View.GONE
                binding.btnResumeAndPause.text = getString(R.string.pause)
                binding.linearPauseAndFinish.visibility = View.VISIBLE
            }
            TrackingStateEnum.PAUSE -> {
                binding.btnStart.visibility = View.GONE
                binding.btnResumeAndPause.text = getString(R.string.resume)
                binding.linearPauseAndFinish.visibility = View.VISIBLE
            }
            else -> {
                binding.btnResumeAndPause.text = getString(R.string.resume)
            }
        }
    }

    private fun moveCameraToPosition() {
        if (pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()) {
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    pathPoints.last().last(),
                    Constants.CAMERA_ZOOM
                )
            )
        }
    }

    private fun addAllPolyline() {
        pathPoints.forEach {
            val polyLineOption = PolylineOptions()
                .color(Constants.POLYLINE_COLOR)
                .width(Constants.POLYLINE_WIDTH)
                .addAll(it)
            map?.addPolyline(polyLineOption)
            moveCameraToPosition()
        }
    }

    private fun addLatestPolyline() {
        if (pathPoints.isNotEmpty() && pathPoints.last().size > 1) {
            val preLastLocation = pathPoints.last()[pathPoints.last().size - 2]
            val lastLocation = pathPoints.last().last()
            val polyLineOption = PolylineOptions()
                .color(Constants.POLYLINE_COLOR)
                .width(Constants.POLYLINE_WIDTH)
                .add(preLastLocation)
                .add(lastLocation)
            map?.addPolyline(polyLineOption)
        }
    }

    private fun sendResponseToService(action: String) {
        Intent(requireContext(), TrackingService::class.java).also {
            it.action = action
            requireContext().startService(it)
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
        addAllPolyline()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }
}