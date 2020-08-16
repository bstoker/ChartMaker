/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

@file:Suppress("unused")

package com.stokerapps.chartmaker.ui.common

import android.content.Context
import android.view.KeyEvent
import android.view.Menu
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.annotation.ColorInt
import androidx.annotation.IdRes
import androidx.core.graphics.drawable.DrawableCompat
import timber.log.Timber


fun View.showKeyboard(): Runnable = setImeVisibility(true)
fun View.hideKeyboard(): Runnable = setImeVisibility(false)

fun View.setImeVisibility(visible: Boolean): Runnable {

    val inputMethodManager = context
        .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    val showImeRunnable = Runnable {
        inputMethodManager.showSoftInput(this, 0)
    }
    if (visible) {
        post(showImeRunnable)
    } else {
        inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
    }
    return showImeRunnable
}

fun EditText.setOnKeyboardDonePress(action: EditText.() -> Unit) {
    setOnKeyListener { _, keyCode, event ->
        if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
            action()
            true
        } else {
            false
        }
    }
    setOnEditorActionListener { _, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            action()
            true
        } else {
            false
        }
    }
}

fun Menu.setItemColor(@IdRes itemId: Int, @ColorInt color: Int) {
    findItem(itemId)?.let { item ->
        var drawable = item.icon
        drawable = DrawableCompat.wrap(drawable)
        DrawableCompat.setTint(drawable.mutate(), color)
        item.icon = drawable
    } ?: Timber.e("No item with id $itemId!")
}