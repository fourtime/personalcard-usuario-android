package br.com.tln.personalcard.usuario.entity

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import br.com.tln.personalcard.usuario.db.DbTypeConverters
import br.com.tln.personalcard.usuario.extensions.format
import br.com.tln.personalcard.usuario.extensions.toLocalDateTime
import org.threeten.bp.LocalDateTime

@Entity(
    tableName = "card",
    foreignKeys = [
        ForeignKey(
            entity = AccountData::class,
            parentColumns = ["_id"],
            childColumns = ["account_id"]
        )
    ]
)
@TypeConverters(DbTypeConverters::class)
data class Card(
        @PrimaryKey(autoGenerate = false) @ColumnInfo(name = "id") val id: String,
        @ColumnInfo(name = "label") val label: String,
        @ColumnInfo(name = "type") val type: CardType,
        @ColumnInfo(name = "last_digits") val lastDigits: String,
        @ColumnInfo(name = "account_id", index = true) val accountId: Int = AccountData.ID,
        @ColumnInfo(name = "balance") val balance: Float,
        @ColumnInfo(name = "averange_daily_balance") val averangeDailyBalance: Float,
        @ColumnInfo(name = "recharge_date") val rechargeDate: LocalDateTime?,
        @ColumnInfo(name = "renovation_date") val renovationDate: LocalDateTime?
) : Parcelable {

    constructor(parcel: Parcel) : this(
        id = parcel.readString() ?: throw IllegalStateException("Id is required"),
        label = parcel.readString() ?: throw IllegalStateException("Label is required"),
        type = CardType.fromId(parcel.readInt())
            ?: throw IllegalStateException("Invalid Card Type"),
        lastDigits = parcel.readString() ?: throw IllegalStateException("Last digits are required"),
        accountId = parcel.readInt(),
        balance = parcel.readFloat(),
        averangeDailyBalance = parcel.readFloat(),
        rechargeDate = parcel.readString()?.toLocalDateTime(),
        renovationDate = parcel.readString()?.toLocalDateTime()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(label)
        parcel.writeInt(type.id)
        parcel.writeString(lastDigits)
        parcel.writeInt(accountId)
        parcel.writeFloat(balance)
        parcel.writeFloat(averangeDailyBalance)
        parcel.writeString(rechargeDate?.format())
        parcel.writeString(renovationDate?.format())
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Card> {
        override fun createFromParcel(parcel: Parcel): Card {
            return Card(parcel)
        }

        override fun newArray(size: Int): Array<Card?> {
            return arrayOfNulls(size)
        }
    }
}