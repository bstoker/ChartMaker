/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.ui

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.stokerapps.chartmaker.domain.ChartRepository
import com.stokerapps.chartmaker.domain.EditorRepository
import com.stokerapps.chartmaker.ui.common.color_picker.ColorPickerDialogFragment
import com.stokerapps.chartmaker.ui.common.save_dialog.SaveDialogFragment
import com.stokerapps.chartmaker.ui.common.save_dialog.csv.SaveAsCsvFragment
import com.stokerapps.chartmaker.ui.explorer.ExplorerFragment
import com.stokerapps.chartmaker.ui.new_chart.NewChartFragment
import com.stokerapps.chartmaker.ui.piechart.PieChartEditFragment
import com.stokerapps.chartmaker.ui.piechart.PieChartEntryEditDialogFragment
import com.stokerapps.chartmaker.ui.piechart.PieChartFragment
import com.stokerapps.chartmaker.ui.piechart.entries.PieChartEntriesFragment
import com.stokerapps.chartmaker.ui.piechart.properties.PieChartPropertiesFragment
import kotlinx.coroutines.CoroutineScope
import androidx.fragment.app.FragmentFactory as AndroidFragmentFactory

@Suppress("unused")
class FragmentFactory(
    private val applicationScope: CoroutineScope,
    private val chartRepository: ChartRepository,
    private val editorRepository: EditorRepository,
    private val viewModelFactory: ViewModelProvider.Factory
) : AndroidFragmentFactory() {

    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        return when (className) {
            ColorPickerDialogFragment::class.java.name ->
                ColorPickerDialogFragment(viewModelFactory)
            ExplorerFragment::class.java.name ->
                ExplorerFragment(viewModelFactory)
            NewChartFragment::class.java.name ->
                NewChartFragment(viewModelFactory)
            PieChartFragment::class.java.name ->
                PieChartFragment(
                    applicationScope,
                    chartRepository,
                    editorRepository,
                    viewModelFactory
                )
            PieChartEditFragment::class.java.name ->
                PieChartEditFragment(
                    applicationScope,
                    chartRepository,
                    editorRepository
                )
            PieChartEntriesFragment::class.java.name ->
                PieChartEntriesFragment(
                    applicationScope,
                    chartRepository,
                    editorRepository,
                    viewModelFactory
                )
            PieChartEntryEditDialogFragment::class.java.name ->
                PieChartEntryEditDialogFragment(
                    applicationScope,
                    chartRepository,
                    editorRepository,
                    viewModelFactory
                )
            PieChartPropertiesFragment::class.java.name ->
                PieChartPropertiesFragment(
                    applicationScope,
                    chartRepository,
                    editorRepository,
                    viewModelFactory
                )
            SaveAsCsvFragment::class.java.name ->
                SaveAsCsvFragment(
                    viewModelFactory
                )
            SaveDialogFragment::class.java.name ->
                SaveDialogFragment(viewModelFactory)
            else -> super.instantiate(classLoader, className)
        }
    }
}