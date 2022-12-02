package br.com.tln.personalcard.usuario.webservice

import br.com.tln.personalcard.usuario.webservice.request.AccreditedNetworkFilterRequest
import br.com.tln.personalcard.usuario.webservice.request.AccreditedNetworkGeoLocationRequest
import br.com.tln.personalcard.usuario.webservice.request.ActivityBranchRequest
import br.com.tln.personalcard.usuario.webservice.request.CityRequest
import br.com.tln.personalcard.usuario.webservice.request.NeighborhoodRequest
import br.com.tln.personalcard.usuario.webservice.request.SegmentRequest
import br.com.tln.personalcard.usuario.webservice.request.SpecialtyRequest
import br.com.tln.personalcard.usuario.webservice.request.UFRequest
import br.com.tln.personalcard.usuario.webservice.response.AccreditedNetworkFilterResponse
import br.com.tln.personalcard.usuario.webservice.response.AccreditedNetworkGeoLocationResponse
import br.com.tln.personalcard.usuario.webservice.response.AccreditedNetworkResponse
import br.com.tln.personalcard.usuario.webservice.response.ListResponse
import br.com.tln.personalcard.usuario.webservice.response.SegmentResponse
import br.com.tln.personalcard.usuario.webservice.response.TelenetBaseResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface AccreditedNetworkService {

    @POST("services/consulta/cartao/segmentos")
    suspend fun getSegment(
        @Header("Authorization") accessToken: String,
        @Body segmentRequest: SegmentRequest
    ): TelenetBaseResponse<List<SegmentResponse>>

    @POST("services/consulta/cartao/atividades")
    suspend fun getActivityBranch(
        @Header("Authorization") accessToken: String,
        @Body activityBranchRequest: ActivityBranchRequest
    ): TelenetBaseResponse<List<String>>

    @POST("services/consulta/cartao/especialidades")
    suspend fun getSpecialty(
        @Header("Authorization") accessToken: String,
        @Body specialtyRequest: SpecialtyRequest
    ): TelenetBaseResponse<List<String>>

    @POST("services/consulta/cartao/ufs")
    suspend fun getUF(
        @Header("Authorization") accessToken: String,
        @Body ufRequest: UFRequest
    ): TelenetBaseResponse<List<String>>

    @POST("services/consulta/cartao/cidades")
    suspend fun getCity(
        @Header("Authorization") accessToken: String,
        @Body cityRequest: CityRequest
    ): TelenetBaseResponse<List<String>>

    @POST("services/consulta/cartao/bairros")
    suspend fun getNeighborhood(
        @Header("Authorization") accessToken: String,
        @Body neighborhoodRequest: NeighborhoodRequest
    ): TelenetBaseResponse<List<String>>

    @POST("services/consulta/cartao/redecredenciada")
    suspend fun getAccreditedNetwork(
        @Header("Authorization") accessToken: String,
        @Body accreditedNetworkFilterRequest: AccreditedNetworkFilterRequest
    ): TelenetBaseResponse<ListResponse<AccreditedNetworkFilterResponse>>

    @POST("services/consulta/cartao/redecredenciadagl")
    suspend fun getAccreditedNetworkGeoLocation(
        @Header("Authorization") accessToken: String,
        @Body accreditedNetworkGeoLocationRequest: AccreditedNetworkGeoLocationRequest
    ): TelenetBaseResponse<List<AccreditedNetworkGeoLocationResponse>>

    @GET("services/consulta/cartao/dadoscredenciado/{codigo}")
    suspend fun getAccreditedNetworkGeoLocationDetail(
        @Header("Authorization") accessToken: String,
        @Path("codigo") code: String
    ): TelenetBaseResponse<AccreditedNetworkResponse>
}