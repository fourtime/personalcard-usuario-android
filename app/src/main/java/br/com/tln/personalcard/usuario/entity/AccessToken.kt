package br.com.tln.personalcard.usuario.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import br.com.tln.personalcard.usuario.db.DbTypeConverters
import org.threeten.bp.LocalDateTime

@Entity(
    tableName = "access_token",
    foreignKeys = [
        ForeignKey(
            entity = AccountData::class,
            parentColumns = ["_id"],
            childColumns = ["account_id"]
        )
    ]
)
@TypeConverters(DbTypeConverters::class)
data class AccessToken(
    @PrimaryKey(autoGenerate = false) val _id: Int = ID,
    @ColumnInfo(name = "token_type") val type: String,
    @ColumnInfo(name = "access_token") val value: String,
    @ColumnInfo(name = "scope") val scope: String,
    @ColumnInfo(name = "expiration") val expiration: LocalDateTime,
    @ColumnInfo(name = "account_id", index = true) val accountId: Int = AccountData.ID
) {

    val formattedToken
        get() = "$type $value"

    companion object {
        const val ID: Int = 1
    }
}