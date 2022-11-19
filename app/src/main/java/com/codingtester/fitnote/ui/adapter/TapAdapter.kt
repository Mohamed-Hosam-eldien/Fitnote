package com.codingtester.fitnote.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.codingtester.fitnote.ui.fragments.HomeFragment
import com.codingtester.fitnote.ui.fragments.RunFragment
import com.codingtester.fitnote.ui.fragments.TipsFragment

class TapAdapter(manager: FragmentManager, lifeCycle: Lifecycle)
    : FragmentStateAdapter(manager, lifeCycle) {

    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> TipsFragment()
            1 -> RunFragment()
            else -> HomeFragment()
        }
    }

}