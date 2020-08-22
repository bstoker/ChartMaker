/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.ui.common.save_dialog

import android.os.Bundle
import android.os.ParcelUuid
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.get
import androidx.recyclerview.widget.LinearLayoutManager
import com.stokerapps.chartmaker.R
import com.stokerapps.chartmaker.databinding.DialogSaveAsBinding
import com.stokerapps.chartmaker.ui.common.*
import com.stokerapps.chartmaker.ui.common.save_dialog.csv.SaveAsCsvFragmentArgs
import java.util.*

class SaveDialogFragment(val viewModelFactory: ViewModelProvider.Factory) :
    DialogFragment(R.layout.dialog_save_as), SaveAsAdapter.Callback {

    companion object {

        const val TAG = "SaveAsDialog"
        private const val ID = "chartId"
        private const val FILENAME = "filename"
        const val SELECTED = "selected"

        fun newInstance(viewModelFactory: ViewModelProvider.Factory, id: UUID, filename: String?) =
            SaveDialogFragment(viewModelFactory).apply {
                arguments = Bundle().apply {
                    putParcelable(ID, ParcelUuid(id))
                    putString(FILENAME, filename)
                }
            }
    }

    private val adapter = SaveAsAdapter(this)
    private val binding by viewBinding(DialogSaveAsBinding::bind)
    private val viewModel: SaveViewModel by activityViewModels { viewModelFactory }

    private val navHostFragment by lazy { childFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment }
    private val navController by lazy { navHostFragment.navController }

    private lateinit var selected: SaveAs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        selected = savedInstanceState?.let {
            SaveAs.valueOf(savedInstanceState.getString(SELECTED, SaveAs.CSV.toString()))
        } ?: SaveAs.CSV
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SELECTED, selected.toString())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding.exportOptions) {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = this@SaveDialogFragment.adapter
        }

        adapter.selected = selected

        arguments?.let { args ->
            SaveAsCsvFragmentArgs.fromBundle(args)
        }

        navController.setGraph(R.navigation.navigation_graph_save_as, arguments)

        viewModel.events.observe(viewLifecycleOwner) { onEvent(it) }
    }

    override fun onResume() {
        super.onResume()
        setDialogSize()
    }

    private fun onEvent(event: Event) {
        when (event) {
            is SaveChart -> dismiss()
        }
    }

    private fun setDialogSize() {
        val width = if (isWideScreen()) 530.dp else 320.dp
        val height = ViewGroup.LayoutParams.WRAP_CONTENT
        dialog?.window?.setLayout(width, height)
    }

    override fun onItemClicked(item: SaveAs) {
        when (item) {
            SaveAs.CSV -> {
                navController.let {
                    if (it.currentDestination != it.graph[R.id.save_as_csv]) {
                        navController.navigate(R.id.save_as_csv, arguments)
                    }
                }
            }
        }
        adapter.selected = item
        selected = item
    }
}