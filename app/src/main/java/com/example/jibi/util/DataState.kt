package com.example.jibi.util

data class DataState<T>(
    var stateMessage: StateMessage? = null,
    var data: T? = null,
    var stateEvent: StateEvent? = null,
    var isLoading: Boolean
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
                stateEvent = stateEvent,
                isLoading = false
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
                stateEvent = stateEvent,
                isLoading = false
            )
        }

        fun <T> loading(isLoading: Boolean): DataState<T> {
            return DataState(
                isLoading = isLoading
            )
        }
    }
}