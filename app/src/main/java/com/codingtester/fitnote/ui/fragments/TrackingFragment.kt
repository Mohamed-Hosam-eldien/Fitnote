package com.codingtester.fitnote.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.codingtester.fitnote.R
import com.codingtester.fitnote.data.local.db.Run
import com.codingtester.fitnote.databinding.FragmentTrackingBinding
import com.codingtester.fitnote.helper.*
import com.codingtester.fitnote.services.Polyline
import com.codingtester.fitnote.services.TrackingService
import com.codingtester.fitnote.ui.viewmodels.MainViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class TrackingFragment : Fragment() {

    private val viewModel: MainViewModel by viewModels()

    private lateinit var binding: FragmentTrackingBinding

    private var map: GoogleMap? = null
    private var trackingState = TrackingStateEnum.HOLD
    private var pathPoints = mutableListOf<Polyline>()
    private var currentTimeInMillis = 0L
    private var startTimeStamp = 0L

    @set:Inject
    internal var weight = 0f

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

        binding.mapView.getMapAsync {
            map = it
            map?.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(),
                    R.raw.map_style
                )
            )
            addAllPolyline()
            if (pathPoints.isNotEmpty()) {
                binding.imgCancelRun.visibility = View.VISIBLE
            }
        }

        binding.btnStart.setOnClickListener {
            sendResponseToService(Constants.ACTION_START_OR_RESUME_RUNNING)
            startTimeStamp = Calendar.getInstance().timeInMillis
        }

        binding.btnResumeAndPause.setOnClickListener {
            changeRunningState()
        }

        binding.imgCancelRun.setOnClickListener { showCancelDialog() }

        binding.btnFinish.setOnClickListener { finishRunning() }

        subscribeToObservers()
    }

    private fun subscribeToObservers() {
        TrackingService.trackingState.observe(viewLifecycleOwner) {
            updateTrackingViews(it)
        }

        TrackingService.pathPoints.observe(viewLifecycleOwner) {
            pathPoints = it
            setRunningDataToViews()
            addLatestPolyline()
            moveCameraToPosition()
        }

        TrackingService.timeRunInMillis.observe(viewLifecycleOwner) {
            if(it != 0L) {
                currentTimeInMillis = it
                binding.txtTimer.text =
                    TrackingUtility.formatTrackingTime(currentTimeInMillis, true)
            } else {
                binding.txtTimer.text = getString(R.string._00_00_00)
            }
        }
    }

    private fun setRunningDataToViews() {
        var distanceInMeter = 0f
        for(polyline in pathPoints) {
            distanceInMeter = TrackingUtility.calculateTrackingLength(polyline)
        }

        binding.txtDistance.text = distanceInMeter.formatDistanceInKilometer()
        binding.txtAvgSpeed.text = distanceInMeter.calculateAvgSpeed(currentTimeInMillis)
        binding.txtCalories.text = calculateCalories(distanceInMeter)
    }

    private fun calculateCalories(distanceInMeter: Float): String {
        return ((distanceInMeter/1000f) * weight).toInt().toString()
    }


    private fun finishRunning() {
        map?.snapshot {bmp ->
            val distanceInMeter = binding.txtDistance.text.toString()
            val avgSpeed = binding.txtAvgSpeed.text.toString()
            val calories = binding.txtCalories.text.toString()
            val run = Run(
                bmp,
                Calendar.getInstance().timeInMillis,
                avgSpeed.toFloat(),
                distanceInMeter.toFloat(),
                currentTimeInMillis,
                calories.toInt()
            )
            viewModel.insertRunDataToDB(run)
            stopRunning()

            Snackbar.make(
                requireActivity().findViewById(R.id.root),
                getString(R.string.saved_successffuly),
                Snackbar.LENGTH_LONG
            ).show()
        }

    }

    private fun changeRunningState() {
        sendResponseToService(
            if (trackingState == TrackingStateEnum.RUNNING) Constants.ACTION_PAUSE_RUNNING
            else Constants.ACTION_START_OR_RESUME_RUNNING
        )
    }

    private fun updateTrackingViews(trackingState: TrackingStateEnum) {
        this.trackingState = trackingState
        when (trackingState) {
            TrackingStateEnum.RUNNING -> {
                binding.btnResumeAndPause.text = getString(R.string.pause)
                binding.txtRunningState.text = getString(R.string.keep_going)
                binding.imgCancelRun.visibility = View.VISIBLE
                binding.btnStart.visibility = View.GONE
                binding.linearPauseAndFinish.visibility = View.VISIBLE
            }
            TrackingStateEnum.PAUSE -> {
                binding.btnResumeAndPause.text = getString(R.string.resume)
                binding.txtRunningState.text = getString(R.string.taking_abreak)
                binding.imgCancelRun.visibility = View.VISIBLE
                binding.btnStart.visibility = View.GONE
                binding.linearPauseAndFinish.visibility = View.VISIBLE
            }
            TrackingStateEnum.CANCEL, TrackingStateEnum.HOLD -> {
                binding.txtTimer.text = getString(R.string._00_00_00)
                binding.txtRunningState.text = getString(R.string.let_s_go)
                binding.imgCancelRun.visibility = View.INVISIBLE
                binding.btnStart.visibility = View.VISIBLE
                binding.linearPauseAndFinish.visibility = View.GONE
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

    private fun showCancelDialog() {
        MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
            .setTitle(getString(R.string.cancel_run_dialog))
            .setMessage(getString(R.string.canel_run_dialog_message))
            .setIcon(R.drawable.ic_delete_24)
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                stopRunning()
            }
            .setNegativeButton(getString(R.string.no), null)
            .show()
    }

    private fun stopRunning() {
        sendResponseToService(Constants.ACTION_STOP_RUNNING)
        findNavController().navigate(R.id.action_trackingFragment_to_homeFragment)
    }

    @Deprecated("Deprecated in Java")
    override fun onPrepareOptionsMenu(menu: Menu) {
        if (currentTimeInMillis > 0L) {
            binding.imgCancelRun.visibility = View.VISIBLE
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