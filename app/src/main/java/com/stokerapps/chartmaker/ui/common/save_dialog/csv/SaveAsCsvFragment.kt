/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.ui.common.save_dialog.csv

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.View
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.stokerapps.chartmaker.R
import com.stokerapps.chartmaker.common.Timestamp
import com.stokerapps.chartmaker.databinding.FragmentSaveAsCsvBinding
import com.stokerapps.chartmaker.ui.common.save_dialog.CreateDocument
import com.stokerapps.chartmaker.ui.common.save_dialog.SaveViewModel
import com.stokerapps.chartmaker.ui.common.viewBinding
import java.io.File


class SaveAsCsvFragment(viewModelFactory: ViewModelProvider.Factory) :
    Fragment(R.layout.fragment_save_as_csv) {

    companion object {
        const val DELIMITER = "delimiter"
    }

    private val args by navArgs<SaveAsCsvFragmentArgs>()
    private val binding by viewBinding(FragmentSaveAsCsvBinding::bind)
    private val viewModel: SaveViewModel by activityViewModels { viewModelFactory }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private val createDocument = registerForActivityResult(CreateDocument()) { uri ->
        uri?.let {
            save(it)
        }
    }

    private lateinit var delimiter: Delimiter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        delimiter = savedInstanceState?.let {
            Delimiter.valueOf(it.getString(DELIMITER, Delimiter.COMMA.toString()))
        } ?: Delimiter.COMMA
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(DELIMITER, delimiter.toString())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            save.setOnClickListener { onSavePressed() }

            comma.setOnClickListener { onDelimiterSelected(Delimiter.COMMA) }
            semicolon.setOnClickListener { onDelimiterSelected(Delimiter.SEMICOLON) }
            colon.setOnClickListener { onDelimiterSelected(Delimiter.COLON) }
            space.setOnClickListener { onDelimiterSelected(Delimiter.SPACE) }
            tab.setOnClickListener { onDelimiterSelected(Delimiter.TAB) }
        }
        updateView(delimiter)
    }

    private fun onDelimiterSelected(delimiter: Delimiter) {
        this.delimiter = delimiter
        updateView(delimiter)
    }

    private fun updateView(delimiter: Delimiter) {
        with(binding) {
            comma.isSelected = delimiter == Delimiter.COMMA
            semicolon.isSelected = delimiter == Delimiter.SEMICOLON
            colon.isSelected = delimiter == Delimiter.COLON
            space.isSelected = delimiter == Delimiter.SPACE
            tab.isSelected = delimiter == Delimiter.TAB
        }
    }

    private fun onSavePressed() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            saveApi19()

        } else {
            saveToCommonDirectory()
        }
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private fun saveApi19() {
        try {
            createDocument.launch(
                CreateDocument.Args(
                    createFilename(),
                    "text/csv",
                    Intent.CATEGORY_OPENABLE
                )
            )
        } catch (e: ActivityNotFoundException) {
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
        val uri = Uri.parse(downloadDirectory.absolutePath + File.separator + createFilename())
        save(uri)
    }

    private fun save(uri: Uri) {
        viewModel.saveAsCsv(uri, args.chartId.uuid, delimiter)
    }

    private fun createFilename(): String {
        val name = args.filename ?: "${getString(R.string.chart)}_$Timestamp"
        return "$name.csv"
    }
}