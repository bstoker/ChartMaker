/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.ui.common.event_handlers

import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import com.stokerapps.chartmaker.R
import com.stokerapps.chartmaker.domain.usecases.ImportCsvFiles
import com.stokerapps.chartmaker.ui.common.showSnackbar

class ImportCsvFileHandler(val fragment: Fragment) : EventHandler<ImportCsvFiles> {

    override fun handle(event: ImportCsvFiles) {
        val snackbar = fragment.showSnackbar(
            message = fragment.getString(R.string.importing_files_x_x, 0, event.total),
            duration = Snackbar.LENGTH_LONG
        )
        snackbar?.let {
            event.state.observe(fragment.viewLifecycleOwner, ImportCsvFilesObserver(event, it))
        }
    }

    private inner class ImportCsvFilesObserver(
        private val task: ImportCsvFiles,
        private val snackbar: Snackbar
    ) : Observer<ImportCsvFiles.State> {

        override fun onChanged(state: ImportCsvFiles.State?) {
            when (state) {
                is ImportCsvFiles.Running -> {
                    snackbar.setText(getMessage(state))
                }
                is ImportCsvFiles.Completed -> {
                    snackbar.setText(R.string.import_completed)
                    snackbar.setAction(R.string.dismiss) {
                        snackbar.dismiss()
                    }
                    task.state.removeObserver(this)
                }
            }
        }

        private fun getMessage(state: ImportCsvFiles.Running) =
            if (state.failures.isEmpty()) {
                fragment.getString(R.string.importing_files_x_x, state.completed.size, state.total)
            } else {
                fragment.getString(
                    R.string.importing_files_x_x_x_failed,
                    state.completed.size,
                    state.total,
                    state.failures.size
                )
            }
    }
}