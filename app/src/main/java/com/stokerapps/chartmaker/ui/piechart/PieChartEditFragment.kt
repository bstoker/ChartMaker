/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.ui.piechart

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.stokerapps.chartmaker.R
import com.stokerapps.chartmaker.databinding.FragmentPieChartEditBinding
import com.stokerapps.chartmaker.domain.ChartRepository
import com.stokerapps.chartmaker.domain.Editor
import com.stokerapps.chartmaker.domain.EditorRepository
import com.stokerapps.chartmaker.ui.SavedStateViewModelFactory
import com.stokerapps.chartmaker.ui.common.viewBinding
import kotlinx.coroutines.CoroutineScope

class PieChartEditFragment(
    applicationScope: CoroutineScope,
    chartRepository: ChartRepository,
    editorRepository: EditorRepository
) : Fragment(R.layout.fragment_pie_chart_edit) {

    private val binding by viewBinding(FragmentPieChartEditBinding::bind)
    private val viewModel: PieChartViewModel by activityViewModels {
        SavedStateViewModelFactory(
            requireActivity(),
            applicationScope,
            chartRepository,
            editorRepository
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bottomNavigationView = binding.navView
        val navHostFragment: View = view.findViewById(R.id.nav_host_fragment)
        val navController = navHostFragment.findNavController()
        bottomNavigationView.setupWithNavController(navController)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            viewModel.setSelectedTab(findSelectedTab(item.itemId))
            NavigationUI.onNavDestinationSelected(item, navController)
        }

        viewModel.observableEditor.observe(viewLifecycleOwner) { editor ->
            val selectedItem = findSelectedItemId(editor.selectedEditTab)
            if (selectedItem != bottomNavigationView.selectedItemId) {
                bottomNavigationView.selectedItemId = selectedItem
            }
        }
    }

    private fun findSelectedTab(itemId: Int) =
        when (itemId) {
            R.id.collaboration -> Editor.EditTab.COLLABORATION
            R.id.entries -> Editor.EditTab.ENTRIES
            R.id.properties -> Editor.EditTab.PROPERTIES
            else -> Editor.EditTab.ENTRIES
        }

    private fun findSelectedItemId(selectedTab: Editor.EditTab) =
        when (selectedTab) {
            Editor.EditTab.COLLABORATION -> R.id.collaboration
            Editor.EditTab.ENTRIES -> R.id.entries
            Editor.EditTab.PROPERTIES -> R.id.properties
        }
}