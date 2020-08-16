/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.ui.common.color_picker

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.stokerapps.chartmaker.R
import com.stokerapps.chartmaker.databinding.DialogPickColorBinding
import com.stokerapps.chartmaker.ui.common.*
import java.lang.Math.toDegrees
import java.lang.Math.toRadians
import java.util.*
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin


class ColorPickerDialogFragment(private val viewModelFactory: ViewModelProvider.Factory) :
    DialogFragment(R.layout.dialog_pick_color), View.OnTouchListener,
    SeekBar.OnSeekBarChangeListener {

    companion object {

        const val TAG = "colorPickerDialog"

        private const val COLOR = "color"

        fun newInstance(viewModelFactory: ViewModelProvider.Factory) =
            ColorPickerDialogFragment(
                viewModelFactory
            )
    }

    interface Callback {
        fun onColorSelected(color: Int)
    }

    private val callback by lazy {
        if (parentFragment is Callback)
            parentFragment as Callback else null
    }

    private val binding by viewBinding(DialogPickColorBinding::bind)
    private val viewModel: ColorPickerViewModel by viewModels { viewModelFactory }

    private val adapter =
        ColorAdapter(
            object :
                ColorAdapter.Callback {
                override fun onColorSelected(color: Int) {
                    updateSelectedColor(color)
                    hsv = Hsv.fromColor(color)
                    updateColorPointerPosition(hsv)
                    with(binding) {
                        colorWheel.setBrightness((hsv.value * 255f).toInt())
                        colorAlpha.progress = (hsv.value * 255f).toInt()
                    }
                }
            })

    private lateinit var hsv: Hsv
    private var selectedColor: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.DialogTheme)
        savedInstanceState?.let {
            if (it.containsKey(COLOR)) {
                selectedColor = it.getInt(COLOR)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        selectedColor?.let {
            outState.putInt(COLOR, it)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {

            hsv = selectedColor?.let { color ->
                updatePreview(color)
                updateColorPointerPosition(Hsv.fromColor(color))
                Hsv.fromColor(color)
            } ?: Hsv()

            colorHistory.adapter = adapter
            colorHistory.layoutManager = GridLayoutManager(
                context, 2,
                if (isWideScreen()) GridLayoutManager.HORIZONTAL else GridLayoutManager.VERTICAL
                ,
                true
            )

            colorPreview.setOnClickListener {
                if (selectedColor == null) dismiss()
            }

            colorWheel.setBrightness((hsv.value * 255f).toInt())
            colorWheel.setOnTouchListener(this@ColorPickerDialogFragment)

            colorAlpha.setOnSeekBarChangeListener(this@ColorPickerDialogFragment)
            colorAlpha.max = 255
            colorAlpha.progress = (hsv.saturation * 255f).toInt()

            done.setOnClickListener {
                selectedColor?.let {
                    viewModel.saveColor(it)
                    callback?.onColorSelected(it)
                }
                dismiss()
            }
        }
        viewModel.editor.observe(viewLifecycleOwner, Observer { adapter.update(it.colors) })
    }

    private fun updateSelectedColor(hsv: Hsv) {
        updateSelectedColor(hsv.toColor())
    }

    private fun updateSelectedColor(color: Int) {
        selectedColor = color
        updatePreview(color)
    }

    private fun updatePreview(color: Int) {
        with(binding.colorPreview) {
            setBackgroundColor(color)
            text = color.toHexColorString()
            setTextColor(getTextColor(color))
        }
    }

    private fun updateColorPointerPosition(hsv: Hsv) {
        cx = binding.colorWheel.centerX()
        cy = binding.colorWheel.centerY()

        theta = toRadians(hsv.hue.toDouble())
        dxy2 = (hsv.saturation * r).toDouble()

        dx = dxy2 * cos(theta)
        dy = dxy2 * -sin(theta)

        with(binding.colorPointer) {
            x = dx.toFloat() + cx - width / 2f
            y = dy.toFloat() + cy - height / 2f
            visibility = View.VISIBLE
        }
    }

    private fun Int.toHexColorString() =
        String.format(Locale.getDefault(), "#%06X", (0xFFFFFF and this))

    override fun onCancel(dialog: DialogInterface) {
        dismiss()
    }

    private var cx = 0
    private var cy = 0
    private var dx = 0.0
    private var dy = 0.0
    private var dxy2 = 0.0

    private val r by lazy { binding.colorWheel.getRadius() }
    private var r2 = 0f
    private var theta = 0.0

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        event?.let {
            cx = binding.colorWheel.centerX()
            cy = binding.colorWheel.centerY()
            dx = (event.x - cx).toDouble()
            dy = (event.y - cy).toDouble()
            dxy2 = dx * dx + dy * dy

            r2 = r * r
            theta = atan2(dy, dx)

            if (dxy2 > r2) {
                dx = r * cos(theta)
                dy = r * sin(theta)
                dxy2 = dx * dx + dy * dy
            }

            with(binding.colorPointer) {
                x = dx.toFloat() + cx - width / 2f
                y = dy.toFloat() + cy - height / 2f
                visibility = View.VISIBLE
            }

            hsv.hue = ((toDegrees(-theta) + 360) % 360).toFloat()
            hsv.saturation = (dxy2 / r2).toFloat()
            updateSelectedColor(hsv)

            if (event.action == MotionEvent.ACTION_BUTTON_PRESS) {
                v?.performClick()
            }
        }
        return true
    }

    private fun View.centerX() = (x + width / 2f).toInt()

    private fun View.centerY() = (y + height / 2f).toInt()

    private fun getTextColor(color: Int): Int {
        return if (color.isBrightColor()) Color.BLACK else Color.WHITE
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        if (fromUser) {
            binding.colorWheel.setBrightness(progress)
            hsv.value = progress / 255f
            updateSelectedColor(hsv)
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
    }

    class ColorAdapter(var callback: Callback? = null) :
        RecyclerView.Adapter<ColorAdapter.ViewHolder>() {

        interface Callback {
            fun onColorSelected(color: Int)
        }

        private val colors = MutableList(0) { 0 }
        private val onClick = View.OnClickListener {
            it?.tag?.let { color ->
                if (color is Int) {
                    callback?.onColorSelected(color)
                }
            }
        }

        fun update(colors: Collection<Int>) {
            this.colors.clear()
            this.colors.addAll(colors)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.view_color_item, parent, false)
                    .apply {
                        setOnClickListener(onClick)
                    }
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val color = colors[position]
            holder.itemView.setBackgroundColor(color)
            holder.itemView.tag = color
        }

        override fun getItemCount() = colors.size

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
    }
}