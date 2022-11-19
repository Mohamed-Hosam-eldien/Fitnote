package com.codingtester.fitnote.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.codingtester.fitnote.R
import com.codingtester.fitnote.databinding.FragmentSetupBinding
import com.codingtester.fitnote.helper.Constants.USER_FIRST_SIGNED
import com.codingtester.fitnote.helper.Constants.USER_NAME
import com.codingtester.fitnote.helper.Constants.USER_WEIGHT
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SetupFragment : Fragment() {

    private lateinit var binding: FragmentSetupBinding

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    @set:Inject
    internal var isUserFirstSigned = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentSetupBinding.bind(layoutInflater.inflate(R.layout.fragment_setup, container, false))
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(!isUserFirstSigned) {
            val navOption = NavOptions.Builder()
                .setPopUpTo(R.id.setupFragment, true)
                .build()

            findNavController().navigate(
                R.id.action_setupFragment_to_homeFragment,
                savedInstanceState,
                navOption)
        }

        binding.btnContinue.setOnClickListener {
            if(saveUserData()) {
                findNavController().navigate(R.id.action_setupFragment_to_homeFragment)
            } else {
                Snackbar.make(requireView(), "please fill all data", Snackbar.LENGTH_SHORT).show()
            }
        }

    }

    private fun saveUserData(): Boolean {
        val name = binding.edtName.text.toString()
        val weight = binding.edtWeight.text.toString()

        if(name.isEmpty() || weight.isEmpty()) {
            return false
        }

        sharedPreferences.edit()
            .putString(USER_NAME, name)
            .putFloat(USER_WEIGHT, weight.toFloat())
            .putBoolean(USER_FIRST_SIGNED, false)
            .apply()

        return true
    }


}