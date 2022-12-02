package br.com.tln.personalcard.usuario.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import br.com.tln.personalcard.usuario.db.DbTypeConverters

@Entity(tableName = "initialization_data")
@TypeConverters(DbTypeConverters::class)
data class InitializationData(
    @PrimaryKey(autoGenerate = false) val _id: Int = ID,
    @ColumnInfo(name = "cpf") val cpf: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "has_email") val hasEmail: Boolean,
    @ColumnInfo(name = "first_access") val firstAccess: Boolean
) {

    companion object {
        const val ID: Int = 1
    }
}