/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.ui.piechart.properties

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.stokerapps.chartmaker.R
import com.stokerapps.chartmaker.databinding.ViewCurrenciesBinding
import com.stokerapps.chartmaker.databinding.ViewCurrencyBinding
import com.stokerapps.chartmaker.ui.common.isWideScreen
import com.stokerapps.chartmaker.ui.common.viewBinding
import java.util.*
import kotlin.collections.HashSet

class CurrencyDialogFragment : DialogFragment(R.layout.view_currencies) {

    companion object {

        const val REQUEST_KEY = "request_currency"
        const val CURRENCY_CODE = "currency_code"

        fun newInstance() = CurrencyDialogFragment()
    }

    val binding by viewBinding(ViewCurrenciesBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val spanCount = if (isWideScreen()) 7 else 6

        with(binding.recyclerView) {
            layoutManager = GridLayoutManager(context, spanCount)
            adapter = CurrencyAdapter()
        }
    }

    private fun onCurrencyClicked(view: View) {
        val currency = view.tag as Currency
        setFragmentResult(REQUEST_KEY, bundleOf(CURRENCY_CODE to currency.currencyCode))
        dismiss()
    }

    inner class CurrencyAdapter : RecyclerView.Adapter<CurrencyAdapter.ViewHolder>() {

        private val currencies = getAvailableCurrencies().sortedBy { it.currencyCode }

        private fun getAvailableCurrencies(): Collection<Currency> {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                Currency.getAvailableCurrencies()
            } else {
                HashSet(Locale.getAvailableLocales().mapNotNull { Currency.getInstance(it) })
            }
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): CurrencyAdapter.ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.view_currency, parent, false)
            view.setOnClickListener(::onCurrencyClicked)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: CurrencyAdapter.ViewHolder, position: Int) {
            holder.bindTo(currencies[position])
        }

        override fun getItemCount() = currencies.size

        inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

            private val binding = ViewCurrencyBinding.bind(view)

            fun bindTo(currency: Currency) {
                view.tag = currency
                with(binding) {
                    symbol.text = currency.symbol
                    code.text = currency.currencyCode
                }
            }
        }
    }
}