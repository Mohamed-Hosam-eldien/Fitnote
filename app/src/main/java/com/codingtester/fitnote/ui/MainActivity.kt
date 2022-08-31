package com.codingtester.fitnote.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.codingtester.fitnote.R
import com.codingtester.fitnote.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navController = findNavController(R.id.navHost)

        binding.bottomNav.background = null
        binding.bottomNav.menu.getItem(2).isEnabled = false

        binding.bottomNav.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.homeFragment, R.id.settingsFragment, R.id.statisticsFragment ->
                    binding.cord.visibility = View.VISIBLE
                else -> binding.cord.visibility = View.GONE
            }
        }

        binding.btnStartRun.setOnClickListener {
            navController.navigate(R.id.action_homeFragment_to_trackingFragment)
        }


    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

}