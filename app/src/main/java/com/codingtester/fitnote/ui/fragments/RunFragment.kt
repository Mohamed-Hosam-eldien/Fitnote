package com.codingtester.fitnote.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.codingtester.fitnote.R
import com.codingtester.fitnote.databinding.FragmentRunBinding
import com.codingtester.fitnote.helper.SortedType
import com.codingtester.fitnote.ui.adapter.RunAdapter
import com.codingtester.fitnote.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RunFragment : Fragment() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding : FragmentRunBinding
    private lateinit var runAdapter : RunAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_run, container, false)
        binding = FragmentRunBinding.bind(view)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRecyclerView()

        viewModel.runList.observe(viewLifecycleOwner) {
            runAdapter.setList(it)
        }

        val array = resources.getStringArray(R.array.sorted_type)
        val sortAdapter = ArrayAdapter(requireContext(), R.layout.sorted_item, array)
        binding.autoComplete.setAdapter(sortAdapter)

        binding.autoComplete.setOnItemClickListener { _, _, pos, _ ->
            when (pos) {
                0 -> viewModel.sortRun(SortedType.DATE)
                1 -> viewModel.sortRun(SortedType.TIME)
                2 -> viewModel.sortRun(SortedType.DISTANCE)
                3 -> viewModel.sortRun(SortedType.AVG_SPEED)
                4 -> viewModel.sortRun(SortedType.CALORIES)
            }
        }

    }

    private fun initRecyclerView() {
        binding.recyclerRun.apply {
            setHasFixedSize(true)
            runAdapter = RunAdapter()
            adapter = runAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

}