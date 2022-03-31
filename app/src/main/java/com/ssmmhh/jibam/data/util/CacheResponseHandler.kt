package com.ssmmhh.jibam.data.util

import com.ssmmhh.jibam.data.source.repository.buildResponse
import com.ssmmhh.jibam.util.*


abstract class CacheResponseHandler<ViewState, Data>(
    private val response: CacheResult<Data?>,
    private val stateEvent: StateEvent?
) {
    suspend fun getResult(): DataState<ViewState> {

        when (response) {

            is CacheResult.GenericError -> {
                return DataState.error(
                    response = Response(
                        message = "${stateEvent?.errorInfo}\n\nReason: ${response.errorMessage}",
                        uiComponentType = UIComponentType.Dialog,
                        messageType = MessageType.Error
                    ),
                    stateEvent = stateEvent
                )
            }

            is CacheResult.Success -> {
                if (response.value == null) {
                    return DataState.error(
                        response = Response(
                            message = "${stateEvent?.errorInfo}\n\nReason: Data is NULL.",
                            uiComponentType = UIComponentType.Dialog,
                            messageType = MessageType.Error
                        ),
                        stateEvent = stateEvent
                    )
                } else {
                    if (response.value is Long || response.value is Int) {//for insert or update or delete
                        if ((convertToLong(response.value)) < 1) {
                            //error case in insert or update or delete
                            return DataState.error(
                                buildResponse(message = "${stateEvent?.errorInfo}\n\nReason: Unknown Database Error!")
                            )
                        }
                    }

                    return handleSuccess(resultObj = response.value)

                }
            }

        }
    }

    abstract suspend fun handleSuccess(resultObj: Data): DataState<ViewState>

    private fun <T> convertToLong(value: T): Long {
        if (value is Long) {
            return value
        }
        if (value is Int) {
            return value.toLong()
        }
        throw Exception("this method only support long or int")
    }
}
