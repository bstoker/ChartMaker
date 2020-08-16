/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.ui.piechart.properties

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.stokerapps.chartmaker.R
import com.stokerapps.chartmaker.domain.ChartRepository
import com.stokerapps.chartmaker.domain.EditorRepository
import com.stokerapps.chartmaker.ui.SavedStateViewModelFactory
import com.stokerapps.chartmaker.ui.common.*
import com.stokerapps.chartmaker.ui.common.color_picker.ColorPickerDialogFragment
import com.stokerapps.chartmaker.ui.piechart.PieChartViewModel
import kotlinx.coroutines.CoroutineScope

class PieChartPropertiesFragment(
    private val applicationScope: CoroutineScope,
    private val chartRepository: ChartRepository,
    private val editorRepository: EditorRepository,
    private val viewModelFactory: ViewModelProvider.Factory
) : Fragment(), ColorPickerDialogFragment.Callback {

    private val pieChartViewModel: PieChartViewModel by activityViewModels {
        SavedStateViewModelFactory(
            requireActivity(),
            applicationScope,
            chartRepository,
            editorRepository
        )
    }
    private val viewModel: PieChartPropertiesViewModel by viewModels {
        SavedStateViewModelFactory(
            this,
            applicationScope,
            chartRepository,
            editorRepository
        )
    }

    private lateinit var generalProperties: GeneralPropertiesView
    private lateinit var labelProperties: LabelPropertiesView
    private lateinit var legendProperties: LegendPropertiesView
    private lateinit var valueProperties: ValuePropertiesView
    private var toast: Toast? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.setChartId(pieChartViewModel.chartId)

        setFragmentResultListener(CurrencyDialogFragment.REQUEST_KEY) { _, bundle ->
            bundle.getString(CurrencyDialogFragment.CURRENCY_CODE)?.let { currencyCode ->
                viewModel.onValueCurrencyChanged(currencyCode)
                valueProperties.updateCurrencyView(currencyCode)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_pie_chart_properties, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        generalProperties = view.findViewById(R.id.general_properties)
        generalProperties.callback = viewModel

        labelProperties = view.findViewById(R.id.label_properties)
        labelProperties.callback = viewModel

        legendProperties = view.findViewById(R.id.legend_properties)
        legendProperties.callback = viewModel

        valueProperties = view.findViewById(R.id.value_properties)
        valueProperties.callback = viewModel

        viewModel.generalProperties.observe(viewLifecycleOwner, Observer { properties ->
            generalProperties.updateView(properties)
        })

        viewModel.labelProperties.observe(viewLifecycleOwner, Observer { properties ->
            labelProperties.updateView(properties)
        })

        viewModel.legendProperties.observe(viewLifecycleOwner, Observer { properties ->
            legendProperties.updateView(properties)
        })

        viewModel.valueProperties.observe(viewLifecycleOwner, Observer { properties ->
            valueProperties.updateView(properties)
        })

        viewModel.events.observe(viewLifecycleOwner, Observer { onEvent(it) })
    }

    private fun onEvent(event: Event) {
        when (event) {
            is ShowColorPicker ->
                showColorPicker(viewModelFactory)
            is ShowCurrencyPicker ->
                CurrencyDialogFragment.newInstance().show(parentFragmentManager, "currency_dialog")
            is MaximumDonutRadiusReached -> {
                generalProperties.updateDonutRadius(event.maximum)
                toast?.cancel()
                toast = showToast(R.string.maximum_value_reached)
            }
            is MinimumDonutRadiusReached -> {
                generalProperties.updateDonutRadius(event.minimum)
                toast?.cancel()
                toast = showToast(R.string.minimum_value_reached)
            }
            is MaximumSliceSpaceReached -> {
                generalProperties.updateSliceSpace(event.maximum)
                toast?.cancel()
                toast = showToast(R.string.maximum_value_reached)
            }
            is MinimumSliceSpaceReached -> {
                generalProperties.updateSliceSpace(event.minimum)
                toast?.cancel()
                toast = showToast(R.string.minimum_value_reached)
            }
        }
    }

    override fun onColorSelected(color: Int) {
        viewModel.onColorSelected(color)
    }
}