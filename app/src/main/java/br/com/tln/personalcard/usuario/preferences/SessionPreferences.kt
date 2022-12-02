package br.com.tln.personalcard.usuario.preferences

import android.content.SharedPreferences
import androidx.core.content.edit
import br.com.tln.personalcard.usuario.SESSION_SHARED_PREFERENCES
import br.com.tln.personalcard.usuario.SessionPreferencesKeys
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class SessionPreferences @Inject constructor(@param:Named(SESSION_SHARED_PREFERENCES) private val sharedPreferences: SharedPreferences) {

    fun getFcmSentToken(): String? =
        sharedPreferences.getString(SessionPreferencesKeys.FCM_SENT_TOKEN, null)

    fun setFcmSentToken(fcmToken: String) = sharedPreferences.edit {
        putString(SessionPreferencesKeys.FCM_SENT_TOKEN, fcmToken)
    }

    fun clear() {
        sharedPreferences.edit {
            clear()
        }
    }
}