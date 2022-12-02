package br.com.tln.personalcard.usuario.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import br.com.tln.personalcard.usuario.db.DbTypeConverters

@Entity(tableName = "account")
@TypeConverters(DbTypeConverters::class)
data class AccountData(
    @PrimaryKey(autoGenerate = false) val _id: Int = ID,
    @ColumnInfo(name = "username") val username: String,
    @ColumnInfo(name = "password") val password: String,
    @ColumnInfo(name = "locked") val locked: Boolean = false
) {

    companion object {
        const val ID: Int = 1
    }

    fun copy(
        username: String = this.username,
        password: String = this.password,
        locked: Boolean = this.locked
    ) = AccountData(_id, username, password, locked)
}