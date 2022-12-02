package br.com.tln.personalcard.usuario.db

import androidx.room.TypeConverter
import br.com.tln.personalcard.usuario.entity.CardType
import br.com.tln.personalcard.usuario.extensions.format
import br.com.tln.personalcard.usuario.extensions.toLocalDate
import br.com.tln.personalcard.usuario.extensions.toLocalDateTime
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime

object DbTypeConverters {

    @JvmStatic
    @TypeConverter
    fun cardTypeToId(type: CardType?): Int? {
        return type?.id
    }

    @JvmStatic
    @TypeConverter
    fun idToCardType(id: Int?): CardType? {
        return CardType.fromId(id)
    }

    @JvmStatic
    @TypeConverter
    fun stringToLocalDate(value: String?): LocalDate? {
        return value?.toLocalDate()
    }

    @JvmStatic
    @TypeConverter
    fun localDateToString(value: LocalDate?): String? {
        return value?.format()
    }

    @JvmStatic
    @TypeConverter
    fun stringToLocalDateTime(value: String?): LocalDateTime? {
        return value?.toLocalDateTime()
    }

    @JvmStatic
    @TypeConverter
    fun localDateTimeToString(value: LocalDateTime?): String? {
        return value?.format()
    }
}