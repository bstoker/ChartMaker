/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.ui.explorer

import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import com.stokerapps.chartmaker.BuildConfig
import com.stokerapps.chartmaker.R
import com.stokerapps.chartmaker.databinding.FragmentAboutBinding
import com.stokerapps.chartmaker.ui.common.viewBinding

class AboutDialogFragment : DialogFragment(R.layout.fragment_about) {

    companion object {
        fun newInstance() = AboutDialogFragment()
    }

    private val binding by viewBinding(FragmentAboutBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {

            version.text =
                getString(R.string.version, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE)
        }

    }
}