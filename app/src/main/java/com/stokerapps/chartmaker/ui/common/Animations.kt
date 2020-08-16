package com.stokerapps.chartmaker.ui.common

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.View

/**
 * @param duration Animation duration in milliseconds
 */
fun View.fadeIn(duration: Long) {
    clearAnimation()
    animate()
        .alpha(1f)
        .setDuration((1L - alpha.toLong()) * duration)
        .setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                visibility = View.VISIBLE
                if (alpha == 1f) {
                    alpha = 0f
                }
            }
        })
}

/**
 * @param duration Animation duration in milliseconds
 */
fun View.fadeOut(duration: Long) {
    clearAnimation()
    animate()
        .alpha(0f)
        .setDuration(alpha.toLong() * duration)
        .setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                visibility = View.GONE
            }
        })
}