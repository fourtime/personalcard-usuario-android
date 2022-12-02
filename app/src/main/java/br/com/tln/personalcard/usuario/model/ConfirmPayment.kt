package br.com.tln.personalcard.usuario.model

import android.os.Parcel
import android.os.Parcelable
import br.com.tln.personalcard.usuario.extensions.format
import br.com.tln.personalcard.usuario.extensions.toLocalDateTime
import org.threeten.bp.LocalDateTime

data class ConfirmPayment(
    val transactionTime: LocalDateTime,
    val nsuAuthorization: String,
    val nsuHost: String,
    val carrierName: String,
    val balanceAfterTransaction: Float
) : Parcelable {

    constructor(parcel: Parcel) : this(
        transactionTime = parcel.readString()?.toLocalDateTime()
            ?: throw IllegalStateException("transactionTime is required"),
        nsuAuthorization = parcel.readString()
            ?: throw IllegalStateException("nsuAuthorization is required"),
        nsuHost = parcel.readString() ?: throw IllegalStateException("nsuHost is required"),
        carrierName = parcel.readString() ?: throw IllegalStateException("carrierName is required"),
        balanceAfterTransaction = parcel.readString()?.toFloat()
            ?: throw IllegalStateException("balanceAfterTransaction is required")
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(transactionTime.format())
        parcel.writeString(nsuAuthorization)
        parcel.writeString(nsuHost)
        parcel.writeString(carrierName)
        parcel.writeFloat(balanceAfterTransaction)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ConfirmPayment> {
        override fun createFromParcel(parcel: Parcel): ConfirmPayment {
            return ConfirmPayment(parcel)
        }

        override fun newArray(size: Int): Array<ConfirmPayment?> {
            return arrayOfNulls(size)
        }
    }
}