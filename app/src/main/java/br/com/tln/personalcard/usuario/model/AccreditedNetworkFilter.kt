package br.com.tln.personalcard.usuario.model

import android.os.Parcel
import android.os.Parcelable

data class AccreditedNetworkFilter(
    val idCard: String,
    val type: Int,
    val segment: String,
    val speciality: String,
    val uf: String,
    val city: String,
    val neighborhood: String,
    val activityBranch: String,
    val pageNumber: Int,
    val pageSize: Int
) : Parcelable {
    constructor(parcel: Parcel) : this(
        idCard = parcel.readString() ?: "",
        type = parcel.readInt(),
        segment = parcel.readString() ?: "",
        speciality = parcel.readString() ?: "",
        uf = parcel.readString() ?: "",
        city = parcel.readString() ?: "",
        neighborhood = parcel.readString() ?: "",
        activityBranch = parcel.readString() ?: "",
        pageNumber = parcel.readInt(),
        pageSize = parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(idCard)
        parcel.writeInt(type)
        parcel.writeString(segment)
        parcel.writeString(speciality)
        parcel.writeString(uf)
        parcel.writeString(city)
        parcel.writeString(neighborhood)
        parcel.writeString(activityBranch)
        parcel.writeInt(pageNumber)
        parcel.writeInt(pageSize)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AccreditedNetworkFilter> {
        override fun createFromParcel(parcel: Parcel): AccreditedNetworkFilter {
            return AccreditedNetworkFilter(parcel)
        }

        override fun newArray(size: Int): Array<AccreditedNetworkFilter?> {
            return arrayOfNulls(size)
        }
    }
}