/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.ui.common

import android.os.Parcel
import android.os.Parcelable
import com.stokerapps.chartmaker.domain.PieChartEntry
import java.math.BigDecimal

class ParcelPieChartEntry(val entry: PieChartEntry) : Parcelable {
    constructor(parcel: Parcel) : this(
        PieChartEntry(
            parcel.readInt(),
            parcel.readString() ?: "",
            BigDecimal(parcel.readString()),
            parcel.readInt()
        )
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(entry.id)
        parcel.writeString(entry.label)
        parcel.writeString(entry.value.toString())
        parcel.writeInt(entry.color)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<ParcelPieChartEntry> {
        override fun createFromParcel(parcel: Parcel): ParcelPieChartEntry {
            return ParcelPieChartEntry(parcel)
        }

        override fun newArray(size: Int): Array<ParcelPieChartEntry?> {
            return arrayOfNulls(size)
        }
    }
}