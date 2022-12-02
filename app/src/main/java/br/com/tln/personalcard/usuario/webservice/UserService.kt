package br.com.tln.personalcard.usuario.webservice

import br.com.tln.personalcard.usuario.webservice.request.ChangePasswordRequest
import br.com.tln.personalcard.usuario.webservice.request.EditProfileRequest
import br.com.tln.personalcard.usuario.webservice.request.InitializationRequest
import br.com.tln.personalcard.usuario.webservice.request.RecoveryPasswordRequest
import br.com.tln.personalcard.usuario.webservice.request.UpdateEmailRequest
import br.com.tln.personalcard.usuario.webservice.response.CardResponse
import br.com.tln.personalcard.usuario.webservice.response.InitializationResponse
import br.com.tln.personalcard.usuario.webservice.response.ProfileResponse
import br.com.tln.personalcard.usuario.webservice.response.TelenetBaseResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface UserService {
    @POST("services/usuario/mobile/inicializaapp")
    suspend fun initialize(
        @Header("Authorization") authorization: String,
        @Body initializationRequest: InitializationRequest
    ): TelenetBaseResponse<InitializationResponse>

    @GET("services/usuario/mobile/perfil")
    suspend fun getProfile(
        @Header("Authorization") accessToken: String
    ): TelenetBaseResponse<ProfileResponse>

    @GET("services/consulta/cartao/cartoesusuario")
    suspend fun getUserCards(
        @Header("Authorization") accessToken: String
    ): TelenetBaseResponse<List<CardResponse>>

    @POST("services/usuario/mobile/alterasenha")
    suspend fun setPassword(
        @Header("Authorization") authorization: String,
        @Body changePasswordRequest: ChangePasswordRequest
    )

    @POST("services/usuario/mobile/alteraperfil")
    suspend fun updateEmail(
        @Header("Authorization") accessToken: String,
        @Body updateEmailRequest: UpdateEmailRequest
    )

    @POST("services/usuario/mobile/alteraperfil")
    suspend fun editProfile(
        @Header("Authorization") accessToken: String,
        @Body editProfileRequest: EditProfileRequest
    )

    @POST("services/usuario/mobile/reiniciasenha")
    suspend fun recoverPassword(
        @Header("Authorization") authorization: String,
        @Body recoveryPasswordRequest: RecoveryPasswordRequest
    )
}