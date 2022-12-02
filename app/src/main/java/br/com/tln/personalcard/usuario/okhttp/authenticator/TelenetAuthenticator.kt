package br.com.tln.personalcard.usuario.okhttp.authenticator

import br.com.tln.personalcard.usuario.AUTHENTICATION_TELENET_SERVICE
import br.com.tln.personalcard.usuario.AUTHORIZATION_HEADER
import br.com.tln.personalcard.usuario.BuildConfig
import br.com.tln.personalcard.usuario.db.AccessTokenDao
import br.com.tln.personalcard.usuario.db.AccountDataDao
import br.com.tln.personalcard.usuario.entity.AccessToken
import br.com.tln.personalcard.usuario.extensions.CryptographyUtils.decrypt
import br.com.tln.personalcard.usuario.preferences.AppPreferences
import br.com.tln.personalcard.usuario.webservice.TelenetService
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import org.threeten.bp.LocalDateTime
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named

class TelenetAuthenticator @Inject constructor(
    @param:Named(AUTHENTICATION_TELENET_SERVICE) private val telenetService: TelenetService,
    private val appPreferences: AppPreferences,
    private val accountDataDao: AccountDataDao,
    private val accessTokenDao: AccessTokenDao
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {

        val previousToken = response.request().header(AUTHORIZATION_HEADER) ?: return null
        if (response.request().tag(TokenRetry::class.java) != null) {
            Timber.d("Token renewal did not fix authentication issues")
            return null
        }

        Timber.d("Invalid token detected: $previousToken\nRenewal will be attempted")

        val accessToken: AccessToken = accountDataDao.get()?.let { account ->
            runBlocking {
                try {
                    val deviceId = appPreferences.getDeviceId()
                    val cpf = account.username
                    val username = "${BuildConfig.OPERATOR_CODE}-$deviceId@$cpf"
                    telenetService.getAccessToken(
                        username = username,
                        password = account.password.decrypt()
                    ).let { loginResponse ->
                        AccessToken(
                            type = loginResponse.type,
                            value = loginResponse.accessToken,
                            scope = loginResponse.scope,
                            expiration = LocalDateTime.now().plusSeconds(loginResponse.expiresIn)
                        )
                    }.also { accessToken ->
                        Timber.d("Token renewal attempt successfully completed.\nNew token is: ${accessToken.formattedToken}")
                        accessTokenDao.update(accessToken)
                    }
                } catch (t: Throwable) {
                    Timber.d(t, "Token renewal attempt failed")
                    return@runBlocking null
                }
            }
        } ?: return null

        return response.request().newBuilder()
            .header(AUTHORIZATION_HEADER, accessToken.formattedToken)
            .tag(TokenRetry::class.java, TokenRetry())
            .build()

        return null
    }
}

private class TokenRetry