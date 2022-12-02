package br.com.tln.personalcard.usuario.task

import arrow.core.Either
import br.com.tln.personalcard.usuario.USER_DATA_RENEWAL_MINUTES_INTERVAL
import br.com.tln.personalcard.usuario.core.SuccessResource
import br.com.tln.personalcard.usuario.repository.SessionRepository
import br.com.tln.personalcard.usuario.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.threeten.bp.LocalDateTime
import org.threeten.bp.temporal.ChronoUnit
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class RenewUserDataTask @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val userRepository: UserRepository
) {

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    private var running = false

    fun start() {
        if (!running) {
            running = true

            scope.launch {
                startTask()
            }
        }
    }

    private suspend fun startTask() {
        getInitialDelayMinutes()?.let {
            delay(TimeUnit.MINUTES.toMillis(it))
        }

        do {
            renewData()
            delay(TimeUnit.MINUTES.toMillis(USER_DATA_RENEWAL_MINUTES_INTERVAL))
        } while (true)
    }

    private suspend fun getInitialDelayMinutes(): Long? {
        val terminal = sessionRepository.getProfile() ?: return null

        val delay =
            USER_DATA_RENEWAL_MINUTES_INTERVAL - terminal.fetchTime.until(
                LocalDateTime.now(),
                ChronoUnit.MINUTES
            )

        return if (delay <= 0) {
            null
        } else {
            delay
        }
    }

    private suspend fun renewData() {

        val accessToken = sessionRepository.getAccessToken() ?: return


        when (val result = userRepository.updateCards(accessToken)) {
            is Either.Left -> {
                lockSession()
                return
            }
            is Either.Right -> {
                val resource = result.b
                if (resource !is SuccessResource) {
                    lockSession()
                    return
                }
            }
        }

        when (val result = userRepository.updateProfile(accessToken)) {
            is Either.Left -> {
                lockSession()
                return
            }
            is Either.Right -> {
                val resource = result.b
                if (resource !is SuccessResource) {
                    lockSession()
                    return
                }
            }
        }
    }

    private fun lockSession() {
        GlobalScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.IO) {
                sessionRepository.getAccountData()?.let { accountData ->
                    sessionRepository.update(accountData.copy(locked = true))
                }
            } ?: return@launch
        }
    }

    fun stop() {
        job.cancelChildren()
        running = false
    }
}