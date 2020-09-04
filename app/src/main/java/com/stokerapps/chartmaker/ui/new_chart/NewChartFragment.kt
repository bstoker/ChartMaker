/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.ui.new_chart

import android.os.Bundle
import android.os.ParcelUuid
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.get
import androidx.recyclerview.widget.GridLayoutManager
import com.stokerapps.chartmaker.R
import com.stokerapps.chartmaker.databinding.FragmentNewChartBinding
import com.stokerapps.chartmaker.domain.Chart
import com.stokerapps.chartmaker.domain.PieChart
import com.stokerapps.chartmaker.ui.common.SpaceItemDecoration
import com.stokerapps.chartmaker.ui.common.dp
import com.stokerapps.chartmaker.ui.common.viewBinding
import com.stokerapps.chartmaker.ui.explorer.ExplorerViewModel

class NewChartFragment(
    viewModelFactory: ViewModelProvider.Factory
) : Fragment(R.layout.fragment_new_chart), NewChartAdapter.Callback {

    private val adapter = NewChartAdapter(this)
    private val binding by viewBinding(FragmentNewChartBinding::bind)
    private val viewModel: ExplorerViewModel by activityViewModels { viewModelFactory }

    private lateinit var pieChart: PieChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pieChart = PieChart.newChart.apply {
            name = getString(R.string.pie_chart)
        }

        adapter.update(
            listOf(
                pieChart
            )
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val spanCount = resources.displayMetrics.widthPixels / 196.dp
        with(binding.recyclerView) {
            setHasFixedSize(true)
            adapter = this@NewChartFragment.adapter
            layoutManager = GridLayoutManager(context, spanCount)
            addItemDecoration(SpaceItemDecoration(2.dp))
        }
    }

    override fun onChartPressed(chart: Chart) {
        when (chart) {
            is PieChart -> {
                val pieChart = PieChart()
                viewModel.createPieChart(pieChart)
                navigateToPieChart(pieChart)
            }
        }
    }

    private fun navigateToPieChart(
        chart: Chart,
        extras: FragmentNavigator.Extras = FragmentNavigatorExtras()
    ) {
        findNavController().let {
            if (it.currentDestination == it.graph[R.id.newChartFragment]) {
                val id = chart.id
                val action = NewChartFragmentDirections.showNewPieChart(ParcelUuid(id))
                it.navigate(action, extras)
            }
        }
    }
}