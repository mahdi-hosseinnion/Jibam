package com.example.jibi.repository

import android.util.Log
import com.example.jibi.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withTimeout

@ExperimentalCoroutinesApi
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

}
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