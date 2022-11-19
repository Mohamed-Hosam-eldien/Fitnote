package com.codingtester.fitnote.ui.fragments

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.codingtester.fitnote.R
import com.codingtester.fitnote.databinding.FragmentHomeBinding
import com.codingtester.fitnote.helper.Constants.REQUEST_LOCATION_PERMISSION_CODE
import com.codingtester.fitnote.helper.TrackingUtility
import com.codingtester.fitnote.ui.adapter.TapAdapter
import com.codingtester.fitnote.ui.viewmodels.MainViewModel
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

@AndroidEntryPoint
class HomeFragment : Fragment(), EasyPermissions.PermissionCallbacks {

    private val viewModel: MainViewModel by viewModels()

    private lateinit var  binding : FragmentHomeBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermissions()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        binding = FragmentHomeBinding.bind(view)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initTapLayout()
    }

    private fun initTapLayout() {
        binding.viewPager.adapter = TapAdapter(requireActivity().supportFragmentManager, lifecycle)
        val tapTitle = listOf("Tips", "Your Run")

        TabLayoutMediator(binding.tapLayout, binding.viewPager) {
            tap, position ->
                tap.text = tapTitle[position]
        }.attach()
    }


    private fun requestPermissions() {
        if(TrackingUtility.hasLocationPermission(requireActivity())) {
            return
        }
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            EasyPermissions.requestPermissions(
                this,
                "",
                REQUEST_LOCATION_PERMISSION_CODE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        } else {
            EasyPermissions.requestPermissions(
                this,
                "",
                REQUEST_LOCATION_PERMISSION_CODE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        if(EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        } else {
            requestPermissions()
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {}

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

}