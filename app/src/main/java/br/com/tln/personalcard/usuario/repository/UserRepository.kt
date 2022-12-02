package br.com.tln.personalcard.usuario.repository

import arrow.core.Either
import br.com.tln.personalcard.usuario.BuildConfig
import br.com.tln.personalcard.usuario.core.ErrorResource
import br.com.tln.personalcard.usuario.core.LoadingResource
import br.com.tln.personalcard.usuario.core.Resource
import br.com.tln.personalcard.usuario.core.SuccessResource
import br.com.tln.personalcard.usuario.db.AccessTokenDao
import br.com.tln.personalcard.usuario.db.AccountDataDao
import br.com.tln.personalcard.usuario.db.CardDao
import br.com.tln.personalcard.usuario.db.InitializationDataDao
import br.com.tln.personalcard.usuario.db.ProfileDao
import br.com.tln.personalcard.usuario.db.SessionDb
import br.com.tln.personalcard.usuario.entity.AccessToken
import br.com.tln.personalcard.usuario.entity.AccountData
import br.com.tln.personalcard.usuario.entity.Address
import br.com.tln.personalcard.usuario.entity.Card
import br.com.tln.personalcard.usuario.entity.CardType
import br.com.tln.personalcard.usuario.entity.InitializationData
import br.com.tln.personalcard.usuario.entity.Profile
import br.com.tln.personalcard.usuario.exception.ConnectionErrorException
import br.com.tln.personalcard.usuario.exception.InvalidAuthenticationException
import br.com.tln.personalcard.usuario.extensions.CryptographyUtils.encrypt
import br.com.tln.personalcard.usuario.extensions.responseError
import br.com.tln.personalcard.usuario.extensions.toLocalDate
import br.com.tln.personalcard.usuario.extensions.toLocalDateTime
import br.com.tln.personalcard.usuario.model.Account
import br.com.tln.personalcard.usuario.preferences.AppPreferences
import br.com.tln.personalcard.usuario.webservice.TelenetService
import br.com.tln.personalcard.usuario.webservice.UserService
import br.com.tln.personalcard.usuario.webservice.request.ChangePasswordRequest
import br.com.tln.personalcard.usuario.webservice.request.EditProfileRequest
import br.com.tln.personalcard.usuario.webservice.request.InitializationRequest
import br.com.tln.personalcard.usuario.webservice.request.RecoveryPasswordRequest
import br.com.tln.personalcard.usuario.webservice.request.UpdateEmailRequest
import br.com.tln.personalcard.usuario.webservice.response.ErrorBodyResponse
import com.squareup.moshi.JsonEncodingException
import com.squareup.moshi.Moshi
import kotlinx.coroutines.CancellationException
import org.threeten.bp.LocalDateTime
import retrofit2.HttpException
import java.io.EOFException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val appPreferences: AppPreferences,
    private val sessionDb: SessionDb,
    private val accessTokenDao: AccessTokenDao,
    private val initializationDataDao: InitializationDataDao,
    private val accountDataDao: AccountDataDao,
    private val profileDao: ProfileDao,
    private val cardDao: CardDao,
    private val telenetService: TelenetService,
    private val userService: UserService,
    private val moshi: Moshi
) {

    suspend fun initialize(
        cpf: String,
        cardLastDigits: String
    ): Either<Throwable, Resource<InitializationData, String?>> {

        val applicationToken = when (val result = applicationTokenRequest()) {
            is Either.Left -> {
                return Either.left(result.a)
            }
            is Either.Right -> {
                val resource: Resource<AccessToken, String?> = result.b
                if (resource is SuccessResource) {
                    resource.data.formattedToken
                } else {
                    return Either.right(
                        ErrorResource(data = null as String?)
                    )
                }
            }
        }

        val deviceId = appPreferences.getDeviceId()

        val initializationData = when (val result = initializeRequest(
            applicationToken = applicationToken,
            cpf = cpf,
            cardLastDigits = cardLastDigits,
            deviceId = deviceId
        )) {
            is Either.Left -> {
                return Either.left(result.a)
            }
            is Either.Right -> {
                val resource = result.b

                if (resource is SuccessResource) {
                    resource.data
                } else {
                    return Either.right(resource)
                }
            }
        }

        sessionDb.runInTransaction {
            initializationDataDao.insert(initializationData)
        }

        if (initializationData.firstAccess) {

            val loginResult = login(cpf = cpf, password = cpf.take(8))

            when (loginResult) {
                is Either.Left -> {
                    return Either.left(loginResult.a)
                }
                is Either.Right -> {
                    val resource = loginResult.b

                    val x = when (resource) {
                        is SuccessResource -> {
                        }
                        is LoadingResource -> return Either.right(LoadingResource(message = resource.message))
                        is ErrorResource -> return Either.right(ErrorResource(data = resource.data))
                    }
                }
            }
        }

        return Either.right(SuccessResource(initializationData))
    }


    suspend fun login(
        cpf: String,
        password: String
    ): Either<Throwable, Resource<Account, String?>> {

        val deviceId = appPreferences.getDeviceId()
        val username = "${BuildConfig.OPERATOR_CODE}-$deviceId@$cpf"

        val accessToken = when (val result = accessTokenRequest(
            username = username,
            password = password
        )) {
            is Either.Left -> {
                return Either.left(result.a)
            }
            is Either.Right -> {
                val resource: Resource<AccessToken, Nothing?> = result.b
                if (resource is SuccessResource) {
                    resource.data
                } else {
                    return Either.right(
                        ErrorResource(data = null as String?)
                    )
                }
            }
        }

        val profile = when (val result = usserProfileData(
            accessToken = accessToken.formattedToken
        )) {
            is Either.Left -> {
                return Either.left(result.a)
            }
            is Either.Right -> {
                val resource: Resource<Profile, Nothing?> = result.b
                if (resource is SuccessResource) {
                    resource.data
                } else {
                    return Either.right(
                        ErrorResource(data = null as String?)
                    )
                }
            }
        }

        val cards = when (val result = userCardsRequest(
            accessToken = accessToken.formattedToken
        )) {
            is Either.Left -> {
                return Either.left(result.a)
            }
            is Either.Right -> {
                val resource: Resource<List<Card>, Nothing?> = result.b
                if (resource is SuccessResource) {
                    resource.data
                } else {
                    return Either.right(
                        ErrorResource(data = null as String?)
                    )
                }
            }
        }

        val accountData = AccountData(
            username = cpf,
            password = password.encrypt(
                key = BuildConfig.DEFAULT_CRYPTOGRAPHY_KEY,
                initializationVector = BuildConfig.DEFAULT_CRYPTOGRAPHY_INITIALIZATION_VECTOR,
                transformation = BuildConfig.DEFAULT_CRYPTOGRAPHY_TRANSFORMATION,
                algorithm = BuildConfig.DEFAULT_CRYPTOGRAPHY_ALGORITHM
            )
        )

        sessionDb.runInTransaction {
            accountDataDao.insert(accountData)

            accessTokenDao.insert(accessToken)
            profileDao.insert(profile)
            cardDao.insert(cards)
        }

        return Either.right(
            SuccessResource(
                data = Account(
                    accessToken = accessToken,
                    accountData = accountData,
                    profile = profile,
                    cards = cards
                )
            )
        )
    }

    suspend fun changePassword(
        accessToken: AccessToken,
        request: ChangePasswordRequest
    ): Either<Throwable, Resource<Nothing?, String?>> {

        val response = when (val result = changePasswordRequest(
            accessToken = accessToken.formattedToken,
            request = request
        )) {
            is Either.Left -> {
                return Either.left(result.a)
            }
            is Either.Right -> {
                val resource: Resource<Nothing?, String?> = result.b
                if (resource is SuccessResource) {
                    resource.data
                } else {
                    return Either.right(resource)
                }
            }
        }

        accountDataDao.get()?.let {
            accountDataDao.update(it.copy(password = request.newPassword.encrypt()))
        }

        return Either.right(SuccessResource(data = null))
    }

    suspend fun editProfile(
        accessToken: AccessToken,
        request: EditProfileRequest
    ): Either<Throwable, Resource<Nothing?, String?>> {

        val response = when (val result = editProfileRequest(
            accessToken = accessToken.formattedToken,
            request = request
        )) {
            is Either.Left -> {
                return Either.left(result.a)
            }
            is Either.Right -> {
                val resource: Resource<Nothing?, String?> = result.b
                if (resource is SuccessResource) {
                    resource.data
                } else {
                    return Either.right(resource)
                }
            }
        }

        profileDao.get()?.apply {
            profileDao.update(
                copy(
                    email = request.email,
                    phone = request.phone,
                    birthDate = request.birthDate.toLocalDate()
                )
            )
        }

        return Either.right(SuccessResource(data = null))
    }

    suspend fun updateEmail(
        accessToken: AccessToken,
        request: UpdateEmailRequest
    ): Either<Throwable, Resource<Nothing?, Nothing?>> {

        val response = when (val result = updateEmailRequest(
            accessToken = accessToken.formattedToken,
            request = request
        )) {
            is Either.Left -> {
                return Either.left(result.a)
            }
            is Either.Right -> {
                val resource: Resource<Nothing?, Nothing?> = result.b
                if (resource is SuccessResource) {
                    resource.data
                } else {
                    return Either.right(
                        ErrorResource(null)
                    )
                }
            }
        }

        profileDao.get()?.apply {
            profileDao.update(copy(email = request.email))
        }

        return Either.right(SuccessResource(data = null))
    }

    suspend fun updateProfile(
        accessToken: AccessToken
    ): Either<Throwable, Resource<Nothing?, Nothing?>> {

        val profile = when (val result = usserProfileData(
            accessToken = accessToken.formattedToken
        )) {
            is Either.Left -> {
                return Either.left(result.a)
            }
            is Either.Right -> {
                val resource: Resource<Profile, Nothing?> = result.b
                if (resource is SuccessResource) {
                    resource.data
                } else {
                    return Either.right(
                        ErrorResource(null)
                    )
                }
            }
        }

        profileDao.update(profile)

        return Either.right(SuccessResource(data = null))
    }

    suspend fun updateCards(
        accessToken: AccessToken
    ): Either<Throwable, Resource<Nothing?, Nothing?>> {

        val cards = when (val result = userCardsRequest(
            accessToken = accessToken.formattedToken
        )) {
            is Either.Left -> {
                return Either.left(result.a)
            }
            is Either.Right -> {
                val resource: Resource<List<Card>, Nothing?> = result.b
                if (resource is SuccessResource) {
                    resource.data
                } else {
                    return Either.right(
                        ErrorResource(null)
                    )
                }
            }
        }

        cardDao.update(cards)

        return Either.right(SuccessResource(data = null))
    }

    suspend fun recoveryPassword(
        recoveryPasswordRequest: RecoveryPasswordRequest
    ): Either<Throwable, Resource<Nothing?, String?>> {

        val applicationToken = when (val result = applicationTokenRequest()) {
            is Either.Left -> {
                return Either.left(result.a)
            }
            is Either.Right -> {
                val resource: Resource<AccessToken, String?> = result.b
                if (resource is SuccessResource) {
                    resource.data.formattedToken
                } else {
                    return Either.right(
                        ErrorResource(data = null as String?)
                    )
                }
            }
        }

        val response = when (val result = recoveryPasswordRequest(
            applicationToken = applicationToken,
            recoveryPasswordRequest = recoveryPasswordRequest
        )) {
            is Either.Left -> {
                return Either.left(result.a)
            }
            is Either.Right -> {
                val resource: Resource<Nothing?, String?> = result.b
                if (resource is SuccessResource) {
                    resource.data
                } else {
                    return Either.right((resource))
                }
            }
        }

        return Either.right(SuccessResource(data = null))
    }


    private suspend fun applicationTokenRequest(): Either<Throwable, Resource<AccessToken, String?>> {
        val response = try {
            telenetService.getApplicationToken()
        } catch (ex: CancellationException) {
            throw ex
        } catch (ex: KotlinNullPointerException) {
            return Either.left(InvalidAuthenticationException())
        } catch (ex: HttpException) {
            val errorBodyResponse: ErrorBodyResponse? = moshi.responseError(ex.response())

            return if (ex.code() == 400) {
                Either.left(InvalidAuthenticationException())
            } else if (ex.code() == 409 && errorBodyResponse != null) {
                Either.right(ErrorResource(errorBodyResponse.message))
            } else {
                Either.right(ErrorResource(data = null as String?))
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

        val accessToken = AccessToken(
            type = response.type,
            value = response.accessToken,
            scope = response.scope,
            expiration = LocalDateTime.now().plusSeconds(response.expiresIn)
        )

        return Either.right(SuccessResource(accessToken))
    }

    private suspend fun initializeRequest(
        applicationToken: String,
        cpf: String,
        cardLastDigits: String,
        deviceId: String
    ): Either<Throwable, Resource<InitializationData, String?>> {

        val initializationRequest = InitializationRequest(
            operatorCode = BuildConfig.OPERATOR_CODE,
            cpf = cpf,
            cardLastDigits = cardLastDigits,
            deviceId = deviceId
        )

        val initializationResponse = try {
            userService.initialize(applicationToken, initializationRequest)
        } catch (ex: CancellationException) {
            throw ex
        } catch (ex: KotlinNullPointerException) {
            return Either.left(InvalidAuthenticationException())
        } catch (ex: HttpException) {

            val errorBodyResponse: ErrorBodyResponse? = moshi.responseError(ex.response())

            return if (ex.code() == 400) {
                Either.left(InvalidAuthenticationException())
            } else if (ex.code() == 409 && errorBodyResponse != null) {
                Either.right(ErrorResource(errorBodyResponse.message))
            } else {
                Either.right(ErrorResource(data = null as String?))
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

        val initializationData = InitializationData(
            cpf = cpf,
            name = initializationResponse.results.userName,
            hasEmail = initializationResponse.results.hasEmail,
            firstAccess = initializationResponse.results.firstAccess
        )

        return Either.right(SuccessResource(initializationData))
    }

    private suspend fun accessTokenRequest(
        username: String,
        password: String
    ): Either<Throwable, Resource<AccessToken, Nothing?>> {
        val response = try {
            telenetService.getAccessToken(username = username, password = password)
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

        val accessToken = AccessToken(
            type = response.type,
            value = response.accessToken,
            scope = response.scope,
            expiration = LocalDateTime.now().plusSeconds(response.expiresIn)
        )

        return Either.right(SuccessResource(accessToken))
    }

    private suspend fun usserProfileData(accessToken: String): Either<Throwable, Resource<Profile, Nothing?>> {

        val userProfileResponse = try {
            userService.getProfile(accessToken = accessToken)
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

        val address = Address(
            state = userProfileResponse.results.uf,
            city = userProfileResponse.results.city,
            neighborhood = userProfileResponse.results.neighborhood,
            street = "",
            number = userProfileResponse.results.numero,
            complement = userProfileResponse.results.complement,
            postalCode = userProfileResponse.results.cep
        )

        val profile = Profile(
            email = userProfileResponse.results.email,
            phone = userProfileResponse.results.phone,
            picture = "",
            birthDate = userProfileResponse.results.birthDate?.toLocalDateTime()?.toLocalDate(),
            address = address,
            fetchTime = LocalDateTime.now()
        )

        return Either.right(SuccessResource(profile))
    }

    private suspend fun userCardsRequest(accessToken: String): Either<Throwable, Resource<List<Card>, Nothing?>> {

        val response = try {
            userService.getUserCards(accessToken = accessToken)
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

        val cardResponse = response.results.map {

            var type: CardType? = CardType.fromId(it.type)
            if (type == null) {
                type = CardType.FLEET
            }

            Card(
                id = it.idCard,
                type = type,
                label = it.label,
                lastDigits = it.cardLastDigits,
                balance = it.balance,
                averangeDailyBalance = it.scheduledLoadvalue,
                rechargeDate = it.rechargeDate?.toLocalDateTime(),
                renovationDate = it.renovationDate?.toLocalDateTime()
            )
        }

        return Either.right(SuccessResource(cardResponse))
    }

    private suspend fun changePasswordRequest(
        accessToken: String,
        request: ChangePasswordRequest
    ): Either<Throwable, Resource<Nothing?, String?>> {

        val response = try {
            userService.setPassword(authorization = accessToken, changePasswordRequest = request)
        } catch (ex: CancellationException) {
            throw ex
        } catch (ex: KotlinNullPointerException) {
            return Either.left(InvalidAuthenticationException())
        } catch (ex: HttpException) {

            val errorBodyResponse: ErrorBodyResponse? = moshi.responseError(ex.response())

            return if (ex.code() == 400) {
                Either.left(InvalidAuthenticationException())
            } else if (ex.code() == 409 && errorBodyResponse != null) {
                Either.right(ErrorResource(errorBodyResponse.message))
            } else {
                Either.right(ErrorResource(data = null as String?))
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

        return Either.right(SuccessResource(data = null))
    }

    private suspend fun editProfileRequest(
        accessToken: String,
        request: EditProfileRequest
    ): Either<Throwable, Resource<Nothing?, String?>> {

        val response = try {
            userService.editProfile(accessToken = accessToken, editProfileRequest = request)
        } catch (ex: CancellationException) {
            throw ex
        } catch (ex: KotlinNullPointerException) {
            return Either.left(InvalidAuthenticationException())
        } catch (ex: HttpException) {

            val errorBodyResponse: ErrorBodyResponse? = moshi.responseError(ex.response())

            return if (ex.code() == 400) {
                Either.left(InvalidAuthenticationException())
            } else if (ex.code() == 409 && errorBodyResponse != null) {
                Either.right(ErrorResource(errorBodyResponse.message))
            } else {
                Either.right(ErrorResource(data = null as String?))
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

        return Either.right(SuccessResource(data = null))
    }

    private suspend fun updateEmailRequest(
        accessToken: String,
        request: UpdateEmailRequest
    ): Either<Throwable, Resource<Nothing?, Nothing?>> {

        val response = try {
            userService.updateEmail(accessToken = accessToken, updateEmailRequest = request)
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

        return Either.right(SuccessResource(data = null))
    }

    private suspend fun recoveryPasswordRequest(
        applicationToken: String,
        recoveryPasswordRequest: RecoveryPasswordRequest
    ): Either<Throwable, Resource<Nothing?, String?>> {

        val response = try {
            userService.recoverPassword(
                applicationToken,
                recoveryPasswordRequest = recoveryPasswordRequest
            )
        } catch (ex: CancellationException) {
            throw ex
        } catch (ex: HttpException) {
            val errorBodyResponse: ErrorBodyResponse? = moshi.responseError(ex.response())

            return if (ex.code() == 400) {
                Either.left(InvalidAuthenticationException())
            } else if (ex.code() == 409 && errorBodyResponse != null) {
                Either.right(ErrorResource(errorBodyResponse.message))
            } else {
                Either.right(ErrorResource(data = null as String?))
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

        return Either.right(SuccessResource(data = null))
    }
}