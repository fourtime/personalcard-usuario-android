package br.com.tln.personalcard.usuario.provider

import androidx.annotation.ArrayRes
import androidx.annotation.BoolRes
import androidx.annotation.IntegerRes
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes

interface ResourceProvider {
    fun getString(@StringRes resId: Int): String
    fun getString(@StringRes resId: Int, vararg formatArgs: Any): String
    fun getQuantityString(@PluralsRes id: Int, quantity: Int): String
    fun getQuantityString(@PluralsRes id: Int, quantity: Int, vararg formatArgs: Any): String
    fun getStringArray(@ArrayRes resId: Int): Array<out String>
    fun getIntArray(@ArrayRes resId: Int): IntArray
    fun getBoolean(@BoolRes resId: Int): Boolean
    fun getInteger(@IntegerRes resId: Int): Int
}