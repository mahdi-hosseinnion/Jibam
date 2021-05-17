package com.example.jibi.repository

import android.util.Log
import com.example.jibi.util.*
import com.example.jibi.util.Constants.CACHE_TIMEOUT
import com.example.jibi.util.ErrorHandling.Companion.CACHE_ERROR_TIMEOUT
import com.example.jibi.util.ErrorHandling.Companion.UNKNOWN_ERROR
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

private const val TAG = "RepositoryExtensions"

/*@ExperimentalCoroutinesApi
fun <T> safeFlowCall(
    timeOut: Long = Constants.CACHE_TIMEOUT,
    flowCallCall: () -> Flow<T?>
): Flow<DataState<T?>> = flow {
    try {
        emit(DataState.loading(true))
        // throws TimeoutCancellationException
        withTimeout(timeOut) {
            emitAll(flowCallCall.invoke()
                .onStart {
                    emit(DataState.loading<T?>(true))
                }
                .map {
                    DataState.data(it)
                }
                .catch { cause ->
                    Log.e("FLOW_ERROR", "safeFlowCall: message = ${cause.message}", cause)
                    emit(
                        DataState.error<T?>(
                            Response(
                                cause.message,
                                ResponseType.Dialog()
                            )
                        )
                    )
                })
        }
    } catch (throwable: Throwable) {
        when (throwable) {
            is TimeoutCancellationException -> {
                emit(
                    DataState.error<T?>(
                        Response(
                            ErrorHandling.CACHE_ERROR_TIMEOUT,
                            ResponseType.Dialog()
                        )
                    )
                )
            }
            else -> {
                emit(
                    DataState.error<T?>(
                        Response(
                            ErrorHandling.UNKNOWN_ERROR,
                            ResponseType.Dialog()
                        )
                    )
                )
            }
        }
    }

}*/
//TODO(test this method)
@ExperimentalCoroutinesApi
fun <T> Flow<T>.asDataState(): Flow<DataState<T>> =
//with one
//    map {
//    mapNotNull {
    map {
        DataState.data(data = it)
//    }
//        .onStart {
//        emit(
////            DataState.loading(true)
//        )
    }.catch { cause ->
        Log.e(TAG, "safeFlowCall: message = ${cause.message}", cause)
        emit(
            DataState.error<T>(
                Response(
                    cause.message,
                    UIComponentType.Dialog,
                    MessageType.Error
                )
            )
        )
    }.flowOn(Dispatchers.IO)


//TODO("handle loading here")
@ExperimentalCoroutinesApi
fun <T> safeFlowCacheCall(
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    transactionName: String,
    cacheCall: suspend () -> T,
    stateEvent: StateEvent?
): Flow<DataState<T>> = flow {
    try {
//        emit(DataState.loading(true))
        // throws TimeoutCancellationException
        withTimeout(CACHE_TIMEOUT) {
            //if result in update and delete ==0 it means error
            //and if result in insert == 0 or -1 it means error
            val result = cacheCall.invoke()
            if (result is Int || result is Long) {

                if ((convertToLong(result)) < 1) {
                    //error case in insert or update or delete
                    emit(
                        DataState.error<T>(
                            Response(
                                "Error: $transactionName fail.",
                                UIComponentType.Dialog,
                                MessageType.Error
                            )
                        )
                    )
                } else {
                    emit(
                        DataState.data<T>(
                            data = result as T,
                            response = Response(
                                "Successfully  ${stateEvent?.toString()}",
                                UIComponentType.Toast,
                                MessageType.Success
                            )
                        )
                    )
                }
            } else {
                //only get Transaction
                emit(DataState.data(data = result))
            }
        }
    } catch (throwable: Throwable) {
        Log.e(TAG, "safeCacheCall: ${throwable.message}", throwable)
        when (throwable) {
            is TimeoutCancellationException -> {
                emit(
                    DataState.error<T>(
                        Response(
                            CACHE_ERROR_TIMEOUT,
                            UIComponentType.Dialog,
                            MessageType.Error
                        )
                    )
                )

            }
            else -> {
                emit(
                    DataState.error<T>(
                        Response(
                            UNKNOWN_ERROR,
                            UIComponentType.Dialog,
                            MessageType.Error
                        )
                    )
                )
            }
        }
    }
}.flowOn(dispatcher)

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

fun <ViewState> buildError(
    message: String,
    uiComponentType: UIComponentType,
    stateEvent: StateEvent?
): DataState<ViewState> {
    return DataState.error(
        response = Response(
            message = "${stateEvent?.errorInfo()}\n\nReason: ${message}",
            uiComponentType = uiComponentType,
            messageType = MessageType.Error
        ),
        stateEvent = stateEvent
    )

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
private fun <T> convertToLong(value: T): Long {
    if (value is Long) {
        return value
    }
    if (value is Int) {
        return value.toLong()
    }
    throw Exception("this method only support long or int")
}