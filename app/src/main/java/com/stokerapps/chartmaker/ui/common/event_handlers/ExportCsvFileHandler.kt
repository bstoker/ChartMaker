/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.ui.common.event_handlers

import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import com.stokerapps.chartmaker.R
import com.stokerapps.chartmaker.domain.usecases.ExportCsvFiles
import com.stokerapps.chartmaker.ui.common.showSnackbar

class ExportCsvFileHandler(val fragment: Fragment) : EventHandler<ExportCsvFiles> {

    override fun handle(event: ExportCsvFiles) {
        val snackbar = fragment.showSnackbar(
            message = fragment.getString(R.string.importing_files_x_x, 0, event.total),
            duration = Snackbar.LENGTH_LONG
        )
        snackbar?.let {
            event.state.observe(fragment.viewLifecycleOwner, ExportCsvFilesObserver(event, it))
        }
    }

    private inner class ExportCsvFilesObserver(
        private val task: ExportCsvFiles,
        private val snackbar: Snackbar
    ) : Observer<ExportCsvFiles.State> {

        override fun onChanged(state: ExportCsvFiles.State?) {
            when (state) {
                is ExportCsvFiles.Running -> {
                    snackbar.setText(getMessage(state))
                }
                is ExportCsvFiles.Completed -> {
                    snackbar.setText(R.string.export_completed)
                    snackbar.setAction(R.string.dismiss) {
                        snackbar.dismiss()
                    }
                    task.state.removeObserver(this)
                }
            }
        }

        private fun getMessage(state: ExportCsvFiles.Running) =
            if (state.failures.isEmpty()) {
                fragment.getString(R.string.exporting_files_x_x, state.completed.size, state.total)
            } else {
                fragment.getString(
                    R.string.exporting_files_x_x_x_failed,
                    state.completed.size,
                    state.total,
                    state.failures.size
                )
            }
    }
}