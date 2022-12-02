package br.com.tln.personalcard.usuario.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Transformations
import arrow.core.Either
import br.com.tln.personalcard.usuario.db.AccessTokenDao
import br.com.tln.personalcard.usuario.db.AccountDataDao
import br.com.tln.personalcard.usuario.db.CardDao
import br.com.tln.personalcard.usuario.db.DeviceDao
import br.com.tln.personalcard.usuario.db.InitializationDataDao
import br.com.tln.personalcard.usuario.db.ProfileDao
import br.com.tln.personalcard.usuario.db.SessionDb
import br.com.tln.personalcard.usuario.entity.AccessToken
import br.com.tln.personalcard.usuario.entity.AccountData
import br.com.tln.personalcard.usuario.entity.Card
import br.com.tln.personalcard.usuario.entity.Device
import br.com.tln.personalcard.usuario.entity.InitializationData
import br.com.tln.personalcard.usuario.entity.Profile
import br.com.tln.personalcard.usuario.exception.ConnectionErrorException
import br.com.tln.personalcard.usuario.exception.InvalidAuthenticationException
import br.com.tln.personalcard.usuario.model.Account
import br.com.tln.personalcard.usuario.model.Session
import br.com.tln.personalcard.usuario.preferences.AppPreferences
import br.com.tln.personalcard.usuario.preferences.SessionPreferences
import br.com.tln.personalcard.usuario.webservice.TelenetService
import br.com.tln.personalcard.usuario.webservice.request.SettingInstanceRequest
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.HttpException
import timber.log.Timber
import java.io.EOFException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionRepository @Inject constructor(
    private val appPreferences: AppPreferences,
    private val sessionPreferences: SessionPreferences,
    private val sessionDb: SessionDb,
    private val accessTokenDao: AccessTokenDao,
    private val initializationDataDao: InitializationDataDao,
    private val accountDataDao: AccountDataDao,
    private val profileDao: ProfileDao,
    private val cardDao: CardDao,
    private val deviceDao: DeviceDao,
    private val telenetService: TelenetService
) {

    private var activeSession: Boolean = false

    private val sessionLiveData: LiveData<Session?>

    init {
        sessionLiveData = createSessionLiveData()
        sessionLiveData.observeForever {
            Timber.i("Session state changed")
            activeSession = it?.account != null

            checkFcmToken(it)
        }
    }

    private fun checkFcmToken(session: Session?) {
        GlobalScope.launch(Dispatchers.IO) {

            if (session?.device == null || session.account == null) {
                return@launch
            }

            if (session.device.token == sessionPreferences.getFcmSentToken()) {
                return@launch
            }

            val settingInstanceRequest = SettingInstanceRequest(
                imei = appPreferences.getDeviceId(),
                instanceId = session.device.token
            )

            settingAppInstance(
                settingInstanceRequest,
                session.account.accessToken,
                session.device.token
            )
        }
    }

    private fun createSessionLiveData(): LiveData<Session?> {
        val liveData = MediatorLiveData<Session?>()

        var initializationDataLoaded = false
        var accountLoaded = false
        var deviceLoaded = false

        var initializationData: InitializationData? = null
        var account: Account? = null
        var device: Device? = null

        liveData.addSource(initializationDataDao.getLiveData()) { data ->
            initializationDataLoaded = true
            initializationData = data

            if (initializationDataLoaded && accountLoaded && deviceLoaded) {
                liveData.postValue(
                    Session(
                        initializationData = initializationData,
                        account = account,
                        device = device
                    )
                )
            }
        }

        liveData.addSource(accountDataDao.getWithRelationsLiveData()) { accountData ->
            accountLoaded = true

            if (accountData != null) {
                account = Account(
                    accessToken = accountData.accessToken,
                    accountData = accountData.accountData,
                    profile = accountData.profile,
                    cards = accountData.cards
                )
            }

            if (initializationDataLoaded && accountLoaded && deviceLoaded) {
                liveData.postValue(
                    Session(
                        initializationData = initializationData,
                        account = account,
                        device = device
                    )
                )
            }
        }

        liveData.addSource(deviceDao.getLiveData()) { data ->
            deviceLoaded = true
            device = data

            if (initializationDataLoaded && accountLoaded && deviceLoaded) {
                liveData.postValue(
                    Session(
                        initializationData = initializationData,
                        account = account,
                        device = device
                    )
                )
            }
        }

        return liveData
    }

    fun getSessionLiveData(allowCache: Boolean = true): LiveData<Session?> {
        return if (allowCache) {
            sessionLiveData
        } else {
            createSessionLiveData()
        }
    }

    fun getInitializationDataLiveData(allowCache: Boolean = true): LiveData<InitializationData?> {
        return Transformations.map(getSessionLiveData(allowCache = allowCache)) {
            it?.initializationData
        }
    }

    fun getAccountLiveData(allowCache: Boolean = true): LiveData<Account?> {
        return Transformations.map(getSessionLiveData(allowCache = allowCache)) {
            it?.account
        }
    }

    fun getAccessTokenLiveData(allowCache: Boolean = true): LiveData<AccessToken?> {
        return Transformations.map(getSessionLiveData(allowCache = allowCache)) {
            it?.account?.accessToken
        }
    }

    fun getAccountDataLiveData(allowCache: Boolean = true): LiveData<AccountData?> {
        return Transformations.map(getSessionLiveData(allowCache = allowCache)) {
            it?.account?.accountData
        }
    }

    fun getProfileLiveData(allowCache: Boolean = true): LiveData<Profile?> {
        return Transformations.map(getSessionLiveData(allowCache = allowCache)) {
            it?.account?.profile
        }
    }

    fun getCardsLiveData(allowCache: Boolean = true): LiveData<List<Card>?> {
        return Transformations.map(getSessionLiveData(allowCache = allowCache)) {
            it?.account?.cards
        }
    }

    fun getDeviceLiveData(allowCache: Boolean = true): LiveData<Device?> {
        return Transformations.map(getSessionLiveData(allowCache = allowCache)) {
            it?.device
        }
    }

    suspend fun settingAppInstance(
        settingInstanceRequest: SettingInstanceRequest,
        accessToken: AccessToken,
        deviceToken: String
    ) {
        try {
            val response = telenetService.settingAppInstance(
                request = settingInstanceRequest,
                authorization = accessToken.formattedToken
            )
            val data = response.results

            sessionPreferences.setFcmSentToken(deviceToken)
        } catch (ex: CancellationException) {
            throw ex
        } catch (ex: HttpException) {
            if (ex.code() == 401) {
                Either.left(InvalidAuthenticationException())
            } else {
                //Either.right(Resource.error())
            }
        } catch (ex: EOFException) {
            Either.left(ex)
        } catch (ex: IOException) {
            Either.left(ConnectionErrorException(ex))
        } catch (t: Throwable) {
            Either.left(t)
        }
    }

    suspend fun getAccount(): Account? {
        val accountData = accountDataDao.getWithRelations()

        return accountData?.let {
            Account(
                accessToken = it.accessToken,
                accountData = it.accountData,
                profile = it.profile,
                cards = it.cards
            )
        }
    }

    suspend fun getInitializationData(): InitializationData? {
        return initializationDataDao.get()
    }

    suspend fun update(initializationData: InitializationData) {
        initializationDataDao.update(initializationData)
    }

    suspend fun getAccessToken(): AccessToken? {
        return accessTokenDao.get()
    }

    suspend fun update(accessToken: AccessToken) {
        accessTokenDao.update(accessToken)
    }

    suspend fun getAccountData(): AccountData? {
        return accountDataDao.get()
    }

    suspend fun update(accountData: AccountData) {
        accountDataDao.update(accountData)
    }

    suspend fun getProfile(): Profile? {
        return profileDao.get()
    }

    suspend fun update(profile: Profile) {
        profileDao.update(profile)
    }

    suspend fun create(cards: List<Card>) {
        cardDao.deleteAll()
        cardDao.insert(cards)
    }

    suspend fun update(card: Card) {
        cardDao.update(card)
    }

    suspend fun create(device: Device) {
        deviceDao.insert(device)
    }

    suspend fun getDevice(): Device? {
        return deviceDao.get()
    }

    suspend fun update(device: Device) {
        deviceDao.update(device)
    }

    suspend fun logout() {
        FirebaseMessaging.getInstance().deleteToken()
        sessionPreferences.clear()
        sessionDb.clearAllTables()
    }
}