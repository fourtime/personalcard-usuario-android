package br.com.tln.personalcard.usuario.webservice.request

import android.os.Parcel
import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class AccreditedNetworkFilterRequest(
    @Json(name = "idCartao") val idCard: String,
    @Json(name = "tipoCartao") val type: Int,
    @Json(name = "segmento") val segment: String,
    @Json(name = "especialidade") val speciality: String,
    @Json(name = "uf") val uf: String,
    @Json(name = "cidade") val city: String,
    @Json(name = "bairro") val neighborhood: String,
    @Json(name = "ramoAtividade") val activityBranch: String,
    @Json(name = "numeroPagina") val pageNumber: Int,
    @Json(name = "tamanhoPagina") val pageSize: Int
) : Serializable, Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readInt()
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

    companion object CREATOR : Parcelable.Creator<AccreditedNetworkFilterRequest> {
        override fun createFromParcel(parcel: Parcel): AccreditedNetworkFilterRequest {
            return AccreditedNetworkFilterRequest(parcel)
        }

        override fun newArray(size: Int): Array<AccreditedNetworkFilterRequest?> {
            return arrayOfNulls(size)
        }
    }
}