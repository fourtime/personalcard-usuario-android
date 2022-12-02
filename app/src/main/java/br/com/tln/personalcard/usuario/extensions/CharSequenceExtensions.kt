package br.com.tln.personalcard.usuario.extensions

import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.DateTimeParseException

fun CharSequence.toLocalDate(pattern: String = "yyyy-MM-dd"): LocalDate {
    return LocalDate.parse(this, DateTimeFormatter.ofPattern(pattern))
}

fun CharSequence.toLocalDateOrNull(pattern: String = "yyyy-MM-dd"): LocalDate? {
    return try {
        LocalDate.parse(this, DateTimeFormatter.ofPattern(pattern))
    } catch (ex: DateTimeParseException) {
        null
    }
}

fun CharSequence.toLocalDateTime(pattern: String = "yyyy-MM-dd'T'HH:mm:ss"): LocalDateTime {
    return LocalDateTime.parse(this, DateTimeFormatter.ofPattern(pattern))
}

fun CharSequence.toLocalDateTimeOrNull(pattern: String = "yyyy-MM-dd'T'HH:mm:ss"): LocalDateTime? {
    return try {
        LocalDateTime.parse(this, DateTimeFormatter.ofPattern(pattern))
    } catch (ex: DateTimeParseException) {
        null
    }
}