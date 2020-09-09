/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.ui.piechart

import android.Manifest
import android.animation.ValueAnimator
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.VisibleForTesting
import androidx.appcompat.widget.ListPopupWindow
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.google.android.material.snackbar.Snackbar
import com.stokerapps.chartmaker.R
import com.stokerapps.chartmaker.common.Timestamp
import com.stokerapps.chartmaker.databinding.FragmentPieChartBinding
import com.stokerapps.chartmaker.domain.ChartRepository
import com.stokerapps.chartmaker.domain.EditorRepository
import com.stokerapps.chartmaker.domain.PieChartEntry
import com.stokerapps.chartmaker.domain.usecases.ExportCsvFiles
import com.stokerapps.chartmaker.ui.SavedStateViewModelFactory
import com.stokerapps.chartmaker.ui.common.*
import com.stokerapps.chartmaker.ui.common.event_handlers.ExportCsvFileHandler
import com.stokerapps.chartmaker.ui.common.export_dialog.ExportDialogFragment
import com.stokerapps.chartmaker.ui.common.export_dialog.ExportViewModel
import com.stokerapps.chartmaker.ui.common.options_menu.Option
import com.stokerapps.chartmaker.ui.common.options_menu.OptionView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.stokerapps.chartmaker.common.AppDispatchers as Dispatchers

class PieChartFragment(
    private val applicationScope: CoroutineScope,
    private val chartRepository: ChartRepository,
    private val editorRepository: EditorRepository,
    private val viewModelFactory: ViewModelProvider.Factory
) : Fragment(R.layout.fragment_pie_chart), OnChartValueSelectedListener,
    DialogInterface.OnCancelListener {

    private val arguments: PieChartFragmentArgs by navArgs()
    private val optionsPopupWindow by lazy { createOptionsPopupWindow() }

    @VisibleForTesting
    val binding by viewBinding(FragmentPieChartBinding::bind)

    private val viewModel: PieChartViewModel by activityViewModels {
        SavedStateViewModelFactory(
            requireActivity(),
            applicationScope,
            chartRepository,
            editorRepository
        )
    }
    private val exportViewModel: ExportViewModel by activityViewModels { viewModelFactory }

    private val askPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isPermissionGranted ->
            if (isPermissionGranted) {
                onScreenshotPressed()

            } else if (userCheckedDoNotAskAgain()) {
                showSnackbar(
                    R.string.please_enable_storage_permission,
                    R.string.settings
                ) {
                    navigateToAppSettings()
                }
            }
        }

    private var errorMessage: Snackbar? = null
    private var shortAnimationDuration: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.show(arguments.id.uuid)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!isChangingConfiguration()) {
            viewModel.show(null)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {

            chart.setExtraOffsets(5f, 10f, 5f, 5f)
            chart.setOnChartValueSelectedListener(this@PieChartFragment)

            edit?.setOnClickListener { viewModel.editChart() }

            options.setOnClickListener { onOptionsPressed() }

            sidebarCollapseExpand?.setOnClickListener { onSidebarExpandCollapseToggle() }
        }

        shortAnimationDuration =
            resources.getInteger(android.R.integer.config_shortAnimTime).toLong()

        viewModel.events.observe(viewLifecycleOwner) { onEvent(it) }
        viewModel.viewState.observe(viewLifecycleOwner) { onStateChanged(it) }

        exportViewModel.events.observe(viewLifecycleOwner) { onEvent(it) }
    }

    private fun onEvent(event: Any) {
        when (event) {
            is ExportCsvFiles -> ExportCsvFileHandler(this).handle(event)
            is NavigateToProperties -> navigateToProperties()
        }
    }

    private fun onStateChanged(state: ViewState) {
        with(binding) {
            when (state) {
                is Loading -> {
                    content.visibility = View.GONE
                    loadingIndicator.visibility = View.VISIBLE
                }
                is Loaded -> {
                    chart.update(state.chart)
                    chart.visibility = View.VISIBLE
                    emptyChart.root.visibility = View.GONE
                    content.visibility = View.VISIBLE
                    loadingIndicator.visibility = View.GONE
                    updateSidebar(state.isEditSidebarExpanded, false)
                }
                is Empty -> {
                    chart.visibility = View.GONE
                    emptyChart.root.visibility = View.VISIBLE
                    content.visibility = View.VISIBLE
                    loadingIndicator.visibility = View.GONE
                }
                is ErrorLoading -> {
                    content.visibility = View.GONE
                    loadingIndicator.visibility = View.VISIBLE
                    errorMessage = showSnackbar(
                        R.string.failed_to_load_chart,
                        R.string.back,
                        Snackbar.LENGTH_INDEFINITE
                    ) {
                        findNavController().navigateUp()
                        errorMessage?.dismiss()
                    }
                }
            }
        }
    }

    private fun onSidebarExpandCollapseToggle() {
        val isExpanded = viewModel.isSidebarExpanded().not()
        viewModel.setSidebarExpanded(isExpanded)
        updateSidebar(isExpanded, true)
    }

    private fun updateSidebar(isExpanded: Boolean, fromUser: Boolean) {
        val angle = if (isExpanded) -90f else 90f
        val duration = 200L
        val margin = 300.dp
        val marginFrom = if (isExpanded) 0 else margin
        val marginTo = if (isExpanded) margin else 0
        if (fromUser) {
            binding.sidebarCollapseExpand?.animate()?.setDuration(duration)?.rotation(angle)
            binding.guideline?.let { guideline ->
                val animator = ValueAnimator.ofInt(marginFrom, marginTo)
                animator.addUpdateListener { animation ->
                    guideline.setGuidelineEnd(animation.animatedValue as Int)
                }
                animator.duration = duration
                animator.start()
            }
        } else {
            binding.sidebarCollapseExpand?.rotation = angle
            binding.guideline?.setGuidelineEnd(marginTo)
        }
    }

    private fun onOptionsPressed() {
        optionsPopupWindow.anchorView = binding.options
        optionsPopupWindow.show()
    }

    private fun onOptionItemClicked(optionId: Long) {
        when (optionId) {
            OptionAdapter.TAKE_SCREENSHOT -> onScreenshotPressed()
            OptionAdapter.EXPORT_AS -> showSaveAsDialog()
        }
        optionsPopupWindow.dismiss()
    }

    private fun showSaveAsDialog() {
        viewModel.chart?.let { chart ->
            ExportDialogFragment.newInstance(viewModelFactory, listOf(chart.id), chart.name)
                .show(childFragmentManager, ExportDialogFragment.TAG)
        }
    }

    private fun onScreenshotPressed() {
        when {
            hasPermission() -> lifecycleScope.launch(Dispatchers.Default) {

                val success = takeAndSaveScreenshot()
                withContext(Dispatchers.Main) {
                    if (success) {
                        showSnackbar(
                            R.string.screenshot_saved,
                            R.string.show
                        ) { navigateToGallery() }
                    } else {
                        showToast(R.string.failed_to_save_screenshot)
                    }
                    Unit
                }
            }
            else -> askPermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    private fun hasPermission(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
            || activity?.let {
        (ContextCompat.checkSelfPermission(it, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED)
    } ?: false

    private fun userCheckedDoNotAskAgain(): Boolean = activity?.let {
        !ActivityCompat.shouldShowRequestPermissionRationale(
            it,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    } ?: false

    private suspend fun takeAndSaveScreenshot() =
        ScreenshotMaker(context).takeAndSaveScreenshot(binding.chart, createFilename())

    private fun createFilename(): String {
        return "${getString(R.string.chart)}_$Timestamp"
    }

    private fun navigateToProperties() {
        findNavController().let {
            if (it.currentDestination?.id == R.id.pieChartFragment) {
                val action = PieChartFragmentDirections.editPieChart()
                it.navigate(action)
            }
        }
    }

    private fun navigateToGallery() {
        startActivity(Intent().apply {
            action = Intent.ACTION_VIEW
            type = "image/*"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
    }

    private fun navigateToAppSettings() {
        startActivity(Intent().apply {
            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            data = Uri.fromParts("package", context?.packageName, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
    }

    private fun createOptionsPopupWindow(): ListPopupWindow {
        return ListPopupWindow(requireContext()).apply {
            animationStyle = R.style.PopupWindowAnimationBottomLeft
            setAdapter(OptionAdapter())
            setOnItemClickListener { _, _, _, id -> onOptionItemClicked(id) }
            setContentWidth(200.dp)
            isModal = true
        }
    }

    override fun onNothingSelected() {
    }

    override fun onValueSelected(e: Entry?, h: Highlight?) {
        val entry = e?.data
        if (entry is PieChartEntry) {
            PieChartEntryEditDialogFragment.newInstance(
                applicationScope,
                chartRepository,
                editorRepository,
                viewModelFactory,
                entry
            ).show(childFragmentManager, "PieChartEntryEditDialogFragment")
        }
    }

    override fun onCancel(dialog: DialogInterface?) {
        binding.chart.highlightValues(null)
    }

    private class OptionAdapter : BaseAdapter() {

        companion object {
            const val TAKE_SCREENSHOT = 1L
            const val EXPORT_AS = 2L
        }

        val options = listOf(
            Option(
                R.drawable.ic_photo_camera,
                R.string.take_screenshot
            ),
            Option(
                R.drawable.ic_save,
                R.string.export_as
            )
        )

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

            val option = options[position]
            return when (convertView) {
                null -> LayoutInflater.from(parent?.context)
                    .inflate(R.layout.view_option, parent, false) as OptionView
                else -> convertView as OptionView
            }.apply {
                icon.setImageResource(option.icon)
                icon.contentDescription = parent?.context?.getString(option.description)
                text.setText(option.description)
            }
        }

        override fun getItem(position: Int) = options[position]

        override fun getItemId(position: Int) = when (position) {
            0 -> TAKE_SCREENSHOT
            else -> EXPORT_AS
        }

        override fun getCount(): Int = options.size
    }
}


