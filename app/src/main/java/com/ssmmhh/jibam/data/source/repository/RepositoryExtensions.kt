package com.ssmmhh.jibam.data.source.repository

import com.ssmmhh.jibam.R
import com.ssmmhh.jibam.data.util.CacheResult
import com.ssmmhh.jibam.data.util.MessageType
import com.ssmmhh.jibam.data.util.Response
import com.ssmmhh.jibam.data.util.UIComponentType
import com.ssmmhh.jibam.util.Constants.CACHE_TIMEOUT
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
                    CacheResult.GenericError(R.string.cache_timeout)
                }
                else -> {
                    CacheResult.GenericError(R.string.cache_unknown_error)
                }
            }
        }
    }
}

fun buildResponse(
    message: IntArray?,
    uiComponentType: UIComponentType = UIComponentType.Dialog,
    messageType: MessageType = MessageType.Error
): Response = Response(
    message = message,
    uiComponentType = uiComponentType,
    messageType = messageType
)