package com.ssmmhh.jibam.data.source.repository

import com.ssmmhh.jibam.data.util.CacheResult
import com.ssmmhh.jibam.util.*
import com.ssmmhh.jibam.util.Constants.CACHE_TIMEOUT
import com.ssmmhh.jibam.util.ErrorHandling.Companion.CACHE_ERROR_TIMEOUT
import com.ssmmhh.jibam.util.ErrorHandling.Companion.UNKNOWN_ERROR
import kotlinx.coroutines.*

private const val TAG = "RepositoryExtensions"

suspend fun <T> safeCacheCall(
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    cacheCall: suspend () -> T?
): CacheResult<T?> {
    return withContext(dispatcher) {
        try {
            // throws TimeoutCancellationException
            withTimeout(CACHE_TIMEOUT) {
                CacheResult.Success(cacheCall.invoke())
            }
        } catch (throwable: Throwable) {
            when (throwable) {
                is TimeoutCancellationException -> {
                    CacheResult.GenericError(CACHE_ERROR_TIMEOUT)
                }
                else -> {
                    CacheResult.GenericError(UNKNOWN_ERROR)
                }
            }
        }
    }
}

fun buildResponse(
    message: String?,
    uiComponentType: UIComponentType = UIComponentType.Dialog,
    messageType: MessageType = MessageType.Error
): Response = Response(
    message = message,
    uiComponentType = uiComponentType,
    messageType = messageType
)