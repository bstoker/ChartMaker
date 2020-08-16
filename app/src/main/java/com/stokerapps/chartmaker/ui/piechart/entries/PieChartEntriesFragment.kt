/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.ui.piechart.entries

import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import com.google.android.material.snackbar.Snackbar
import com.stokerapps.chartmaker.R
import com.stokerapps.chartmaker.databinding.FragmentPieChartEntriesBinding
import com.stokerapps.chartmaker.domain.ChartRepository
import com.stokerapps.chartmaker.domain.EditorRepository
import com.stokerapps.chartmaker.domain.PieChartEntry
import com.stokerapps.chartmaker.ui.SavedStateViewModelFactory
import com.stokerapps.chartmaker.ui.common.*
import com.stokerapps.chartmaker.ui.common.color_picker.ColorPickerDialogFragment
import com.stokerapps.chartmaker.ui.piechart.Loaded
import com.stokerapps.chartmaker.ui.piechart.PieChartViewModel
import com.stokerapps.chartmaker.ui.piechart.ViewState
import kotlinx.coroutines.CoroutineScope


class PieChartEntriesFragment(
    applicationScope: CoroutineScope,
    chartRepository: ChartRepository,
    editorRepository: EditorRepository,
    private val viewModelFactory: ViewModelProvider.Factory
) : Fragment(R.layout.fragment_pie_chart_entries), PieChartEntriesAdapter.Callback,
    ColorPickerDialogFragment.Callback {

    private val binding by viewBinding(FragmentPieChartEntriesBinding::bind)
    private val viewModel: PieChartViewModel by activityViewModels {
        SavedStateViewModelFactory(
            requireActivity(),
            applicationScope,
            chartRepository,
            editorRepository
        )
    }

    private lateinit var adapter: PieChartEntriesAdapter
    private lateinit var layoutManager: LinearLayoutManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = PieChartEntriesAdapter(callback = this)
        layoutManager = LinearLayoutManager(context)

        with(binding) {
            recyclerView.adapter = adapter.apply {
                itemTouchHelper.attachToRecyclerView(recyclerView)
            }
            recyclerView.layoutManager = layoutManager
            recyclerView.addItemDecoration(SpaceItemDecoration(2.dp))
        }

        viewModel.viewState.observe(viewLifecycleOwner, Observer { onStateChanged(it) })
        viewModel.events.observe(viewLifecycleOwner, Observer { onEvent(it) })
    }

    private fun onStateChanged(state: ViewState) {
        when (state) {
            is Loaded -> adapter.submitList(state.chart.entries)
        }
    }

    private fun onEvent(event: Event) {
        when (event) {
            is ScrollToLastEntry -> scrollToLastPosition()
            is ShowColorPicker -> showColorPicker(viewModelFactory)
            is ShowUndoDeleteMessage -> showSnackbar(
                R.string.slice_deleted,
                R.string.undo,
                Snackbar.LENGTH_LONG
            ) { undoDelete() }
        }
    }

    private fun scrollToLastPosition() {
        layoutManager.startSmoothScroll(
            object : LinearSmoothScroller(context) {
                private val MILLISECONDS_PER_INCH = 500f
                override fun getVerticalSnapPreference(): Int {
                    return SNAP_TO_START
                }

                override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics?): Float {
                    return MILLISECONDS_PER_INCH / (displayMetrics?.densityDpi
                        ?: DisplayMetrics.DENSITY_HIGH)
                }
            }.apply {
                targetPosition = adapter.itemCount - 1
            })
    }

    private fun undoDelete() {
        viewModel.undoDelete()
    }

    override fun onColorClicked(entry: PieChartEntry) {
        viewModel.changeColor(entry)
    }

    override fun onDeletePressed(entry: PieChartEntry) {
        viewModel.delete(entry)
    }

    override fun onColorSelected(color: Int) {
        viewModel.updateEntryColor(color)
    }

    override fun onAddNewEntryPressed() {
        viewModel.addNewEntry()
    }

    override fun onEntriesChanged(entries: List<PieChartEntry>) {
        viewModel.update(entries)
    }

    override fun onEntryChanged(entry: PieChartEntry) {
        viewModel.update(entry)
    }
}
