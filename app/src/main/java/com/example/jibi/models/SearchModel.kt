package com.example.jibi.models

import com.example.jibi.persistence.RecordsDao

data class SearchModel(
    val query: String = "",
    val filter: DateBaseFilters = DateBaseFilters.None
) {
    fun isNotEmpty(): Boolean {
        if (query != "")
            return true
        if (filter != DateBaseFilters.None)
            return true
        return false
    }
}

sealed class DateBaseFilters {
    abstract fun toDBString(): String

    object None : DateBaseFilters() {
        override fun toDBString() = "UN IMPLEMENTED"
    }
}