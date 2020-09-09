/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.ui.piechart

import android.content.DialogInterface
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.stokerapps.chartmaker.R
import com.stokerapps.chartmaker.databinding.DialogEntryEditBinding
import com.stokerapps.chartmaker.domain.ChartRepository
import com.stokerapps.chartmaker.domain.EditorRepository
import com.stokerapps.chartmaker.domain.PieChartEntry
import com.stokerapps.chartmaker.ui.SavedStateViewModelFactory
import com.stokerapps.chartmaker.ui.common.*
import com.stokerapps.chartmaker.ui.common.color_picker.ColorPickerDialogFragment
import kotlinx.coroutines.CoroutineScope
import java.text.DecimalFormat


class PieChartEntryEditDialogFragment(
    private val applicationScope: CoroutineScope,
    private val chartRepository: ChartRepository,
    private val editorRepository: EditorRepository,
    private val viewModelFactory: ViewModelProvider.Factory
) : DialogFragment(R.layout.dialog_entry_edit), ColorPickerDialogFragment.Callback {

    companion object {

        private const val ENTRY = "entry"

        fun newInstance(
            applicationScope: CoroutineScope,
            chartRepository: ChartRepository,
            editorRepository: EditorRepository,
            viewModelFactory: ViewModelProvider.Factory,
            entry: PieChartEntry
        ): PieChartEntryEditDialogFragment {
            val args = Bundle().apply {
                putParcelable(ENTRY, ParcelPieChartEntry(entry))
            }
            return PieChartEntryEditDialogFragment(
                applicationScope,
                chartRepository,
                editorRepository,
                viewModelFactory
            ).apply { arguments = args }
        }
    }

    private val callback by lazy {
        if (parentFragment is DialogInterface.OnCancelListener)
            parentFragment as DialogInterface.OnCancelListener else null
    }

    private val binding by viewBinding(DialogEntryEditBinding::bind)
    private val viewModel: PieChartViewModel by activityViewModels {
        SavedStateViewModelFactory(
            requireActivity(),
            applicationScope,
            chartRepository,
            editorRepository
        )
    }

    private lateinit var originalEntry: PieChartEntry
    private lateinit var savedEntry: PieChartEntry

    private fun getEntryFromInput() = savedEntry.copy(
        label = binding.label.text?.toString() ?: "",
        value = binding.value.text?.toString()?.toFloatOrNull() ?: 0f,
        color = binding.color.getColor()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        originalEntry = requireArguments().getParcelable<ParcelPieChartEntry>(ENTRY)!!.entry
        savedEntry = savedInstanceState?.getParcelable<ParcelPieChartEntry>(ENTRY)?.entry
            ?: originalEntry

        activity?.currentFocus?.clearFocus()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(ENTRY, ParcelPieChartEntry(getEntryFromInput()))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {

            color.setColor(savedEntry.color)
            color.setOnClickListener {
                showColorPicker(viewModelFactory)
            }

            done.setOnClickListener { onDonePressed() }

            val valueText = DecimalFormat("#.##").format(savedEntry.value)
            value.text = SpannableStringBuilder(valueText)
            if (isPortraitMode()) {
                value.requestFocus()
                showKeyboard(value)
            }

            label.text = SpannableStringBuilder(savedEntry.label)
            label.setSelection(label.text.length)
            label.setOnKeyboardDonePress { done.callOnClick() }
        }

        viewModel.viewState.observe(viewLifecycleOwner) { onStateChanged(it) }
    }

    override fun onResume() {
        super.onResume()
        setDialogSize()
    }

    private fun setDialogSize() {
        val width = resources.getDimensionPixelSize(R.dimen.popup_width)
        val height = ViewGroup.LayoutParams.WRAP_CONTENT
        dialog?.window?.setLayout(width, height)
    }

    private fun onStateChanged(state: ViewState) {
        val entryHasBeenModifiedOrDeleted = when (state) {
            is Loaded -> state.chart.entries.contains(originalEntry).not()
            else -> true
        }
        if (entryHasBeenModifiedOrDeleted) {
            showSnackbar(R.string.entry_has_been_modified_or_deleted)
            dismiss()
        }
    }

    private fun onDonePressed() {
        val newEntry = getEntryFromInput()
        if (newEntry == originalEntry) {
            callback?.onCancel(dialog)
        } else {
            viewModel.update(newEntry)
        }
        dismiss()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        callback?.onCancel(dialog)
    }

    private var mShowImeRunnable: Runnable? = null
    private fun showKeyboard(view: View) {
        view.removeCallbacks(mShowImeRunnable)
        mShowImeRunnable = view.showKeyboard()
    }

    override fun onColorSelected(color: Int) {
        binding.color.setColor(color)
    }
}