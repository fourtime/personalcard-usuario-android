package br.com.tln.personalcard.usuario.repository

import arrow.core.Either
import br.com.tln.personalcard.usuario.core.ErrorResource
import br.com.tln.personalcard.usuario.core.Resource
import br.com.tln.personalcard.usuario.core.SuccessResource
import br.com.tln.personalcard.usuario.entity.AccessToken
import br.com.tln.personalcard.usuario.exception.ConnectionErrorException
import br.com.tln.personalcard.usuario.exception.InvalidAuthenticationException
import br.com.tln.personalcard.usuario.model.AccreditedNetwork
import br.com.tln.personalcard.usuario.model.AccreditedNetworkGeoLocation
import br.com.tln.personalcard.usuario.model.ActivityBranch
import br.com.tln.personalcard.usuario.model.City
import br.com.tln.personalcard.usuario.model.Neighborhood
import br.com.tln.personalcard.usuario.model.Segment
import br.com.tln.personalcard.usuario.model.Specialty
import br.com.tln.personalcard.usuario.model.UF
import br.com.tln.personalcard.usuario.webservice.AccreditedNetworkService
import br.com.tln.personalcard.usuario.webservice.request.AccreditedNetworkFilterRequest
import br.com.tln.personalcard.usuario.webservice.request.AccreditedNetworkGeoLocationRequest
import br.com.tln.personalcard.usuario.webservice.request.ActivityBranchRequest
import br.com.tln.personalcard.usuario.webservice.request.CityRequest
import br.com.tln.personalcard.usuario.webservice.request.NeighborhoodRequest
import br.com.tln.personalcard.usuario.webservice.request.SegmentRequest
import br.com.tln.personalcard.usuario.webservice.request.SpecialtyRequest
import br.com.tln.personalcard.usuario.webservice.request.UFRequest
import br.com.tln.personalcard.usuario.webservice.response.ListResponse
import com.squareup.moshi.JsonEncodingException
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.EOFException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccreditedNetworkRepository @Inject constructor(
    private val accreditedNetworkService: AccreditedNetworkService
) {

    suspend fun getAccreditedNetwork(
        accessToken: AccessToken,
        code: String
    ): Either<Throwable, Resource<AccreditedNetwork, Nothing?>> {
        return getAccreditedNetworkDetailRequest(
            accessToken = accessToken,
            code = code
        )
    }

    suspend fun getAccreditedNetworksGeoLocation(
        accessToken: AccessToken,
        request: AccreditedNetworkGeoLocationRequest
    ): Either<Throwable, Resource<List<AccreditedNetworkGeoLocation>, Nothing?>> {
        return getAccreditedNetworksGeoLocationRequest(
            accessToken = accessToken,
            request = request
        )
    }

    suspend fun getAccreditedNetworkFilter(
        accessToken: AccessToken,
        request: AccreditedNetworkFilterRequest
    ): Either<Throwable, Resource<ListResponse<AccreditedNetwork>, Nothing?>> {
        return getAccreditedNetworkRequest(
            accessToken = accessToken,
            request = request
        )
    }

    suspend fun getSegment(
        accessToken: AccessToken,
        request: SegmentRequest
    ): Either<Throwable, Resource<List<Segment>, Nothing?>> {
        return getSegmentRequest(
            accessToken = accessToken,
            request = request
        )
    }

    suspend fun getActivityBranch(
        accessToken: AccessToken,
        request: ActivityBranchRequest
    ): Either<Throwable, Resource<List<ActivityBranch>, Nothing?>> {
        return getActivityBranchRequest(
            accessToken = accessToken,
            request = request
        )
    }

    suspend fun getSpecialty(
        accessToken: AccessToken,
        request: SpecialtyRequest
    ): Either<Throwable, Resource<List<Specialty>, Nothing?>> {
        return getSpecialtyRequest(
            accessToken = accessToken,
            request = request
        )
    }

    suspend fun getUF(
        accessToken: AccessToken,
        request: UFRequest
    ): Either<Throwable, Resource<List<UF>, Nothing?>> {
        return getUFRequest(
            accessToken = accessToken,
            request = request
        )
    }

    suspend fun getCity(
        accessToken: AccessToken,
        request: CityRequest
    ): Either<Throwable, Resource<List<City>, Nothing?>> {
        return getCityRequest(
            accessToken = accessToken,
            request = request
        )
    }

    suspend fun getNeighborhood(
        accessToken: AccessToken,
        request: NeighborhoodRequest
    ): Either<Throwable, Resource<List<Neighborhood>, Nothing?>> {
        return getNeighborhoodRequest(
            accessToken = accessToken,
            request = request
        )
    }

    private suspend fun getSegmentRequest(
        accessToken: AccessToken,
        request: SegmentRequest
    ): Either<Throwable, Resource<List<Segment>, Nothing?>> {

        val response = try {
            accreditedNetworkService.getSegment(
                accessToken = accessToken.formattedToken,
                segmentRequest = request
            )
        } catch (ex: CancellationException) {
            throw ex
        } catch (ex: KotlinNullPointerException) {
            return Either.left(InvalidAuthenticationException())
        } catch (ex: HttpException) {
            return if (ex.code() == 400) {
                Either.left(InvalidAuthenticationException())
            } else {
                Either.right(ErrorResource(null))
            }
        } catch (ex: EOFException) {
            return Either.left(ex)
        } catch (ex: JsonEncodingException) {
            return Either.left(ex)
        } catch (ex: IOException) {
            return Either.left(ConnectionErrorException(ex))
        } catch (t: Throwable) {
            return Either.left(t)
        }

        val segmentResponse = response.results.map {

            Segment(
                name = it.name,
                type = it.type
            )
        }

        return Either.right(SuccessResource(segmentResponse))
    }

    private suspend fun getActivityBranchRequest(
        accessToken: AccessToken,
        request: ActivityBranchRequest
    ): Either<Throwable, Resource<List<ActivityBranch>, Nothing?>> {

        val response = try {
            accreditedNetworkService.getActivityBranch(
                accessToken = accessToken.formattedToken,
                activityBranchRequest = request
            )
        } catch (ex: CancellationException) {
            throw ex
        } catch (ex: KotlinNullPointerException) {
            return Either.left(InvalidAuthenticationException())
        } catch (ex: HttpException) {
            return if (ex.code() == 400) {
                Either.left(InvalidAuthenticationException())
            } else {
                Either.right(ErrorResource(null))
            }
        } catch (ex: EOFException) {
            return Either.left(ex)
        } catch (ex: JsonEncodingException) {
            return Either.left(ex)
        } catch (ex: IOException) {
            return Either.left(ConnectionErrorException(ex))
        } catch (t: Throwable) {
            return Either.left(t)
        }

        val activityBranchResponse = response.results.map {
            ActivityBranch(
                activityBranch = it
            )
        }

        return Either.right(SuccessResource(activityBranchResponse))
    }

    private suspend fun getSpecialtyRequest(
        accessToken: AccessToken,
        request: SpecialtyRequest
    ): Either<Throwable, Resource<List<Specialty>, Nothing?>> {

        val response = try {
            accreditedNetworkService.getSpecialty(
                accessToken = accessToken.formattedToken,
                specialtyRequest = request
            )
        } catch (ex: CancellationException) {
            throw ex
        } catch (ex: KotlinNullPointerException) {
            return Either.left(InvalidAuthenticationException())
        } catch (ex: HttpException) {
            return if (ex.code() == 400) {
                Either.left(InvalidAuthenticationException())
            } else {
                Either.right(ErrorResource(null))
            }
        } catch (ex: EOFException) {
            return Either.left(ex)
        } catch (ex: JsonEncodingException) {
            return Either.left(ex)
        } catch (ex: IOException) {
            return Either.left(ConnectionErrorException(ex))
        } catch (t: Throwable) {
            return Either.left(t)
        }

        val specialtyResponse = response.results.map {
            Specialty(
                specialty = it
            )
        }

        return Either.right(SuccessResource(specialtyResponse))
    }

    private suspend fun getUFRequest(
        accessToken: AccessToken,
        request: UFRequest
    ): Either<Throwable, Resource<List<UF>, Nothing?>> {

        val response = try {
            accreditedNetworkService.getUF(
                accessToken = accessToken.formattedToken,
                ufRequest = request
            )
        } catch (ex: CancellationException) {
            throw ex
        } catch (ex: KotlinNullPointerException) {
            return Either.left(InvalidAuthenticationException())
        } catch (ex: HttpException) {
            return if (ex.code() == 400) {
                Either.left(InvalidAuthenticationException())
            } else {
                Either.right(ErrorResource(null))
            }
        } catch (ex: EOFException) {
            return Either.left(ex)
        } catch (ex: JsonEncodingException) {
            return Either.left(ex)
        } catch (ex: IOException) {
            return Either.left(ConnectionErrorException(ex))
        } catch (t: Throwable) {
            return Either.left(t)
        }

        val ufResponse = response.results.map {
            UF(
                uf = it
            )
        }

        return Either.right(SuccessResource(ufResponse))
    }

    private suspend fun getCityRequest(
        accessToken: AccessToken,
        request: CityRequest
    ): Either<Throwable, Resource<List<City>, Nothing?>> {

        val response = try {
            accreditedNetworkService.getCity(
                accessToken = accessToken.formattedToken,
                cityRequest = request
            )
        } catch (ex: CancellationException) {
            throw ex
        } catch (ex: KotlinNullPointerException) {
            return Either.left(InvalidAuthenticationException())
        } catch (ex: HttpException) {
            return if (ex.code() == 400) {
                Either.left(InvalidAuthenticationException())
            } else {
                Either.right(ErrorResource(null))
            }
        } catch (ex: EOFException) {
            return Either.left(ex)
        } catch (ex: JsonEncodingException) {
            return Either.left(ex)
        } catch (ex: IOException) {
            return Either.left(ConnectionErrorException(ex))
        } catch (t: Throwable) {
            return Either.left(t)
        }

        val cityResponse = response.results.map {
            City(
                city = it
            )
        }

        return Either.right(SuccessResource(cityResponse))
    }

    private suspend fun getNeighborhoodRequest(
        accessToken: AccessToken,
        request: NeighborhoodRequest
    ): Either<Throwable, Resource<List<Neighborhood>, Nothing?>> {

        val response = try {
            accreditedNetworkService.getNeighborhood(
                accessToken = accessToken.formattedToken,
                neighborhoodRequest = request
            )
        } catch (ex: CancellationException) {
            throw ex
        } catch (ex: KotlinNullPointerException) {
            return Either.left(InvalidAuthenticationException())
        } catch (ex: HttpException) {
            return if (ex.code() == 400) {
                Either.left(InvalidAuthenticationException())
            } else {
                Either.right(ErrorResource(null))
            }
        } catch (ex: EOFException) {
            return Either.left(ex)
        } catch (ex: JsonEncodingException) {
            return Either.left(ex)
        } catch (ex: IOException) {
            return Either.left(ConnectionErrorException(ex))
        } catch (t: Throwable) {
            return Either.left(t)
        }

        val neighborhoodResponse = response.results.map {
            Neighborhood(
                neighborhood = it
            )
        }

        return Either.right(SuccessResource(neighborhoodResponse))
    }

    private suspend fun getAccreditedNetworkRequest(
        accessToken: AccessToken,
        request: AccreditedNetworkFilterRequest
    ): Either<Throwable, Resource<ListResponse<AccreditedNetwork>, Nothing?>> {

        val response = try {
            accreditedNetworkService.getAccreditedNetwork(
                accessToken = accessToken.formattedToken,
                accreditedNetworkFilterRequest = request
            )
        } catch (ex: CancellationException) {
            throw ex
        } catch (ex: KotlinNullPointerException) {
            return Either.left(InvalidAuthenticationException())
        } catch (ex: HttpException) {
            return if (ex.code() == 400) {
                Either.left(InvalidAuthenticationException())
            } else {
                Either.right(ErrorResource(null))
            }
        } catch (ex: EOFException) {
            return Either.left(ex)
        } catch (ex: JsonEncodingException) {
            return Either.left(ex)
        } catch (ex: IOException) {
            return Either.left(ConnectionErrorException(ex))
        } catch (t: Throwable) {
            return Either.left(t)
        }

        val accreditedNetworkResponse = response.results.list.map {
            AccreditedNetwork(
                id = it.code,
                fantasyName = it.fantasyName,
                address = it.address,
                complement = it.complement,
                city = it.city,
                neighborhood = it.neighborhood,
                uf = it.uf,
                latitude = it.latitude,
                longitude = it.longitude,
                phone = it.phone
            )
        }

        val listModel = ListResponse(
            pageNumber = response.results.pageNumber,
            pageSize = response.results.pageSize,
            totalSize = response.results.totalSize,
            list = accreditedNetworkResponse
        )

        return Either.right(SuccessResource(listModel))
    }

    private suspend fun getAccreditedNetworksGeoLocationRequest(
        accessToken: AccessToken,
        request: AccreditedNetworkGeoLocationRequest
    ): Either<Throwable, Resource<List<AccreditedNetworkGeoLocation>, Nothing?>> {

        val response = try {
            accreditedNetworkService.getAccreditedNetworkGeoLocation(
                accessToken = accessToken.formattedToken,
                accreditedNetworkGeoLocationRequest = request
            )
        } catch (ex: CancellationException) {
            throw ex
        } catch (ex: KotlinNullPointerException) {
            return Either.left(InvalidAuthenticationException())
        } catch (ex: HttpException) {
            return if (ex.code() == 400) {
                Either.left(InvalidAuthenticationException())
            } else {
                Either.right(ErrorResource(null))
            }
        } catch (ex: EOFException) {
            return Either.left(ex)
        } catch (ex: JsonEncodingException) {
            return Either.left(ex)
        } catch (ex: IOException) {
            return Either.left(ConnectionErrorException(ex))
        } catch (t: Throwable) {
            return Either.left(t)
        }

        val accreditedNetworkResponse = response.results.map {
            AccreditedNetworkGeoLocation(
                code = it.code,
                latitude = it.latitude,
                longitude = it.longitude
            )
        }

        return Either.right(SuccessResource(accreditedNetworkResponse))
    }

    private suspend fun getAccreditedNetworkDetailRequest(
        accessToken: AccessToken,
        code: String
    ): Either<Throwable, Resource<AccreditedNetwork, Nothing?>> {

        val response = try {
            accreditedNetworkService.getAccreditedNetworkGeoLocationDetail(
                accessToken = accessToken.formattedToken,
                code = code
            )
        } catch (ex: CancellationException) {
            throw ex
        } catch (ex: KotlinNullPointerException) {
            return Either.left(InvalidAuthenticationException())
        } catch (ex: HttpException) {
            return if (ex.code() == 400) {
                Either.left(InvalidAuthenticationException())
            } else {
                Either.right(ErrorResource(null))
            }
        } catch (ex: EOFException) {
            return Either.left(ex)
        } catch (ex: JsonEncodingException) {
            return Either.left(ex)
        } catch (ex: IOException) {
            return Either.left(ConnectionErrorException(ex))
        } catch (t: Throwable) {
            return Either.left(t)
        }

        val accreditedNetwork = AccreditedNetwork(
            id = response.results.id,
            fantasyName = response.results.fantasyName,
            address = response.results.address,
            complement = response.results.complement,
            city = response.results.city,
            neighborhood = response.results.neighborhood,
            uf = response.results.uf,
            latitude = response.results.latitude,
            longitude = response.results.longitude,
            phone = response.results.phone
        )

        return Either.right(SuccessResource(accreditedNetwork))
    }
}
