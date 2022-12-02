package br.com.tln.personalcard.usuario.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "device")
data class Device(
    @PrimaryKey(autoGenerate = true) val _id: Int = ID,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "token") val token: String
) {

    companion object {
        const val ID: Int = 1
    }
}