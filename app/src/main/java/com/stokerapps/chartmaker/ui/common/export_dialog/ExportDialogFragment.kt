/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.ui.common.export_dialog

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.ParcelUuid
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import com.stokerapps.chartmaker.R
import com.stokerapps.chartmaker.common.Timestamp
import com.stokerapps.chartmaker.databinding.DialogExportBinding
import com.stokerapps.chartmaker.domain.Delimiter
import com.stokerapps.chartmaker.domain.usecases.ExportCsvFiles
import com.stokerapps.chartmaker.ui.common.dp
import com.stokerapps.chartmaker.ui.common.viewBinding
import timber.log.Timber
import java.io.File
import java.util.*

class ExportDialogFragment(val viewModelFactory: ViewModelProvider.Factory) :
    DialogFragment(R.layout.dialog_export), ExportAdapter.Callback {

    companion object {

        const val TAG = "ExportDialog"
        private const val IDS = "chartIds"
        private const val FILENAME = "filename"
        const val SELECTED = "selected"

        fun newInstance(
            viewModelFactory: ViewModelProvider.Factory,
            ids: List<UUID>,
            filename: String? = ""
        ) =
            ExportDialogFragment(viewModelFactory).apply {
                arguments = Bundle().apply {
                    putParcelableArray(IDS, ids.map { id -> ParcelUuid(id) }.toTypedArray())
                    putString(FILENAME, filename)
                }
            }
    }

    private val adapter = ExportAdapter(this)
    private val binding by viewBinding(DialogExportBinding::bind)
    private val viewModel: ExportViewModel by activityViewModels { viewModelFactory }

    private val chartIds by lazy {
        requireArguments().getParcelableArray(IDS)
            ?.mapNotNull { (it as? ParcelUuid)?.uuid }
            ?: emptyList()
    }
    private val delimiter
        get() = when (selected) {
            SaveAs.CSV -> Delimiter.COMMA
            SaveAs.TSV -> Delimiter.TAB
        }
    private val extension
        get() = when (selected) {
            SaveAs.TSV -> ExportCsvFiles.EXTENSION_TSV
            else -> ExportCsvFiles.EXTENSION_CSV
        }
    private val filename by lazy { requireArguments().getString(FILENAME) }
    private val mimeType
        get() = when (selected) {
            SaveAs.TSV -> ExportCsvFiles.MIME_TYPE_TSV
            else -> ExportCsvFiles.MIME_TYPE_CSV
        }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private val createDocument = registerForActivityResult(CreateDocument()) { uri ->
        uri?.let {
            save(it)
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private val createDocuments = registerForActivityResult(CreateDocuments()) { uri ->
        uri?.let {
            save(it)
        }
    }

    private lateinit var selected: SaveAs
    private var exportMultiple: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exportMultiple = arguments?.let { args ->
            args.getParcelableArray(IDS)?.let { ids ->
                ids.size > 1
            }
        } ?: false

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
            adapter = this@ExportDialogFragment.adapter
        }
        adapter.selected = selected

        binding.save.setOnClickListener { onSavePressed() }

        viewModel.events.observe(viewLifecycleOwner) { onEvent(it) }
    }

    override fun onResume() {
        super.onResume()
        setDialogSize()
    }

    private fun onEvent(event: Any) {
        when (event) {
            is ExportCsvFiles -> dismiss()
        }
    }

    private fun setDialogSize() {
        val width = 320.dp
        val height = ViewGroup.LayoutParams.WRAP_CONTENT
        dialog?.window?.setLayout(width, height)
    }

    override fun onItemClicked(item: SaveAs) {
        adapter.selected = item
        selected = item
    }

    private fun onSavePressed() =
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> saveApi21()
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT -> saveApi19()
            else -> saveToCommonDirectory()
        }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun saveApi21() {
        try {
            if (exportMultiple) {
                createDocuments.launch(
                    CreateDocuments.Args(
                        mimeType,
                        Intent.CATEGORY_OPENABLE
                    )
                )
            } else {
                createDocument.launch(
                    CreateDocument.Args(
                        createFilename(),
                        mimeType,
                        Intent.CATEGORY_OPENABLE
                    )
                )
            }
        } catch (e: Exception) {
            Timber.e(e)
            saveToCommonDirectory()
        }
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private fun saveApi19() {
        try {
            if (exportMultiple) {
                saveToCommonDirectory()

            } else {
                createDocument.launch(
                    CreateDocument.Args(
                        createFilename(),
                        mimeType,
                        Intent.CATEGORY_OPENABLE
                    )
                )
            }
        } catch (e: ActivityNotFoundException) {
            Timber.e(e)
            saveToCommonDirectory()
        }
    }

    @Suppress("DEPRECATION")
    private fun saveToCommonDirectory() {
        val downloadDirectory = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT -> {
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            }
            else -> {
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            }
        }
        val uri = Uri.parse(
            if (exportMultiple) {
                downloadDirectory.absolutePath
            } else {
                downloadDirectory.absolutePath + File.separator + createFilename()
            }
        )
        save(uri)
    }

    private fun save(uri: Uri) {
        viewModel.exportCsvFiles(uri, chartIds, delimiter)
    }

    private fun createFilename(): String {
        val chartName = filename
        val filename = when {
            chartName.isNullOrEmpty() -> "${getString(R.string.chart)}_$Timestamp"
            else -> chartName
        }
        return "$filename$extension"
    }
}