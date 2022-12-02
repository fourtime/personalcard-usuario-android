package br.com.tln.personalcard.usuario.moshi

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.math.BigDecimal

class BigDecimalJsonAdapter {

    @ToJson
    fun toJson(value: BigDecimal?): Double? {
        return value?.toDouble()
    }

    @FromJson
    fun fromJson(value: Double?): BigDecimal? {
        return value?.let {
            BigDecimal(it.toString())
        }
    }
}