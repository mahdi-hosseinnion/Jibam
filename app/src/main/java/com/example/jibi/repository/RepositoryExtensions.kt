package com.example.jibi.repository

import android.util.Log
import com.example.jibi.util.*
import com.example.jibi.util.Constants.Companion.CACHE_TIMEOUT
import com.example.jibi.util.ErrorHandling.Companion.CACHE_ERROR_TIMEOUT
import com.example.jibi.util.ErrorHandling.Companion.UNKNOWN_ERROR
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

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
        DataState.data(it)
    }.onStart {
        emit(
            DataState.loading(true)
        )
    }.catch { cause ->
        Log.e("FLOW_ERROR", "safeFlowCall: message = ${cause.message}", cause)
        emit(
            DataState.error<T>(
                Response(
                    cause.message,
                    ResponseType.Dialog()
                )
            )
        )
    }.flowOn(Dispatchers.IO)


//TODO("handle loading here")
@ExperimentalCoroutinesApi
suspend fun <T> safeCacheCall(
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    transactionName: String,
    cacheCall: suspend () -> T
): Flow<DataState<T>> = flow {
    emit(DataState.loading(true))
    try {
        // throws TimeoutCancellationException
        withTimeout(CACHE_TIMEOUT) {
            //if result in update and delete ==0 it means error
            //and if result in insert == 0 or -1 it means error
            val result = cacheCall.invoke()
            if (result is Int || result is Long) {
                if ((result as Long) < 1) {
                    //error case in insert or update or delete
                    emit(
                        DataState.error<T>(
                            Response(
                                "Error: $transactionName fail.",
                                ResponseType.Dialog()
                            )
                        )
                    )
                } else {
                    emit(
                        DataState.data<T>(
                            result as T,
                            Response(
                                "Successfully  $transactionName",
                                ResponseType.None()
                            )
                        )
                    )
                }
            } else {
                emit(DataState.data(data = result))
            }
        }
    } catch (throwable: Throwable) {
        when (throwable) {
            is TimeoutCancellationException -> {
                emit(
                    DataState.error<T>(
                        Response(
                            CACHE_ERROR_TIMEOUT,
                            ResponseType.Dialog()
                        )
                    )
                )

            }
            else -> {
                emit(
                    DataState.error<T>(
                        Response(
                            UNKNOWN_ERROR,
                            ResponseType.Dialog()
                        )
                    )
                )
            }
        }
    }
}.flowOn(dispatcher)


