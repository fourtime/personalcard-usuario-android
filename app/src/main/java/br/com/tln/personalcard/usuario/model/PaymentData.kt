package br.com.tln.personalcard.usuario.model

import android.os.Parcel
import android.os.Parcelable
import br.com.tln.personalcard.usuario.extensions.format
import br.com.tln.personalcard.usuario.extensions.toLocalDateTime
import org.threeten.bp.LocalDateTime
import java.math.BigDecimal

data class PaymentData(
    val condition: String,
    val establishment: String,
    val address: String,
    val transactionTime: LocalDateTime,
    val value: BigDecimal,
    val token: String
) : Parcelable {

    constructor(parcel: Parcel) : this(
        condition = parcel.readString() ?: throw IllegalStateException("condition is required"),
        establishment = parcel.readString()
            ?: throw IllegalStateException("establishment is required"),
        address = parcel.readString() ?: throw IllegalStateException("address is required"),
        transactionTime = parcel.readString()?.toLocalDateTime()
            ?: throw IllegalStateException("transactionTime is required"),
        value = parcel.readString()?.toBigDecimal()
            ?: throw IllegalStateException("value is required"),
        token = parcel.readString() ?: throw IllegalStateException("Token is required")
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(condition)
        parcel.writeString(establishment)
        parcel.writeString(address)
        parcel.writeString(transactionTime.format())
        parcel.writeString(value.toString())
        parcel.writeString(token)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PaymentData> {
        override fun createFromParcel(parcel: Parcel): PaymentData {
            return PaymentData(parcel)
        }

        override fun newArray(size: Int): Array<PaymentData?> {
            return arrayOfNulls(size)
        }
    }
}