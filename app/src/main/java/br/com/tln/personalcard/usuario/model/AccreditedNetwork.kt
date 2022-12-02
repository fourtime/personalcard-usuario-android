package br.com.tln.personalcard.usuario.model

import android.os.Parcel
import android.os.Parcelable

data class AccreditedNetwork(
    val id: String,
    val fantasyName: String,
    val address: String,
    val complement: String,
    val city: String,
    val neighborhood: String,
    val uf: String,
    val latitude: String,
    val longitude: String,
    val phone: String?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        id = parcel.readString() ?: "",
        fantasyName = parcel.readString() ?: "",
        address = parcel.readString() ?: "",
        complement = parcel.readString() ?: "",
        city = parcel.readString() ?: "",
        neighborhood = parcel.readString() ?: "",
        uf = parcel.readString() ?: "",
        latitude = parcel.readString() ?: "",
        longitude = parcel.readString() ?: "",
        phone = parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(fantasyName)
        parcel.writeString(address)
        parcel.writeString(complement)
        parcel.writeString(city)
        parcel.writeString(neighborhood)
        parcel.writeString(uf)
        parcel.writeString(latitude)
        parcel.writeString(longitude)
        parcel.writeString(phone)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AccreditedNetwork> {
        override fun createFromParcel(parcel: Parcel): AccreditedNetwork {
            return AccreditedNetwork(parcel)
        }

        override fun newArray(size: Int): Array<AccreditedNetwork?> {
            return arrayOfNulls(size)
        }
    }
}