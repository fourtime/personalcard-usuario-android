package br.com.tln.personalcard.usuario.preferences

import android.content.SharedPreferences
import androidx.core.content.edit
import br.com.tln.personalcard.usuario.APP_CONFIG_SHARED_PREFERENCES
import br.com.tln.personalcard.usuario.AppPreferencesKeys
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class AppPreferences @Inject constructor(@param:Named(APP_CONFIG_SHARED_PREFERENCES) private val sharedPreferences: SharedPreferences) {

    init {
        GlobalScope.launch(Dispatchers.IO) {
            if (!sharedPreferences.contains(AppPreferencesKeys.DEVICE_ID)) {
                sharedPreferences.edit {
                    putString(AppPreferencesKeys.DEVICE_ID, createAppIdentifier())
                }
            }
        }
    }

    private fun createAppIdentifier(): String = UUID.randomUUID().toString()

    fun getDeviceId(): String = sharedPreferences.getString(AppPreferencesKeys.DEVICE_ID, null)
        ?: throw IllegalStateException("Device ID is not initialized")
}