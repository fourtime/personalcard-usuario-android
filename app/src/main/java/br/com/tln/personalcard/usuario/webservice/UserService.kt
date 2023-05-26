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
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Streaming
import java.io.File

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

    @Streaming
    @GET("services/usuario/mobile/fotoperfil")
    suspend fun getAvatar(
        @Header("Authorization") authorization: String
    ): Response<ResponseBody>

    @Multipart
    @POST("services/usuario/mobile/definefotoperfil")
    suspend fun updateAvatar(
        @Header("Authorization") authorization: String,
        @Part avatar: MultipartBody.Part
    )
}