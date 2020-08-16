/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.ui.explorer

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import com.stokerapps.chartmaker.BuildConfig
import com.stokerapps.chartmaker.R
import com.stokerapps.chartmaker.databinding.FragmentFeedbackBinding
import com.stokerapps.chartmaker.ui.common.showToast
import com.stokerapps.chartmaker.ui.common.viewBinding
import kotlin.math.roundToInt

class FeedbackDialogFragment : DialogFragment(R.layout.fragment_feedback) {

    companion object {

        const val SUBJECT = "ChartMaker Feedback"
        const val DEVELOPER_EMAIL = "StokerApps <stokerapps@gmail.com>"

        fun newInstance() = FeedbackDialogFragment()
    }

    private val binding by viewBinding(FragmentFeedbackBinding::bind)
    private var rating = 0
    private lateinit var starViews: List<ImageView>

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("rating", rating)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {

            starViews = listOf(star1, star2, star3, star4, star5)

            rateApp.setOnTouchListener { view, event ->
                val isActionDown = event.action == MotionEvent.ACTION_DOWN
                if (isActionDown || event.action == MotionEvent.ACTION_MOVE) {

                    val percent = (event.x - view.x) / view.width
                    rating = (percent * 5 + 0.3f).roundToInt()
                    updateRating(rating, true)

                    if (isActionDown) {
                        view.performClick()
                    }
                    true

                } else if (event.action == MotionEvent.ACTION_UP) {
                    if (rating == 5) {
                        rateButton.isVisible = true
                        googlePlayIcon.isVisible = true
                    }
                    true

                } else {
                    false
                }
            }

            rating = savedInstanceState?.getInt("rating", 0) ?: 0
            if (rating > 0) {
                updateRating(rating, false)
            }

            rateButton.setOnClickListener { navigateToPlayStore() }
            googlePlayIcon.setOnClickListener { navigateToPlayStore() }

            sendButton.setOnClickListener { sendEmail(feedbackEditText.text.toString(), rating) }
        }
    }

    private fun updateRating(rating: Int, fromUser: Boolean) {
        starViews.forEachIndexed { index, starView ->
            starView.setImageResource(if (index < rating) R.drawable.ic_star else R.drawable.ic_star_border)
        }

        if (fromUser.not() && rating == 5) {
            with(binding) {
                rateButton.isVisible = true
                googlePlayIcon.isVisible = true
            }
        }
    }

    private fun navigateToPlayStore() {
        val packageName = context?.packageName ?: BuildConfig.APPLICATION_ID
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(
                "https://play.google.com/store/apps/details?id=$packageName"
            )
            setPackage("com.android.vending")
        }
        startActivity(intent)
    }

    private fun sendEmail(message: String, rating: Int) {

        val ratingString = if (rating == 0) "(unrated)" else "($rating/5)"

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(DEVELOPER_EMAIL))
            putExtra(Intent.EXTRA_SUBJECT, "$SUBJECT $ratingString")
            putExtra(Intent.EXTRA_TEXT, message)
        }

        activity?.let {
            if (intent.resolveActivity(it.packageManager) != null) {
                startActivity(intent)
            } else {
                showCouldNotFindAppToSendEmailWithToast()
            }
        } ?: showCouldNotFindAppToSendEmailWithToast()
    }

    private fun showCouldNotFindAppToSendEmailWithToast() {
        showToast(R.string.could_not_find_app_to_send_the_email_with)
    }
}