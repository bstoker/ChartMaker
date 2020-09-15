/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

@file:Suppress("unused", "SpellCheckingInspection")

package com.stokerapps.chartmaker.ui.common

import android.content.res.Configuration
import android.graphics.Point
import android.view.View
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.google.android.material.snackbar.Snackbar
import com.stokerapps.chartmaker.R
import com.stokerapps.chartmaker.ui.common.color_picker.ColorPickerDialogFragment

@ColorInt
fun Fragment.getColorCompat(@ColorRes colorRes: Int, @ColorInt fallback: Int) =
    context?.getColorCompat(colorRes) ?: fallback

@Suppress("deprecation")
fun Fragment.getScreenWidthDp() = when {
    resources.configuration.screenWidthDp != Configuration.SCREEN_WIDTH_DP_UNDEFINED ->
        resources.configuration.screenWidthDp
    resources.displayMetrics.widthPixels != Configuration.SCREEN_WIDTH_DP_UNDEFINED ->
        resources.displayMetrics.widthPixels.px
    android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R -> {
        activity?.let {
            it.windowManager.currentWindowMetrics.bounds.width().let { width ->
                if (width > 0) {
                    width.px
                } else {
                    Configuration.SCREEN_WIDTH_DP_UNDEFINED
                }
            }
        } ?: Configuration.SCREEN_WIDTH_DP_UNDEFINED
    }
    android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1 -> {
        activity?.let {
            val size = Point()
            it.windowManager.defaultDisplay.getRealSize(size)
            size.x.px
        } ?: Configuration.SCREEN_WIDTH_DP_UNDEFINED
    }
    else -> {
        activity?.let {
            val size = Point()
            it.windowManager.defaultDisplay.getSize(size)
            size.x.px
        } ?: Configuration.SCREEN_WIDTH_DP_UNDEFINED
    }
}

fun Fragment.isChangingConfiguration() = activity?.isChangingConfigurations ?: false

fun Fragment.isPortraitMode() =
    resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT

fun Fragment.isWideScreen() = resources.configuration.screenWidthDp >= 700

fun Fragment.isDarkMode() =
    resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

fun Fragment.showToast(@StringRes message: Int, duration: Int = Toast.LENGTH_LONG): Toast? =
    context?.showToast(message, duration)

fun Fragment.showToast(message: CharSequence, duration: Int = Toast.LENGTH_LONG): Toast? =
    context?.showToast(message, duration)

fun Fragment.showSnackbar(
    @StringRes message: Int,
    @StringRes actionText: Int? = null,
    duration: Int = Snackbar.LENGTH_LONG,
    action: ((View) -> Unit)? = null
) = showSnackbar(
    getString(message),
    actionText,
    duration, action
)

fun Fragment.showSnackbar(
    message: CharSequence,
    @StringRes actionText: Int? = null,
    duration: Int = Snackbar.LENGTH_LONG,
    action: ((View) -> Unit)? = null
) = activity?.let {
    val snackbar = Snackbar.make(
        it.findViewById(R.id.coordinator),
        message, duration
    )

    actionText?.let { text ->
        snackbar.setAction(text, action)
        snackbar.setActionTextColor(it.getColorAttribute(R.attr.colorSecondary))
    }
    snackbar.show()
    snackbar
}

fun Fragment.showColorPicker(viewModelFactory: ViewModelProvider.Factory) {
    ColorPickerDialogFragment.newInstance(viewModelFactory)
        .show(childFragmentManager, ColorPickerDialogFragment.TAG)
}

/**
 * Allows the following code in a [Fragment] to lazily fetch it's [ViewBinding]:
 *
 *     val binding by viewBinding(FragmentXBinding::bind)
 *
 * See also: [FragmentViewBindingDelegate]
 *
 * [source](https://medium.com/@Zhuinden/simple-one-liner-viewbinding-in-fragments-and-activities-with-kotlin-961430c6c07c)
 */
fun <T : ViewBinding> Fragment.viewBinding(viewBindingFactory: (View) -> T) =
    FragmentViewBindingDelegate(this, viewBindingFactory)

