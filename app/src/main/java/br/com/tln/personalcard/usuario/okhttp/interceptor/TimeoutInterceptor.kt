package br.com.tln.personalcard.usuario.okhttp.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber
import java.io.IOException
import java.net.SocketTimeoutException
import javax.inject.Inject

class TimeoutInterceptor @Inject constructor() : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response? {
        val request = chain.request()

        var response: Response? = null

        try {
            response = chain.proceed(request)
        } catch (e: SocketTimeoutException) {
            if (!shouldHandleTimeouts()) {
                throw e
            }
        }

        var retryCount = 0

        while (response == null && shouldRetry(retryCount)) {
            try {
                retryCount++
                Timber.d(
                    "Request to %s timed out. Retry %d out of %d",
                    request.url().toString(),
                    retryCount,
                    MAX_RETRIES
                )
                response = chain.proceed(request)
            } catch (e: SocketTimeoutException) {
                if (!shouldRetry(retryCount)) {
                    throw e
                }
            }

        }
        return response
    }

    private fun shouldHandleTimeouts(): Boolean {
        return MAX_RETRIES > 0
    }

    private fun shouldRetry(retryCount: Int): Boolean {
        return retryCount < MAX_RETRIES
    }

    companion object {
        private const val MAX_RETRIES = 3
    }
}