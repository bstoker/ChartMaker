/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.ui.explorer

import android.os.Bundle
import android.os.ParcelUuid
import android.view.*
import android.widget.PopupWindow
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.get
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.stokerapps.chartmaker.R
import com.stokerapps.chartmaker.databinding.FragmentExplorerBinding
import com.stokerapps.chartmaker.domain.Chart
import com.stokerapps.chartmaker.domain.Sort
import com.stokerapps.chartmaker.domain.usecases.ExportCsvFiles
import com.stokerapps.chartmaker.domain.usecases.ImportCsvFiles
import com.stokerapps.chartmaker.ui.common.*
import com.stokerapps.chartmaker.ui.common.event_handlers.ExportCsvFileHandler
import com.stokerapps.chartmaker.ui.common.event_handlers.ImportCsvFileHandler
import com.stokerapps.chartmaker.ui.common.export_dialog.ExportDialogFragment
import com.stokerapps.chartmaker.ui.common.export_dialog.ExportViewModel
import java.util.*
import kotlin.collections.set


class ExplorerFragment(
    private val viewModelFactory: ViewModelProvider.Factory
) : Fragment(R.layout.fragment_explorer), ChartAdapter.Callback, SortChangedCallback {

    private lateinit var itemKeyProvider: ItemKeyProvider

    private val adapter = ChartAdapter(this).apply {
        registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                binding.recyclerView.layoutManager?.scrollToPosition(0)
            }
        })
    }

    @VisibleForTesting
    val binding by viewBinding(FragmentExplorerBinding::bind)

    @VisibleForTesting
    val viewModel: ExplorerViewModel by activityViewModels { viewModelFactory }
    private val exportViewModel: ExportViewModel by activityViewModels { viewModelFactory }

    private var actionMode: ActionMode? = null
    private var selectionTracker: SelectionTracker<String>? = null
    private val shortAnimationDuration by lazy {
        resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
    }
    private val spanCount by lazy { resources.displayMetrics.widthPixels / 330.dp }
    private lateinit var sortMenuItem: MenuItem
    private val sortMenuPopupWindow: PopupWindow by lazy { createSortPopupWindow() }
    private val sortView: SortView by lazy { createSortView() }

    private val openFiles =
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { files ->
            if (files.isNotEmpty()) {
                viewModel.import(files.map { it.toString() })
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        selectionTracker?.onSaveInstanceState(outState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {

            activity.let {
                if (it is AppCompatActivity) {
                    it.setSupportActionBar(toolbar)
                }
            }

            add.setOnClickListener { onAddPressed() }
            header.headerView.callback = this@ExplorerFragment
            itemKeyProvider = ItemKeyProvider(recyclerView)

            with(recyclerView) {
                setHasFixedSize(true)
                adapter = this@ExplorerFragment.adapter
                layoutManager = GridLayoutManager(context, spanCount)
                addItemDecoration(SpaceItemDecoration(2.dp))
            }

            with(createSelectionTracker(recyclerView, itemKeyProvider, savedInstanceState)) {
                selectionTracker = this
                addObserver(onSelectionChanged)
                if (hasSelection()) {
                    startActionMode()
                }
                adapter.selectionTracker = this
            }
        }
        exportViewModel.events.observe(viewLifecycleOwner, Observer { onEvent(it) })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.explorer_toolbar_menu, menu)
        context?.getColorAttribute(R.attr.colorControlNormal)?.let { color ->
            menu.setIconTint(color)
        }
        sortMenuItem = menu.findItem(R.id.sort)

        viewModel.events.observe(viewLifecycleOwner, Observer { onEvent(it) })
        viewModel.viewState.observe(viewLifecycleOwner, Observer { onStateChanged(it) })
    }

    override fun onOptionsItemSelected(item: MenuItem) =
        when (item.itemId) {
            R.id.sort -> {
                val view = binding.toolbar.findViewById<View>(R.id.sort)
                sortMenuPopupWindow.showAtLocation(view, Gravity.TOP or Gravity.END, 4.dp, 4.dp)
                true
            }
            R.id._import -> {
                openFiles.launch("text/*")
                true
            }
            R.id.feedback -> {
                FeedbackDialogFragment.newInstance().show(childFragmentManager, "feedback")
                true
            }
            R.id.about -> {
                AboutDialogFragment.newInstance().show(childFragmentManager, "about")
                true
            }
            else -> false
        }

    private fun onEvent(event: Any) {
        when (event) {
            is ExportCsvFiles -> ExportCsvFileHandler(this).handle(event)
            is ImportCsvFiles -> ImportCsvFileHandler(this).handle(event)
            is ShowUndoDeleteMessage -> showSnackbar(
                resources.getQuantityString(
                    R.plurals.x_charts_deleted,
                    event.deleteCount,
                    event.deleteCount
                ),
                R.string.undo,
                Snackbar.LENGTH_LONG
            ) { undoDelete() }
        }
    }

    private fun onStateChanged(state: ViewState) {
        when (state) {
            is Loaded -> {
                with(binding) {
                    add.visibility = View.VISIBLE
                    loadingIndicator.visibility = View.GONE
                    noCharts.noChartsView.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                    header.headerView.visibility = if (spanCount > 1) View.GONE else View.VISIBLE
                    header.headerView.update(state.sort)
                }

                setSortMenuVisibility(spanCount > 1 && state.charts.size > 1)
                sortView.update(state.sort)

                itemKeyProvider.invalidateItemPositions()
                adapter.submitList(state.charts)
            }
            is Empty -> {
                with(binding) {
                    add.visibility = View.VISIBLE
                    loadingIndicator.visibility = View.GONE
                    noCharts.noChartsView.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                    header.headerView.visibility = View.GONE
                }
                setSortMenuVisibility(false)

                itemKeyProvider.invalidateItemPositions()
                adapter.submitList(state.charts)
            }
            is Loading -> {
                with(binding) {
                    add.visibility = View.GONE
                    header.headerView.visibility = View.GONE
                    loadingIndicator.visibility = View.VISIBLE
                    noCharts.noChartsView.visibility = View.GONE
                    recyclerView.visibility = View.INVISIBLE
                }
                setSortMenuVisibility(false)
            }
        }
    }

    private fun setSortMenuVisibility(isVisible: Boolean) {
        sortMenuItem.isVisible = isVisible
    }

    private fun undoDelete() {
        viewModel.undoDelete()
    }

    private fun onAddPressed() {
        navigateToNewChart()
    }

    private fun onDeletePressed() {
        getSelectedChartIds()?.let { viewModel.delete(it) }
    }

    private fun onExportPressed() {
        getSelectedChartIds()?.let { chartIds ->
            ExportDialogFragment.newInstance(viewModelFactory, chartIds)
                .show(childFragmentManager, ExportDialogFragment.TAG)
        }
    }

    private fun getSelectedChartIds(): List<UUID>? =
        adapter.selectionTracker?.selection?.mapNotNull {
            UUID.fromString(it)
        }

    override fun onItemPressed(chart: Chart, view: View) {
        if (actionMode == null) {
            navigateToChart(chart)
        }
    }

    override fun onSortChanged(sort: Sort) {
        sortMenuPopupWindow.dismiss()
        viewModel.sort(sort)
    }

    private fun startActionMode() {
        actionMode = activity?.startActionMode(actionModeCallback)
    }

    private fun navigateToChart(
        chart: Chart,
        extras: FragmentNavigator.Extras = FragmentNavigatorExtras()
    ) {
        findNavController().let {
            if (it.currentDestination == it.graph[R.id.explorerFragment]) {
                val id = chart.id
                val action = ExplorerFragmentDirections.showPieChart(ParcelUuid(id))
                it.navigate(action, extras)
            }
        }
    }

    private fun navigateToNewChart() {
        findNavController().let {
            if (it.currentDestination == it.graph[R.id.explorerFragment]) {
                it.navigate(R.id.createNewChart)
            }
        }
    }

    private val actionModeCallback = object : ActionMode.Callback {

        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            val inflater: MenuInflater = mode.menuInflater
            inflater.inflate(R.menu.explorer_context_menu, menu)
            val color = getColorCompat(R.color.colorOnPrimary, NERO)
            menu.setItemColor(R.id.delete, color)
            menu.setItemColor(R.id.export, color)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            binding.add.fadeOut(shortAnimationDuration)
            mode.title = selectionTracker?.let { "${it.selection.size()}" } ?: ""
            return false
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            return when (item.itemId) {
                R.id.delete -> {
                    onDeletePressed()
                    mode.finish()
                    true
                }
                R.id.export -> {
                    onExportPressed()
                    mode.finish()
                    true
                }
                else -> false
            }
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            selectionTracker?.clearSelection()
            binding.add.fadeIn(shortAnimationDuration)
            actionMode = null
        }
    }

    private val onSelectionChanged = object : SelectionTracker.SelectionObserver<String>() {
        override fun onSelectionChanged() {
            selectionTracker?.let {
                if (it.hasSelection()) {
                    if (actionMode == null) {
                        startActionMode()
                    }
                    actionMode?.title = "${it.selection.size()}"

                } else {
                    actionMode?.finish()
                }
            }
        }
    }

    private fun createSelectionTracker(
        recyclerView: RecyclerView,
        itemKeyProvider: ItemKeyProvider,
        savedInstanceState: Bundle?
    ): SelectionTracker<String> =
        SelectionTracker.Builder(
            "selection",
            recyclerView,
            itemKeyProvider,
            ItemDetailsLookup(recyclerView),
            StorageStrategy.createStringStorage()
        ).withSelectionPredicate(
            SelectionPredicates.createSelectAnything()
        ).build().apply {
            onRestoreInstanceState(savedInstanceState)
        }

    private fun createSortPopupWindow(): PopupWindow =
        PopupWindow(
            sortView,
            196.dp,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        ).apply {
            animationStyle = R.style.PopupWindowAnimation
        }

    private fun createSortView(): SortView {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.view_sort, view as ViewGroup, false) as SortView
        view.callback = this@ExplorerFragment
        return view
    }

    class ItemKeyProvider(private val recyclerView: RecyclerView) :
        androidx.recyclerview.selection.ItemKeyProvider<String>(SCOPE_CACHED) {

        private val keyToPosition = mutableMapOf<String, Int>()
        private val items: List<Chart?>?
            get() = (recyclerView.adapter as? ChartAdapter)?.currentList

        fun invalidateItemPositions() {
            keyToPosition.clear()
        }

        private fun keyOf(item: Chart?) = item?.id.toString()

        override fun getKey(position: Int): String? =
            items?.get(position)?.let { item -> keyOf(item) }

        override fun getPosition(key: String) =
            if (keyToPosition.containsKey(key) && keyToPosition[key] != RecyclerView.NO_POSITION) {
                keyToPosition[key]
            } else {
                items
                    ?.filterNotNull()
                    ?.indexOfFirst { key == keyOf(it) }
                    ?.also { index -> keyToPosition[key] = index }
            } ?: RecyclerView.NO_POSITION
    }

    class ItemDetailsLookup(private val recyclerView: RecyclerView) :
        androidx.recyclerview.selection.ItemDetailsLookup<String>() {
        override fun getItemDetails(event: MotionEvent): ItemDetails<String>? {
            val view = recyclerView.findChildViewUnder(event.x, event.y)
            if (view != null) {
                val viewHolder = (recyclerView.getChildViewHolder(view) as ChartViewHolder)
                return viewHolder.getItemDetails()
            }
            return null
        }
    }
}