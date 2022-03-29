package com.ssmmhh.jibam.data.util

import com.ssmmhh.jibam.util.Response
import com.ssmmhh.jibam.util.StateEvent
import com.ssmmhh.jibam.util.StateMessage

data class DataState<T>(
    var stateMessage: StateMessage? = null,
    var data: T? = null,
    var stateEvent: StateEvent? = null
) {

    companion object {

        fun <T> error(
            response: Response,
            stateEvent: StateEvent? = null
        ): DataState<T> {
            return DataState(
                stateMessage = StateMessage(
                    response
                ),
                data = null,
                stateEvent = stateEvent
            )
        }

        fun <T> data(
            response: Response? = null,
            data: T? = null,
            stateEvent: StateEvent? = null
        ): DataState<T> {
            return DataState(
                stateMessage = response?.let {
                    StateMessage(
                        it
                    )
                },
                data = data,
                stateEvent = stateEvent
            )
        }

/*        fun <T> loading(isLoading: Boolean): DataState<T> {
            return DataState(
            )
        }*/
    }
}