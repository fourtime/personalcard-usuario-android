package br.com.tln.personalcard.usuario.provider

import android.app.Application
import androidx.annotation.ArrayRes
import androidx.annotation.BoolRes
import androidx.annotation.IntegerRes
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes

class ResourceProviderImpl(val application: Application) : ResourceProvider {

    override fun getString(@StringRes resId: Int) = application.resources.getString(resId)
    override fun getString(@StringRes resId: Int, vararg formatArgs: Any) =
        application.resources.getString(resId, *formatArgs)

    override fun getQuantityString(@PluralsRes id: Int, quantity: Int) =
        application.resources.getQuantityString(id, quantity)

    override fun getQuantityString(@PluralsRes id: Int, quantity: Int, vararg formatArgs: Any) =
        application.resources.getQuantityString(id, quantity, *formatArgs)

    override fun getStringArray(@ArrayRes resId: Int) = application.resources.getStringArray(resId)

    override fun getIntArray(@ArrayRes resId: Int) = application.resources.getIntArray(resId)

    override fun getBoolean(@BoolRes resId: Int) = application.resources.getBoolean(resId)

    override fun getInteger(@IntegerRes resId: Int) = application.resources.getInteger(resId)
}