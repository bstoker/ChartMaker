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
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.VisibleForTesting
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.google.android.material.snackbar.Snackbar
import com.stokerapps.chartmaker.R
import com.stokerapps.chartmaker.databinding.FragmentPieChartBinding
import com.stokerapps.chartmaker.domain.ChartRepository
import com.stokerapps.chartmaker.domain.EditorRepository
import com.stokerapps.chartmaker.domain.PieChartEntry
import com.stokerapps.chartmaker.ui.SavedStateViewModelFactory
import com.stokerapps.chartmaker.ui.common.*
import kotlinx.coroutines.CoroutineScope
import com.stokerapps.chartmaker.common.AppDispatchers as Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

private const val FILE_NAME_PREFIX = "pie_chart_"

class PieChartFragment(
    private val applicationScope: CoroutineScope,
    private val chartRepository: ChartRepository,
    private val editorRepository: EditorRepository,
    private val viewModelFactory: ViewModelProvider.Factory
) : Fragment(R.layout.fragment_pie_chart), OnChartValueSelectedListener,
    DialogInterface.OnCancelListener {

    private val arguments: PieChartFragmentArgs by navArgs()

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

            screenshot.setOnClickListener { onScreenshotPressed() }

            sidebarCollapseExpand?.setOnClickListener { onSidebarExpandCollapseToggle() }
        }

        shortAnimationDuration =
            resources.getInteger(android.R.integer.config_shortAnimTime).toLong()

        viewModel.events.observe(viewLifecycleOwner, Observer { onEvent(it) })
        viewModel.viewState.observe(viewLifecycleOwner, Observer(::onStateChanged))
    }

    private fun onEvent(event: Event) {
        when (event) {
            NavigateToProperties -> navigateToProperties()
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
        ScreenshotMaker(context).takeAndSaveScreenshot(binding.chart, newFileName())

    private fun newFileName(): String {
        val formatter =
            SimpleDateFormat(getString(R.string.timestamp_file_format), Locale.getDefault())
        val timestamp = formatter.format(Date())
        return FILE_NAME_PREFIX + timestamp
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
}


