/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.ui.piechart.entries


import android.annotation.SuppressLint
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.EditorInfo
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.stokerapps.chartmaker.databinding.ViewPieChartEntryItemBinding
import com.stokerapps.chartmaker.databinding.ViewPieChartFooterItemBinding
import com.stokerapps.chartmaker.ui.piechart.PieChartEntryItem
import timber.log.Timber
import java.text.DecimalFormat
import java.util.*

const val VIEW_TYPE_FOOTER = 0
const val VIEW_TYPE_ITEM = 1

class PieChartEntriesAdapter(
    private val callback: Callback? = null
) : ListAdapter<PieChartEntryItem, RecyclerView.ViewHolder>(
    object : DiffUtil.ItemCallback<PieChartEntryItem>() {
        override fun areItemsTheSame(oldItem: PieChartEntryItem, newItem: PieChartEntryItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: PieChartEntryItem, newItem: PieChartEntryItem): Boolean {
            return oldItem == newItem
        }
    }) {

    interface Callback {
        fun onAddNewEntryPressed()
        fun onColorClicked(entry: PieChartEntryItem)
        fun onDeletePressed(entry: PieChartEntryItem)
        fun onEntriesChanged(entries: List<PieChartEntryItem>)
        fun onEntryChanged(entry: PieChartEntryItem)
    }

    companion object {
        val formatter = DecimalFormat("#.##")
    }

    private val itemTouchHelperCallback by lazy { ItemTouchHelperCallback(this) }
    val itemTouchHelper by lazy { ItemTouchHelper(itemTouchHelperCallback) }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_FOOTER -> FooterViewHolder(
                ViewPieChartFooterItemBinding.inflate(inflater, parent, false),
                callback
            )
            VIEW_TYPE_ITEM -> EntryViewHolder(
                ViewPieChartEntryItemBinding.inflate(
                    inflater,
                    parent,
                    false
                )
            )
            else -> throw IllegalArgumentException("Unknown view type $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is FooterViewHolder -> holder.update(itemCount)
            is EntryViewHolder -> {
                val entry = getItem(position)
                holder.bind(entry)
            }
        }
    }

    override fun getItemViewType(position: Int) =
        when (position) {
            itemCount - 1 -> VIEW_TYPE_FOOTER
            else -> VIEW_TYPE_ITEM
        }

    override fun getItemCount(): Int = super.getItemCount() + 1

    fun onItemMoved(fromPosition: Int, toPosition: Int) {
        val entries = currentList.toMutableList()
        Collections.swap(entries, fromPosition, toPosition)
        submitList(entries)
        callback?.onEntriesChanged(entries)
    }

    fun onItemRemoved(position: Int) {
        val entries = currentList.toMutableList()
        val entry = entries.removeAt(position)
        submitList(entries)
        callback?.onDeletePressed(entry)
    }

    inner class EntryViewHolder(
        private val binding: ViewPieChartEntryItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val entry: PieChartEntryItem?
            get() = binding.root.tag as? PieChartEntryItem

        private val onColorClick = View.OnClickListener {
            entry?.let { entry -> callback?.onColorClicked(entry) }
        }

        private val onDelete = View.OnClickListener {
            onItemRemoved(adapterPosition)
        }

        private val labelChangedListener = object : ItemTextWatcher() {
            override fun afterTextChanged(s: Editable?) {
                entry?.let { entry ->
                    Timber.d("Text changed on: %s", Thread.currentThread().name)
                    entry.label = s.toString()
                    callback?.onEntryChanged(entry)
                }
            }
        }
        private val valueChangedListener = object : ItemTextWatcher() {
            override fun afterTextChanged(s: Editable?) {
                entry?.let { entry ->
                    entry.value = s?.toString()?.toFloatOrNull() ?: 0f
                    callback?.onEntryChanged(entry)
                }
            }
        }

        init {
            with(binding) {
                color.setOnClickListener(onColorClick)
                delete.setOnClickListener(onDelete)
                value.setOnEditorActionListener { view, actionId, event ->
                    if (actionId == EditorInfo.IME_ACTION_DONE
                        || actionId == EditorInfo.IME_ACTION_NEXT
                        || event?.action == KeyEvent.KEYCODE_ENTER
                    ) {
                        view?.clearFocus()
                    }
                    false
                }
            }
        }

        @SuppressLint("ClickableViewAccessibility")
        fun bind(entry: PieChartEntryItem) {

            with(binding) {
                root.tag = entry
                color.setColor(entry.color)

                label.removeTextChangedListener(labelChangedListener)
                label.text = SpannableStringBuilder(entry.label)
                label.addTextChangedListener(labelChangedListener)

                val valueText = formatter.format(entry.value)
                value.removeTextChangedListener(valueChangedListener)
                value.text = SpannableStringBuilder(valueText)
                value.addTextChangedListener(valueChangedListener)

                reorder.setOnTouchListener { view, event ->
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            itemTouchHelper.startDrag(this@EntryViewHolder)
                            false
                        }
                        MotionEvent.ACTION_UP -> {
                            view.performClick()
                        }
                        else -> false
                    }
                }
            }
        }

        abstract inner class ItemTextWatcher : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
    }

    class FooterViewHolder(
        private val binding: ViewPieChartFooterItemBinding,
        callback: Callback?
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.button.setOnClickListener { callback?.onAddNewEntryPressed() }
        }

        fun update(itemSize: Int) {
            with(binding) {
                if (itemSize > 1) {
                    actionPointer.visibility = View.GONE
                    actionText.visibility = View.GONE
                } else {
                    actionPointer.visibility = View.VISIBLE
                    actionText.visibility = View.VISIBLE
                }
            }
        }
    }

    class ItemTouchHelperCallback(val adapter: PieChartEntriesAdapter) :
        ItemTouchHelper.Callback() {

        override fun getMovementFlags(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder
        ) = when (viewHolder) {
            is FooterViewHolder -> makeMovementFlags(0, 0)
            else -> makeMovementFlags(
                ItemTouchHelper.UP or ItemTouchHelper.DOWN,
                ItemTouchHelper.START or ItemTouchHelper.END
            )
        }

        override fun canDropOver(
            recyclerView: RecyclerView,
            current: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ) = target is EntryViewHolder

        override fun isItemViewSwipeEnabled() = false

        override fun isLongPressDragEnabled() = false

        override fun onMove(
            recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            adapter.onItemMoved(
                viewHolder.adapterPosition,
                target.adapterPosition
            )
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            adapter.onItemRemoved(viewHolder.adapterPosition)
        }
    }
}
