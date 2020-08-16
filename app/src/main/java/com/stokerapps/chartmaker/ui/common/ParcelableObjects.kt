/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.ui.common

import android.os.Parcel
import android.os.Parcelable
import com.stokerapps.chartmaker.domain.PieChartEntry

class ParcelPieChartEntry(val entry: PieChartEntry) : Parcelable {
    constructor(parcel: Parcel) : this(
        PieChartEntry(
            parcel.readInt(),
            parcel.readString() ?: "",
            parcel.readFloat(),
            parcel.readInt()
        )
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(entry.id)
        parcel.writeString(entry.label)
        parcel.writeFloat(entry.value)
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