package br.com.tln.personalcard.usuario

import android.os.Build
import java.util.Locale

/* General */
val DEVICE_NAME =
    "${Build.MANUFACTURER} ${Build.MODEL}, Android ${Build.VERSION.RELEASE} (${Build.VERSION.SDK_INT})"
const val USER_DATA_RENEWAL_MINUTES_INTERVAL: Long = 30

/* Database */
const val SESSION_DB = "TelenetSession.db"
const val APP_DB = "TelenetApp.db"

/* Shared Preferences */
const val SESSION_SHARED_PREFERENCES = "TelenetSessionPreferences"
const val APP_CONFIG_SHARED_PREFERENCES = "TelenetAppConfigPreferences"

/* Api */
const val AUTHORIZATION_HEADER = "Authorization"

/* Dagger names */
const val AUTHENTICATION_OK_HTTP_CLIENT = "authenticatorOkHttpClient"
const val AUTHENTICATION_TELENET_SERVICE = "authenticatorTelenetService"
const val TRANSACTION_OK_HTTP_CLIENT = "transactionOkHttpClient"

val PT_BR = Locale("pt", "BR")

object AppPreferencesKeys {
    const val DEVICE_ID = "deviceId"
    const val IS_INITIALIZED = "initialized"

    const val MOCK_CPF = "mockCpf" //TODO REMOVER
    const val MOCK_PASSWORD = "mockPassword" //TODO REMOVER
}

object SessionPreferencesKeys {
    const val FCM_SENT_TOKEN = "fcmsenttoken"
}