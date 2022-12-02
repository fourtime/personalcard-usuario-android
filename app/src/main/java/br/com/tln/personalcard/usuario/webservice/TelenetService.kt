package br.com.tln.personalcard.usuario.webservice

import br.com.tln.personalcard.usuario.AUTHORIZATION_HEADER
import br.com.tln.personalcard.usuario.BuildConfig
import br.com.tln.personalcard.usuario.webservice.request.SettingInstanceRequest
import br.com.tln.personalcard.usuario.webservice.response.AccessTokenResponse
import br.com.tln.personalcard.usuario.webservice.response.TelenetBaseResponse
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST

interface TelenetService {

    @FormUrlEncoded
    @POST("security/authentication/token")
    suspend fun getApplicationToken(
        @Field("applicationKey") applicationKey: String = BuildConfig.APPLICATION_KEY
    ): AccessTokenResponse

    @FormUrlEncoded
    @POST("security/authentication/token")
    suspend fun getAccessToken(
        @Field("applicationKey") applicationKey: String = BuildConfig.USER_APPLICATION_KEY,
        @Field("username") username: String,
        @Field("password") password: String
    ): AccessTokenResponse

    @POST("services/usuario/mobile/ConfiguraInstanciaApp")
    suspend fun settingAppInstance(
        @Header(AUTHORIZATION_HEADER) authorization: String,
        @Body request: SettingInstanceRequest
    ): TelenetBaseResponse<Any>
}