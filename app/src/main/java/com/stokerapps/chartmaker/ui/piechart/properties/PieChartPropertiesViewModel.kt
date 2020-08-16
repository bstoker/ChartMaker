/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.ui.piechart.properties

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.stokerapps.chartmaker.domain.*
import com.stokerapps.chartmaker.ui.common.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.*

class PieChartPropertiesViewModel(
    private val savedState: SavedStateHandle,
    private val applicationScope: CoroutineScope,
    private val repository: ChartRepository,
    private val editorRepository: EditorRepository
) : ViewModel(), GeneralPropertiesView.Callback, LabelPropertiesView.Callback,
    LegendPropertiesView.Callback,
    ValuePropertiesView.Callback {

    companion object {
        private const val PROPERTY_TYPE = "propertySection"
    }

    enum class PropertySection { GENERAL, LABELS, LEGEND, VALUES }

    private var propertySection: PropertySection?
        get() = savedState.get<String>(PROPERTY_TYPE)?.let { PropertySection.valueOf(it) }
        set(value) {
            value?.let { savedState.set(PROPERTY_TYPE, it.name) }
                ?: savedState.remove<Int>(PROPERTY_TYPE)
        }

    private var chart: PieChart? = null
    private var descriptionUpdateJob: Job? = null
    private var editor: Editor? = null

    val events = LiveEvent<Event>()

    private val chartIdStateFlow: MutableStateFlow<UUID?> = MutableStateFlow(null)

    private suspend fun getEditor() = editor ?: editorRepository.getEditor()

    private val editorFlow = editorRepository.getEditorFlow()
        .mapNotNull { it.getOrNull() }
        .onEach { editor = it }

    private val pieChartFlow = chartIdStateFlow
        .filterNotNull()
        .flatMapLatest { chartId ->
            repository.getPieChartFlow(chartId)
                .conflate()
                .mapNotNull { it.getOrNull() }
                .onEach { chart = it }
        }

    val generalProperties = pieChartFlow
        .combine(editorFlow) { chart, editor ->
            GeneralProperties(
                editor.isGeneralPropertySectionExpanded,
                chart.name,
                chart.descriptionTextStyleBold,
                chart.descriptionTextStyleItalic,
                chart.isDescriptionVisible,
                chart.descriptionFontName,
                chart.descriptionTextColor,
                chart.descriptionTextSize,
                chart.sliceSpace,
                chart.donutRadius,
                chart.isRotationEnabled
            )
        }
        .flowOn(Dispatchers.Default)
        .distinctUntilChanged()
        .asLiveData(viewModelScope.coroutineContext)

    val labelProperties = pieChartFlow
        .combine(editorFlow) { chart, editor ->
            LabelProperties(
                editor.isLabelPropertySectionExpanded,
                chart.labelTextStyleBold,
                chart.labelTextStyleItalic,
                chart.areLabelsVisible,
                chart.labelFontName,
                chart.labelTextColor,
                chart.labelTextSize
            )
        }
        .flowOn(Dispatchers.Default)
        .distinctUntilChanged()
        .asLiveData(viewModelScope.coroutineContext)

    val legendProperties = pieChartFlow
        .combine(editorFlow) { chart, editor ->
            LegendProperties(
                editor.isLegendPropertySectionExpanded,
                chart.legendTextStyleBold,
                chart.legendTextStyleItalic,
                chart.isLegendVisible,
                chart.legendFontName,
                chart.legendTextColor,
                chart.legendTextSize
            )
        }
        .flowOn(Dispatchers.Default)
        .distinctUntilChanged()
        .asLiveData(viewModelScope.coroutineContext)

    val valueProperties = pieChartFlow
        .combine(editorFlow) { chart, editor ->
            ValueProperties(
                editor.isValuePropertySectionExpanded,
                chart.valueTextStyleBold,
                chart.valueTextStyleItalic,
                chart.areValuesVisible,
                chart.currencyCode,
                chart.valueDecimals,
                chart.valueFontName,
                chart.valueTextColor,
                chart.valueTextSize,
                chart.valueType
            )
        }
        .flowOn(Dispatchers.Default)
        .distinctUntilChanged()
        .asLiveData(viewModelScope.coroutineContext)

    fun setChartId(chartId: UUID?) {
        chartIdStateFlow.value = chartId
    }

    fun onColorSelected(color: Int) {
        when (propertySection) {
            PropertySection.GENERAL -> updateDescriptionTextColor(color)
            PropertySection.LEGEND -> updateLegendTextColor(color)
            PropertySection.LABELS -> updateLabelTextColor(color)
            PropertySection.VALUES -> updateValueTextColor(color)
        }
    }

    private fun updateDescriptionTextColor(color: Int) {
        chart?.let { chart ->
            applicationScope.launch {
                if (chart.descriptionTextColor != color) {
                    repository.store(
                        chart.update(
                            descriptionTextColor = color
                        )
                    )
                }
            }
        }
    }

    private fun updateLegendTextColor(color: Int) {
        chart?.let { chart ->
            applicationScope.launch {
                if (chart.legendTextColor != color) {
                    repository.store(
                        chart.update(legendTextColor = color)
                    )
                }
            }
        }
    }

    private fun updateLabelTextColor(color: Int) {
        chart?.let { chart ->
            applicationScope.launch {
                if (chart.labelTextColor != color) {
                    repository.store(
                        chart.update(labelTextColor = color)
                    )
                }
            }
        }
    }

    private fun updateValueTextColor(color: Int) {
        chart?.let { chart ->
            applicationScope.launch {
                if (chart.valueTextColor != color) {
                    repository.store(
                        chart.update(valueTextColor = color)
                    )
                }
            }
        }
    }

    override fun onValueClearFormatClicked() {
        chart?.let { chart ->
            applicationScope.launch {
                if (chart.valueFontName != Fonts.DEFAULT_FONT_NAME
                    || chart.valueTextColor != WHITE
                    || chart.valueTextSize != Fonts.DEFAULT_TEXT_SIZE
                    || chart.valueTextStyleBold
                    || chart.valueTextStyleItalic
                ) {
                    repository.store(
                        chart.update(
                            valueFontName = Fonts.DEFAULT_FONT_NAME,
                            valueTextColor = WHITE,
                            valueTextSize = Fonts.DEFAULT_TEXT_SIZE,
                            valueTextStyleBold = false,
                            valueTextStyleItalic = false
                        )
                    )
                }
            }
        }
    }

    override fun onValueCurrencyClicked() {
        events.value = ShowCurrencyPicker
    }

    override fun onValueCurrencyChanged(currencyCode: String) {
        chart?.let { chart ->
            applicationScope.launch {
                if (chart.currencyCode != currencyCode) {
                    repository.store(
                        chart.update(
                            currencyCode = currencyCode
                        )
                    )
                }
            }
        }
    }

    override fun onValueDecimalsChanged(decimals: Int) {
        chart?.let { chart ->
            applicationScope.launch {
                if (chart.valueDecimals != decimals) {
                    repository.store(
                        chart.update(
                            valueDecimals = decimals
                        )
                    )
                }
            }
        }
    }

    override fun onValueExpandChanged(isExpanded: Boolean) {
        applicationScope.launch {
            getEditor().let { editor ->
                if (editor.isValuePropertySectionExpanded != isExpanded) {
                    editorRepository.store(
                        editor.copy(isValuePropertySectionExpanded = isExpanded)
                    )
                }
            }
        }
    }

    override fun onValueFontChanged(name: String) {
        chart?.let { chart ->
            applicationScope.launch {
                if (chart.valueFontName != name) {
                    repository.store(
                        chart.update(
                            valueFontName = name
                        )
                    )
                }
            }
        }
    }

    override fun onValueTextColorClicked() {
        propertySection = PropertySection.VALUES
        events.value = ShowColorPicker
    }

    override fun onValueTextSizeChanged(size: Float) {
        chart?.let { chart ->
            applicationScope.launch {
                if (chart.valueTextSize != size) {
                    repository.store(
                        chart.update(
                            valueTextSize = size
                        )
                    )
                }
            }
        }
    }

    override fun onValueTextStyleBoldChanged(isBold: Boolean) {
        chart?.let { chart ->
            applicationScope.launch {
                if (chart.valueTextStyleBold != isBold) {
                    repository.store(
                        chart.update(
                            valueTextStyleBold = isBold
                        )
                    )
                }
            }
        }
    }

    override fun onValueTextStyleItalicChanged(isItalic: Boolean) {
        chart?.let { chart ->
            applicationScope.launch {
                if (chart.valueTextStyleItalic != isItalic) {
                    repository.store(
                        chart.update(
                            valueTextStyleItalic = isItalic
                        )
                    )
                }
            }
        }
    }

    override fun onValueTypeChanged(type: ValueType) {
        chart?.let { chart ->
            applicationScope.launch {
                if (chart.valueType != type) {
                    repository.store(
                        chart.update(
                            valueType = type
                        )
                    )
                }
            }
        }
    }

    override fun onValueVisibilityClicked() {
        chart?.let { chart ->
            applicationScope.launch {
                repository.store(
                    chart.update(
                        areValuesVisible = !chart.areValuesVisible
                    )
                )
            }
        }
    }

    override fun onLegendClearFormatClicked() {
        chart?.let { chart ->
            applicationScope.launch {
                if (chart.legendFontName != Fonts.DEFAULT_FONT_NAME
                    || chart.legendTextColor != null
                    || chart.legendTextSize != Fonts.DEFAULT_TEXT_SIZE
                    || chart.legendTextStyleBold
                    || chart.legendTextStyleItalic
                ) {
                    repository.store(
                        chart.update(
                            legendFontName = Fonts.DEFAULT_FONT_NAME,
                            legendTextColor = null,
                            legendTextSize = Fonts.DEFAULT_TEXT_SIZE,
                            legendTextStyleBold = false,
                            legendTextStyleItalic = false
                        )
                    )
                }
            }
        }
    }

    override fun onLegendExpandChanged(isExpanded: Boolean) {
        applicationScope.launch {
            getEditor().let { editor ->
                if (editor.isLegendPropertySectionExpanded != isExpanded) {
                    editorRepository.store(
                        editor.copy(isLegendPropertySectionExpanded = isExpanded)
                    )
                }
            }
        }
    }

    override fun onLegendFontChanged(name: String) {
        chart?.let { chart ->
            applicationScope.launch {
                if (chart.legendFontName != name) {
                    repository.store(
                        chart.update(
                            legendFontName = name
                        )
                    )
                }
            }
        }
    }

    override fun onLegendTextColorClicked() {
        propertySection = PropertySection.LEGEND
        events.value =
            ShowColorPicker
    }

    override fun onLegendTextSizeChanged(size: Float) {
        chart?.let { chart ->
            applicationScope.launch {
                if (chart.legendTextSize != size) {
                    repository.store(
                        chart.update(
                            legendTextSize = size
                        )
                    )
                }
            }
        }
    }

    override fun onLegendTextStyleBoldChanged(isBold: Boolean) {
        chart?.let { chart ->
            applicationScope.launch {
                if (chart.legendTextStyleBold != isBold) {
                    repository.store(
                        chart.update(
                            legendTextStyleBold = isBold
                        )
                    )
                }
            }
        }
    }

    override fun onLegendTextStyleItalicChanged(isItalic: Boolean) {
        chart?.let { chart ->
            applicationScope.launch {
                if (chart.legendTextStyleItalic != isItalic) {
                    repository.store(
                        chart.update(
                            legendTextStyleItalic = isItalic
                        )
                    )
                }
            }
        }
    }

    override fun onLegendVisibilityClicked() {
        chart?.let { chart ->
            applicationScope.launch {
                repository.store(
                    chart.update(
                        isLegendVisible = !chart.isLegendVisible
                    )
                )
            }
        }
    }

    override fun onLabelClearFormatClicked() {
        chart?.let { chart ->
            applicationScope.launch {
                if (chart.labelFontName != Fonts.DEFAULT_FONT_NAME
                    || chart.labelTextColor != WHITE
                    || chart.labelTextSize != Fonts.DEFAULT_TEXT_SIZE
                    || chart.labelTextStyleBold
                    || chart.labelTextStyleItalic
                ) {
                    repository.store(
                        chart.update(
                            labelFontName = Fonts.DEFAULT_FONT_NAME,
                            labelTextColor = WHITE,
                            labelTextSize = Fonts.DEFAULT_TEXT_SIZE,
                            labelTextStyleBold = false,
                            labelTextStyleItalic = false
                        )
                    )
                }
            }
        }
    }

    override fun onLabelExpandChanged(isExpanded: Boolean) {
        applicationScope.launch {
            getEditor().let { editor ->
                if (editor.isLabelPropertySectionExpanded != isExpanded) {
                    editorRepository.store(
                        editor.copy(isLabelPropertySectionExpanded = isExpanded)
                    )
                }
            }
        }
    }

    override fun onLabelFontChanged(name: String) {
        chart?.let { chart ->
            applicationScope.launch {
                if (chart.labelFontName != name) {
                    repository.store(
                        chart.update(
                            labelFontName = name
                        )
                    )
                }
            }
        }
    }

    override fun onLabelTextColorClicked() {
        propertySection = PropertySection.LABELS
        events.value =
            ShowColorPicker
    }

    override fun onLabelTextSizeChanged(size: Float) {
        chart?.let { chart ->
            applicationScope.launch {
                if (chart.labelTextSize != size) {
                    repository.store(
                        chart.update(
                            labelTextSize = size
                        )
                    )
                }
            }
        }
    }

    override fun onLabelTextStyleBoldChanged(isBold: Boolean) {
        chart?.let { chart ->
            applicationScope.launch {
                if (chart.labelTextStyleBold != isBold) {
                    repository.store(
                        chart.update(
                            labelTextStyleBold = isBold
                        )
                    )
                }
            }
        }
    }

    override fun onLabelTextStyleItalicChanged(isItalic: Boolean) {
        chart?.let { chart ->
            applicationScope.launch {
                if (chart.labelTextStyleItalic != isItalic) {
                    repository.store(
                        chart.update(
                            labelTextStyleItalic = isItalic
                        )
                    )
                }
            }
        }
    }

    override fun onLabelVisibilityClicked() {
        chart?.let { chart ->
            applicationScope.launch {
                repository.store(
                    chart.update(
                        areLabelsVisible = !chart.areLabelsVisible
                    )
                )
            }
        }
    }

    override fun onDescriptionClearFormatClicked() {
        chart?.let { chart ->
            applicationScope.launch {
                if (chart.descriptionFontName != Fonts.DEFAULT_FONT_NAME
                    || chart.descriptionTextColor != WHITE
                    || chart.descriptionTextSize != Fonts.DEFAULT_TEXT_SIZE
                    || chart.descriptionTextStyleBold
                    || chart.descriptionTextStyleItalic
                ) {
                    repository.store(
                        chart.update(
                            descriptionFontName = Fonts.DEFAULT_FONT_NAME,
                            descriptionTextColor = WHITE,
                            descriptionTextSize = Fonts.DEFAULT_TEXT_SIZE,
                            descriptionTextStyleBold = false,
                            descriptionTextStyleItalic = false
                        )
                    )
                }
            }
        }
    }

    override fun onGeneralExpandChanged(isExpanded: Boolean) {
        applicationScope.launch {
            getEditor().let { editor ->
                if (editor.isGeneralPropertySectionExpanded != isExpanded) {
                    editorRepository.store(
                        editor.copy(isGeneralPropertySectionExpanded = isExpanded)
                    )
                }
            }
        }
    }

    override fun onDescriptionChanged(description: String) {
        descriptionUpdateJob?.cancel()
        descriptionUpdateJob = applicationScope.launch {
            chart?.let { chart ->
                delay(1000L)
                if (chart.name != description) {
                    repository.store(
                        chart.update(name = description)
                    )
                }
            }
        }
    }

    override fun onDescriptionFontChanged(name: String) {
        chart?.let { chart ->
            applicationScope.launch {
                if (chart.descriptionFontName != name) {
                    repository.store(
                        chart.update(descriptionFontName = name)
                    )
                }
            }
        }
    }

    override fun onDescriptionTextColorClicked() {
        propertySection = PropertySection.GENERAL
        events.value =
            ShowColorPicker
    }

    override fun onDescriptionTextSizeChanged(size: Float) {
        chart?.let { chart ->
            applicationScope.launch {
                if (chart.descriptionTextSize != size) {
                    repository.store(
                        chart.update(descriptionTextSize = size)
                    )
                }
            }
        }
    }

    override fun onDescriptionTextStyleBoldChanged(isBold: Boolean) {
        chart?.let { chart ->
            applicationScope.launch {
                if (chart.descriptionTextStyleBold != isBold) {
                    repository.store(
                        chart.update(descriptionTextStyleBold = isBold)
                    )
                }
            }
        }
    }

    override fun onDescriptionTextStyleItalicChanged(isItalic: Boolean) {
        chart?.let { chart ->
            applicationScope.launch {
                if (chart.descriptionTextStyleItalic != isItalic) {
                    repository.store(
                        chart.update(descriptionTextStyleItalic = isItalic)
                    )
                }
            }
        }
    }

    override fun onDescriptionVisibilityClicked() {
        chart?.let { chart ->
            applicationScope.launch {
                repository.store(
                    chart.update(isDescriptionVisible = !chart.isDescriptionVisible)
                )
            }
        }
    }

    override fun onDonutRadiusChanged(donutRadius: Float) {
        val newDonutRadius = when {
            donutRadius < PieChart.MIN_DONUT_RADIUS -> {
                events.value =
                    MinimumDonutRadiusReached(
                        PieChart.MIN_DONUT_RADIUS
                    )
                PieChart.MIN_DONUT_RADIUS
            }
            donutRadius > PieChart.MAX_DONUT_RADIUS -> {
                events.value =
                    MaximumDonutRadiusReached(
                        PieChart.MAX_DONUT_RADIUS
                    )
                PieChart.MAX_DONUT_RADIUS
            }
            else -> donutRadius
        }

        chart?.let { chart ->
            applicationScope.launch {
                if (chart.donutRadius != newDonutRadius) {
                    repository.store(
                        chart.update(donutRadius = newDonutRadius)
                    )
                }
            }
        }
    }

    override fun onDonutRadiusDecrement() {
        chart?.let { onDonutRadiusChanged(it.donutRadius - 1) }
    }

    override fun onDonutRadiusIncrement() {
        chart?.let { onDonutRadiusChanged(it.donutRadius + 1) }
    }

    override fun onShowDescriptionInCenterChanged(isEnabled: Boolean) {
        chart?.let { chart ->
            applicationScope.launch {
                if (chart.isDescriptionVisible != isEnabled) {
                    repository.store(
                        chart.update(isDescriptionVisible = isEnabled)
                    )
                }
            }
        }
    }

    override fun onSliceSpaceChanged(sliceSpace: Float) {
        val newSliceSpace = when {
            sliceSpace < PieChart.MIN_SLICE_SPACE -> {
                events.value =
                    MinimumSliceSpaceReached(
                        PieChart.MIN_SLICE_SPACE
                    )
                PieChart.MIN_SLICE_SPACE
            }
            sliceSpace > PieChart.MAX_SLICE_SPACE -> {
                events.value =
                    MaximumSliceSpaceReached(
                        PieChart.MAX_SLICE_SPACE
                    )
                PieChart.MAX_SLICE_SPACE
            }
            else -> sliceSpace
        }

        chart?.let { chart ->
            applicationScope.launch {
                if (chart.sliceSpace != newSliceSpace) {
                    repository.store(
                        chart.update(sliceSpace = newSliceSpace)
                    )
                }
            }
        }
    }

    override fun onSliceSpaceDecrement() {
        chart?.let { onSliceSpaceChanged(it.sliceSpace - 1) }
    }

    override fun onSliceSpaceIncrement() {
        chart?.let { onSliceSpaceChanged(it.sliceSpace + 1) }
    }
}