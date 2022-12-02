package br.com.tln.personalcard.usuario.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import br.com.tln.personalcard.usuario.db.DbTypeConverters
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime

@Entity(
    tableName = "profile",
    foreignKeys = [
        ForeignKey(
            entity = AccountData::class,
            parentColumns = ["_id"],
            childColumns = ["account_id"]
        )
    ]
)
@TypeConverters(DbTypeConverters::class)
data class Profile(
    @PrimaryKey(autoGenerate = false) val _id: Int = ID,
    @ColumnInfo(name = "email") val email: String,
    @ColumnInfo(name = "phone") val phone: String,
    @ColumnInfo(name = "picture") val picture: String?,
    @ColumnInfo(name = "birth_date") val birthDate: LocalDate?,
    @Embedded(prefix = "address_") val address: Address,
    @ColumnInfo(name = "account_id", index = true) val accountId: Int = AccountData.ID,
    @ColumnInfo(name = "fetchTime") val fetchTime: LocalDateTime
) {

    companion object {
        const val ID: Int = 1
    }
}