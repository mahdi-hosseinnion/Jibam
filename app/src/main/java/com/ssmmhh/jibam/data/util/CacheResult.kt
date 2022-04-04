package com.ssmmhh.jibam.data.util

sealed class CacheResult<out T> {

    data class Success<out T>(val value: T): CacheResult<T>()

    data class GenericError(
        val errorMessage: Int? = null
    ): CacheResult<Nothing>()
}